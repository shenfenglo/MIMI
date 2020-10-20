package com.dabenxiang.mimi.view.clip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.FileIOUtils
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ApiBaseItem
import com.dabenxiang.mimi.model.api.vo.LikeRequest
import com.dabenxiang.mimi.model.api.vo.MeItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.VideoConsumeResult
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.widget.utility.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.io.File

class ClipViewModel : BaseViewModel() {

    private var _clipResult = MutableLiveData<ApiResult<Triple<String, Int, File>>>()
    val clipResult: LiveData<ApiResult<Triple<String, Int, File>>> = _clipResult

    private var _followResult = MutableLiveData<ApiResult<Int>>()
    val followResult: LiveData<ApiResult<Int>> = _followResult

    private var _favoriteResult = MutableLiveData<ApiResult<Int>>()
    val favoriteResult: LiveData<ApiResult<Int>> = _favoriteResult

    private var _likePostResult = MutableLiveData<ApiResult<Int>>()
    val likePostResult: LiveData<ApiResult<Int>> = _likePostResult

    private var _postDetailResult = MutableLiveData<ApiResult<Int>>()
    val postDetailResult: LiveData<ApiResult<Int>> = _postDetailResult

    private val _meItem = MutableLiveData<ApiResult<MeItem>>()
    val meItem: LiveData<ApiResult<MeItem>> = _meItem

    fun getClip(id: String, pos: Int) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteStream = result.body()?.byteStream()
                val filename = result.headers()["content-disposition"]
                    ?.split("; ")
                    ?.takeIf { it.size >= 2 }
                    ?.run { this[1].split("=") }
                    ?.takeIf { it.size >= 2 }
                    ?.run { this[1] }
                    ?: "$id.mov"
                val file = FileUtil.getClipFile(filename)
                FileIOUtils.writeFileFromIS(file, byteStream)

                emit(ApiResult.success(Triple(id, pos, file)))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _clipResult.value = it }
        }
    }

    fun followPost(item: MemberPostItem, position: Int, isFollow: Boolean) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFollow -> apiRepository.followPost(item.creatorId)
                    else -> apiRepository.cancelFollowPost(item.creatorId)
                }
                if (!result.isSuccessful) throw HttpException(result)
                item.isFollow = isFollow
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _followResult.value = it }
        }
    }

    fun getPostDetail(item: MemberPostItem, position: Int) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.getMemberPostDetail(item.id)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _postDetailResult.value = it }
        }
    }

    fun sendPlayerError(item: MemberPostItem, error: String) {
        //TODO
    }

    fun favoritePost(item: MemberPostItem, position: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFavorite -> apiRepository.addFavorite(item.id)
                    else -> apiRepository.deleteFavorite(item.id)
                }
                if (!result.isSuccessful) throw HttpException(result)
                item.isFavorite = isFavorite
                if (isFavorite) item.favoriteCount++ else item.favoriteCount--
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _favoriteResult.value = it }
        }
    }

    fun likePost(item: MemberPostItem, position: Int, isLike: Boolean) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val likeType = when {
                    isLike -> LikeType.LIKE
                    else -> LikeType.DISLIKE
                }
                val request = LikeRequest(likeType)
                val result = apiRepository.like(item.id, request)
                if (!result.isSuccessful) throw HttpException(result)

                item.likeType = likeType
                item.likeCount = when (item.likeType) {
                    LikeType.LIKE -> item.likeCount + 1
                    else -> item.likeCount - 1
                }

                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _likePostResult.value = it }
        }
    }

    fun getMe(item: MemberPostItem, position: Int) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getMe()
                if (!result.isSuccessful) throw HttpException(result)
                val meItem = result.body()?.content
                meItem?.let {
                    accountManager.setupProfile(it)
                }
                emit(ApiResult.success(meItem))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    when (it) {
                        is ApiResult.Success -> {
                            it.result.takeIf { checkConsumeResult(it) }?.let {
                                getPostDetail(item, position)
                            }
                        }
                        is ApiResult.Error -> _meItem.value = it
                        else -> _meItem.value = it
                    }

                }
        }

    }

    private fun checkConsumeResult(me: MeItem): Boolean {
        Timber.i("checkConsumeResult me:$me")
        return when {
            me.isSubscribed -> true
            me.videoCount ?: 0 > 0 -> true
            else -> false
        }
    }
}
