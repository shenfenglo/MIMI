package com.dabenxiang.mimi.view.base

import android.app.Application
import android.net.Uri
import android.text.TextUtils
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.dabenxiang.mimi.PROJECT_NAME
import com.dabenxiang.mimi.extension.decryptSource
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.ExceptionResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.db.DBRemoteKey
import com.dabenxiang.mimi.model.db.MiMiDB
import com.dabenxiang.mimi.model.db.PostDBItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.model.manager.mqtt.MQTTManager
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.view.my_pages.base.MyPagesPostMediator
import com.dabenxiang.mimi.view.my_pages.base.MyPagesType
import com.dabenxiang.mimi.widget.utility.FileUtil
import com.dabenxiang.mimi.widget.utility.GeneralUtils.getExceptionDetail
import com.dabenxiang.mimi.widget.utility.LoadImageUtils
import com.google.gson.Gson
import io.ktor.client.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.HttpException
import timber.log.Timber
import tw.gov.president.manager.submanager.logmoniter.di.SendLogManager

abstract class BaseViewModel : ViewModel(), KoinComponent {

    val mimiDB: MiMiDB by inject()

    companion object {
        const val POP_HINT_DURATION = 1000L
        const val POP_HINT_ANIM_TIME = 500L
    }

    val app: Application by inject()
    val gson: Gson by inject()
    val pref: Pref by inject()
    val accountManager: AccountManager by inject()
    val domainManager: DomainManager by inject()
    val mqttManager: MQTTManager by inject()

    var adWidth = 0
    var adHeight = 0

    private val _showProgress by lazy { MutableLiveData<Boolean>() }
    val showProgress: LiveData<Boolean> get() = _showProgress

    private val _showPopHint = MutableLiveData<String>()
    val showPopHint: LiveData<String> = _showPopHint

    private val _topAdResult = MutableLiveData<AdItem>()
    val topAdResult: LiveData<AdItem> = _topAdResult

    private val _bottomAdResult = MutableLiveData<AdItem>()
    val bottomAdResult: LiveData<AdItem> = _bottomAdResult

    fun setShowProgress(show: Boolean) {
        _showProgress.value = show
    }

    fun setShowPopHint(text: String) {
        viewModelScope.launch {
            _showPopHint.postValue(text)
            delay(POP_HINT_DURATION)
            _showPopHint.postValue("")
        }
    }

    fun processException(exceptionResult: ExceptionResult) {
        when (exceptionResult) {
            is ExceptionResult.Crash -> {
                sendCrashReport(getExceptionDetail(exceptionResult.throwable))
            }
            is ExceptionResult.HttpError -> {
                sendCrashReport(getExceptionDetail(exceptionResult.httpExceptionItem.httpExceptionClone))
            }
        }
    }

    fun isLogin(): Boolean {
        return accountManager.isLogin()
    }

    fun logoutLocal() {
        accountManager.logoutLocal()
    }

    /**
     * 取得要分享的 URL
     * @param categoryURI 分類tab, e.g: 首頁、電視劇、綜藝、etc.
     * @param videoId 該影片 ID
     * @param episodeID 該級數 ID, 若為 null or -1 不帶, 反之加上 "&e="
     */
    fun getShareUrl(categoryURI: String, videoId: Long, episodeID: String? = null): String {
        val result: StringBuilder = StringBuilder()
        result.append(domainManager.getApiDomain())
            .append("/play/")
            .append(Uri.encode(categoryURI))
            .append("?u=")
            .append(videoId)
        if (episodeID != null && episodeID != "-1") {
            result.append("&e=")
                .append(episodeID)
        }
        return result.toString()
    }

    fun sendCrashReport(data: String) {
        SendLogManager.e(PROJECT_NAME, data)
    }

    private var _deletePostResult = MutableLiveData<ApiResult<Int>>()
    val deletePostResult: LiveData<ApiResult<Int>> = _deletePostResult

    private val _cleanRemovedPosList = MutableLiveData<Nothing>()
    val cleanRemovedPosList: LiveData<Nothing> = _cleanRemovedPosList

    fun deletePost(
        item: MemberPostItem,
        position: Int
    ) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.deleteMyPost(item.id)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion {
                    mimiDB.withTransaction {
                        mimiDB.postDBItemDao().deleteMemberPostItem(item.id)
                        mimiDB.postDBItemDao().deleteItem(item.id)
                    }
                }
                .collect {
                    _deletePostResult.value = it
                }
        }
    }

    fun clearDeletePostResult() {
        _deletePostResult.value = null
    }

    fun cleanRemovedPosList() {
        _cleanRemovedPosList.value = null
    }

    fun loadImage(id: Long? = 0, view: ImageView, type: LoadImageType, filePath: String = "") {
        viewModelScope.launch {
            LoadImageUtils.loadImage(id, view, type, filePath)
        }
    }

    fun getDecryptSetting(source: String): DecryptSettingItem? {
        var result: DecryptSettingItem? = null
        pref.decryptSettingArray.forEach {
            if (TextUtils.equals(source, it.source)) {
                result = it
                return@forEach
            }
        }
        return result
    }

    fun decryptCover(
        encryptCover: String,
        decryptItem: DecryptSettingItem,
        update: (ByteArray) -> Unit
    ) {
        viewModelScope.launch {
            HttpClient().decryptSource(encryptCover, decryptItem.key ?: "".toByteArray())
                .catch { update("".toByteArray()) }
                .collect {
                    when (it) {
                        is DownloadResult.Success -> update(it.data as ByteArray)
                        else -> {
                        }
                    }
                }
        }
    }

    fun decryptM3U8(
        encryptM3U8: String,
        decryptItem: DecryptSettingItem,
        update: (String) -> Unit
    ) {
        viewModelScope.launch {
            HttpClient().decryptSource(encryptM3U8, decryptItem.key ?: "".toByteArray())
                .catch { update("") }
                .collect {
                    when (it) {
                        is DownloadResult.Success -> {
                            val path = FileUtil.unzipSourceToFile(it.data as ByteArray)
                            update(path)
                        }
                        else -> {
                        }
                    }
                }
        }
    }

    fun clearDBData() {
        viewModelScope.launch {
            mimiDB.apply {
                withTransaction {
                    postDBItemDao().deleteAll()
                    postDBItemDao().deleteAllMemberPostItems()
                }
            }
        }

    }

    suspend fun changeFavoritePostInDb(id: Long, isFavorite:Boolean, favoriteCount: Int) {
        changeFavoriteInDb(id, MyPagesType.FAVORITE_POST, isFavorite, favoriteCount)
    }

    suspend fun changeFavoriteMimiVideoInDb(id: Long, isFavorite:Boolean, favoriteCount: Int) {
        changeFavoriteInDb(id, MyPagesType.FAVORITE_MIMI_VIDEO, isFavorite, favoriteCount)
    }

    suspend fun changeFavoriteSmallVideoInDb(id: Long, isFavorite:Boolean, favoriteCount: Int) {
        changeFavoriteInDb(id, MyPagesType.FAVORITE_SHORT_VIDEO, isFavorite, favoriteCount)
    }

    private suspend fun changeFavoriteInDb(id: Long, type: MyPagesType, isFavorite:Boolean, favoriteCount: Int) {
        mimiDB.withTransaction {
            mimiDB.postDBItemDao().getMemberPostItemById(id)?.let { memberPostItem ->
                val dbItem = memberPostItem.apply {
                    this.isFavorite = isFavorite
                    this.favoriteCount = favoriteCount
                }
                mimiDB.postDBItemDao().insertMemberPostItem(dbItem)
                val pageCode =
                    MyPagesPostMediator::class.simpleName + type.toString()
                if (!isFavorite) {
                    mimiDB.postDBItemDao().deleteItemByPageCode(pageCode, id)
                } else {
                    mimiDB.remoteKeyDao().insertOrReplace(DBRemoteKey(pageCode, 0))
                    val timestamp =
                        mimiDB.postDBItemDao().getFirstPostDBItem(pageCode)?.timestamp?.minus(1)
                            ?: System.nanoTime()
                    mimiDB.postDBItemDao().insertItem(
                        PostDBItem(
                            postDBId = id,
                            postType = dbItem.type,
                            pageCode = pageCode,
                            timestamp = timestamp,
                            index = 0
                        )
                    )
                }
            }
        }
    }

    suspend fun changeLikePostInDb(id: Long, likeType: LikeType?, likeCount: Int) {
        changeLikeInDb(id, MyPagesType.LIKE_POST, likeType, likeCount)
    }

    suspend fun changeLikeMimiVideoInDb(id: Long, likeType: LikeType?, likeCount: Int) {
        changeLikeInDb(id, MyPagesType.LIKE_MIMI, likeType, likeCount)
    }

    private suspend fun changeLikeInDb(id: Long, type: MyPagesType, likeType: LikeType?, likeCount: Int) {
        mimiDB.withTransaction {
            mimiDB.postDBItemDao().getMemberPostItemById(id)?.let { memberPostItem ->
                val dbItem = memberPostItem.apply {
                    this.likeType = likeType
                    this.likeCount = likeCount
                }
                mimiDB.postDBItemDao().insertMemberPostItem(dbItem)

                if (type == MyPagesType.LIKE_POST || type == MyPagesType.LIKE_MIMI) {
                    val pageCode =
                        MyPagesPostMediator::class.simpleName + type.toString()
                    if (likeType != LikeType.LIKE) {
                        mimiDB.postDBItemDao().deleteItemByPageCode(pageCode, id)
                    } else {
                        mimiDB.remoteKeyDao().insertOrReplace(DBRemoteKey(pageCode, 0))
                        val timestamp =
                            mimiDB.postDBItemDao().getFirstPostDBItem(pageCode)?.timestamp?.minus(1)
                                ?: System.nanoTime()
                        mimiDB.postDBItemDao().insertItem(
                            PostDBItem(
                                postDBId = id,
                                postType = dbItem.type,
                                pageCode = pageCode,
                                timestamp = timestamp,
                                index = 0
                            )
                        )
                    }
                }
            }
        }
    }

    suspend fun changeCommentInDb(id: Long, count: Int) {
        mimiDB.withTransaction {
            mimiDB.postDBItemDao().getMemberPostItemById(id)?.let { memberPostItem ->
                val dbItem = memberPostItem.apply {
                    this.commentCount = count
                }
                mimiDB.postDBItemDao().insertMemberPostItem(dbItem)
            }
        }
    }

    fun getTopAd(code: String) {
        viewModelScope.launch {
            getAd(code).collect { _topAdResult.value = it }
        }
    }

    fun getBottomAd(code: String) {
        viewModelScope.launch {
            getAd(code).collect { _bottomAdResult.value = it }
        }
    }

    fun getAd(code: String): Flow<AdItem> {
        return flow {
            val adItem =
                domainManager.getAdRepository().getAD(code, adWidth, adHeight)
                    .body()?.content?.get(0)?.ad?.first() ?: AdItem()
            emit(adItem)
        }
            .flowOn(Dispatchers.IO)
    }

    fun cleanDb() {
        viewModelScope.launch {
            mimiDB.withTransaction {
                mimiDB.postDBItemDao().deleteAll()
                mimiDB.postDBItemDao().deleteAllMemberPostItems()
                mimiDB.remoteKeyDao().deleteAll()
            }
        }
    }
}
