package com.dabenxiang.mimi.view.club.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.LikeRequest
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.db.MiMiDB
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import retrofit2.HttpException
import timber.log.Timber

abstract class ClubViewModel : BaseViewModel(){

    val mimiDB: MiMiDB by inject()

    private val _followResult = MutableLiveData<ApiResult<Nothing>>()
    val followResult: LiveData<ApiResult<Nothing>> = _followResult

    private var _likePostResult = MutableLiveData<ApiResult<Int>>()
    val likePostResult: LiveData<ApiResult<Int>> = _likePostResult

    private var _videoLikeResult = MutableLiveData<ApiResult<VideoItem>>()
    val videoLikeResult: LiveData<ApiResult<VideoItem>> = _videoLikeResult

    private var _favoriteResult = MutableLiveData<ApiResult<Int>>()
    val favoriteResult: LiveData<ApiResult<Int>> = _favoriteResult

    private val _adResult = MutableLiveData<ApiResult<AdItem>>()
    val adResult: LiveData<ApiResult<AdItem>> = _adResult

    fun followMember(
        item: MemberPostItem,
        items: ArrayList<MemberPostItem>,
        isFollow: Boolean,
        update: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFollow -> apiRepository.followPost(item.creatorId)
                    else -> apiRepository.cancelFollowPost(item.creatorId)
                }
                if (!result.isSuccessful) throw HttpException(result)
                items.forEach {
                    if (it.creatorId == item.creatorId) {
                        it.isFollow = isFollow
                    }
                }
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _followResult.value = it }
        }
    }

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
                item.isFavorite = isFavorite
                _postChangedResult.postValue(ApiResult.success(item))
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion {
                        mimiDB.postDBItemDao().getMemberPostItemById(item.id)?.let { memberPostItem->
                            val item = memberPostItem.apply {
                                this.isFavorite = isFavorite
                                this.favoriteCount = when(isFavorite) {
                                    true -> this.favoriteCount+1
                                    else -> this.favoriteCount-1
                                }
                            }
                            mimiDB.postDBItemDao().insertMemberPostItem(item)
                        }
                 }
                .collect { _favoriteResult.value = it }
        }
    }

    fun likePost(
            item: MemberPostItem,
            position: Int,
            isLike: Boolean) {
        viewModelScope.launch {
            Timber.i("likePost item=$item")
            flow {
                val apiRepository = domainManager.getApiRepository()

                val likeType: LikeType = when {
                isLike -> LikeType.LIKE
                else -> LikeType.DISLIKE
            }
                val request = LikeRequest(likeType)
                val result = when {
                    isLike -> apiRepository.like(item.id, request)
                    else -> apiRepository.deleteLike(item.id)
                }
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion {
                    mimiDB.postDBItemDao().getMemberPostItemById(item.id)?.let { memberPostItem->
                        val item = memberPostItem.apply {
                            when {
                                isLike -> {
                                    this.likeType = LikeType.LIKE
                                    this.likeCount += 1
                                }
                                else-> {
                                    this.likeType = LikeType.DISLIKE
                                    this.likeCount -= 1
                                }
                            }
                        }
                        mimiDB.postDBItemDao().insertMemberPostItem(item)
                    }
                }
                .collect {
                    _likePostResult.value = it }
        }
    }

    fun videoLike(item: VideoItem, type: LikeType) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val request = LikeRequest(type)
                val result = apiRepository.like(item.id, request)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(item))
            }
                    .flowOn(Dispatchers.IO)
                    .onStart { emit(ApiResult.loading()) }
                    .onCompletion {
                        mimiDB.postDBItemDao().getMemberPostItemById(item.id)?.let { memberPostItem->
                            val item = memberPostItem.apply {
                                this.likeType = type
                                when(type) {
                                    LikeType.LIKE -> {
                                        this.likeCount += 1
                                    }
                                    else-> {
                                        this.likeCount -= 1
                                    }
                                }
                            }
                            mimiDB.postDBItemDao().insertMemberPostItem(item)
                        }
                        emit(ApiResult.loaded())
                    }
                    .catch { e -> emit(ApiResult.error(e)) }
                    .collect { _videoLikeResult.value = it }
        }
    }

    fun getAd() {
        viewModelScope.launch {
            flow {
                val adResult = domainManager.getAdRepository().getAD(adWidth, adHeight)
                if (!adResult.isSuccessful) throw HttpException(adResult)
                emit(ApiResult.success(adResult.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .catch { emit(ApiResult.error(it)) }
                .collect { _adResult.value = it}
        }

    }

}