package com.dabenxiang.mimi.view.club.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.db.MemberPostWithPostDBItem
import com.dabenxiang.mimi.model.db.PostDBItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.my_pages.base.MyPagesType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

abstract class ClubViewModel : BaseViewModel() {

    private val _postCount = MutableLiveData<Int>()
    val postCount: LiveData<Int> = _postCount

    private val _followResult = MutableLiveData<ApiResult<Nothing>>()
    val followResult: LiveData<ApiResult<Nothing>> = _followResult

    private var _likePostResult = MutableLiveData<ApiResult<Int>>()
    val likePostResult: LiveData<ApiResult<Int>> = _likePostResult

    private var _videoLikeResult = MutableLiveData<ApiResult<Int>>()
    val videoLikeResult: LiveData<ApiResult<Int>> = _videoLikeResult

    private var _favoriteResult = MutableLiveData<ApiResult<Int>>()
    val favoriteResult: LiveData<ApiResult<Int>> = _favoriteResult

    fun followPost(items: ArrayList<MemberPostItem>, position: Int, isFollow: Boolean) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFollow -> apiRepository.followPost(items[position].creatorId)
                    else -> apiRepository.cancelFollowPost(items[position].creatorId)
                }
                if (!result.isSuccessful) throw HttpException(result)
                items.forEach { item ->
                    if (items[position].creatorId == item.creatorId)
                        item.isFollow = isFollow
                }
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _followResult.value = it }
        }
    }

    open fun favoritePost(
        item: MemberPostItem,
        position: Int,
        isFavorite: Boolean
    ) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFavorite -> apiRepository.addFavorite(item.id)
                    else -> apiRepository.deleteFavorite(item.id)
                }
                if (!result.isSuccessful) throw HttpException(result)
                val countItem = result.body()?.content.let {
                    when {
                        isFavorite -> it
                        else -> (it as ArrayList<*>)[0]
                    }
                } as InteractiveHistoryItem
                item.isFavorite = isFavorite
                item.favoriteCount = countItem.favoriteCount?.toInt()?:0
                changeFavoritePostInDb(item.id, isFavorite, countItem.favoriteCount?.toInt()?:0)
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .onStart { setShowProgress(true) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { setShowProgress(false) }
                .collect { _favoriteResult.value = it }
        }
    }

    open fun likePost(
        item: MemberPostItem,
        position: Int,
        isLike: Boolean
    ) {
        viewModelScope.launch {
            Timber.i("likePost item=$item")
            flow {
                val apiRepository = domainManager.getApiRepository()
                val request = LikeRequest(LikeType.LIKE)
                val result = when {
                    isLike -> apiRepository.like(item.id, request)
                    else -> apiRepository.deleteLike(item.id)
                }
                if (!result.isSuccessful) throw HttpException(result)
                val countItem = result.body()?.content.let {
                    when {
                        isLike -> it
                        else -> (it as ArrayList<*>)[0]
                    }
                } as InteractiveHistoryItem
                item.likeType = if (isLike) LikeType.LIKE else null
                item.likeCount = countItem.likeCount?.toInt() ?: 0
                changeLikePostInDb(item.id, if (isLike) LikeType.LIKE else null, countItem.likeCount?.toInt() ?: 0)
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .onStart { setShowProgress(true) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { setShowProgress(false) }
                .collect {
                    _likePostResult.value = it
                }
        }
    }

    fun videoLike(item: VideoItem, position: Int, type: LikeType?, pageType: MyPagesType) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val request = LikeRequest(type)
                val result = apiRepository.like(item.id, request)
                if (!result.isSuccessful) throw HttpException(result)
                item.likeType = type
                item.likeCount = result.body()?.content?.likeCount?.toInt() ?: 0
                when (pageType) {
                    MyPagesType.FAVORITE_MIMI_VIDEO,
                    MyPagesType.LIKE_MIMI -> changeLikeMimiVideoInDb(item.id, type, item.likeCount)
                }
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { setShowProgress(false) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _videoLikeResult.value = it }
        }
    }

    open val pagingCallback = object : PagingCallback {
        override fun onTotalCount(count: Long) {
            _postCount.postValue(count.toInt())
        }
    }

    suspend fun checkoutItemsSize(pageCode: String): Int {
        return mimiDB.withTransaction {
            mimiDB.postDBItemDao().getPostDBItems(pageCode)?.size ?: 0
        }
    }
}