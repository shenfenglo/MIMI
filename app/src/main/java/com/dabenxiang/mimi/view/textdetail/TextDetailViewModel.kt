package com.dabenxiang.mimi.view.textdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.LikeRequest
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.MembersPostCommentItem
import com.dabenxiang.mimi.model.api.vo.ReportRequest
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class TextDetailViewModel : BaseViewModel() {

    private var _followPostResult = MutableLiveData<ApiResult<Int>>()
    val followPostResult: LiveData<ApiResult<Int>> = _followPostResult

    private var _likePostResult = MutableLiveData<ApiResult<MemberPostItem>>()
    val likePostResult: LiveData<ApiResult<MemberPostItem>> = _likePostResult

    private val _postReportResult = MutableLiveData<ApiResult<Nothing>>()
    val postReportResult: LiveData<ApiResult<Nothing>> = _postReportResult

    private val _postCommentReportResult = MutableLiveData<ApiResult<Nothing>>()
    val postCommentReportResult: LiveData<ApiResult<Nothing>> = _postCommentReportResult

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
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _followPostResult.value = it }
        }
    }

    fun likePost(item: MemberPostItem, isLike: Boolean) {
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

                emit(ApiResult.success(item))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _likePostResult.value = it }
        }
    }

    fun sendPostReport(item: MemberPostItem, content: String) {
        viewModelScope.launch {
            flow {
                val request = ReportRequest(content)
                val result = domainManager.getApiRepository().sendPostReport(item.id, request)
                if (!result.isSuccessful) throw HttpException(result)
                item.reported = true
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _postReportResult.value = it }
        }
    }

    fun sendCommentPostReport(
        postItem: MemberPostItem,
        postCommentItem: MembersPostCommentItem,
        content: String
    ) {
        viewModelScope.launch {
            flow {
                val request = ReportRequest(content)
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.sendPostCommentReport(
                    postItem.id, postCommentItem.id!!, request
                )
                if (!result.isSuccessful) throw HttpException(result)
                postCommentItem.reported = true
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _postCommentReportResult.value = it }
        }
    }
}