package com.dabenxiang.mimi.view.chathistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.blankj.utilcode.util.ImageUtils
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ChatListItem
import com.dabenxiang.mimi.model.vo.AttachmentItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ChatHistoryViewModel : BaseViewModel() {
    private val _chatHistory = MutableLiveData<PagedList<ChatListItem>>()
    val chatHistory: LiveData<PagedList<ChatListItem>> = _chatHistory

    private var _attachmentResult = MutableLiveData<ApiResult<AttachmentItem>>()
    val attachmentResult: LiveData<ApiResult<AttachmentItem>> = _attachmentResult

    private val pagingCallback = object : PagingCallback {
        override fun onLoading() {

        }

        override fun onLoaded() {

        }

        override fun onThrowable(throwable: Throwable) {

        }

        override fun onSucceed() {
            super.onSucceed()
        }
    }

    private fun getChatHistoryPagingItems(): LiveData<PagedList<ChatListItem>> {
        val dataSrc = ChatHistoryListDataSource(
                viewModelScope,
                domainManager,
                pagingCallback
        )
        val factory = ChatHistoryListFactory(dataSrc)
        val config = PagedList.Config.Builder()
                .setPageSize(ChatHistoryListDataSource.PER_LIMIT.toInt())
                .build()

        return LivePagedListBuilder(factory, config).build()
    }

    fun getChatList() {
        viewModelScope.launch {
            getChatHistoryPagingItems().asFlow()
                    .collect { _chatHistory.postValue(it) }
        }
    }

    fun getAttachment(id: String, position: Int) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                val bitmap = ImageUtils.bytes2Bitmap(byteArray)
                val item = AttachmentItem(
                        id = id,
                        bitmap = bitmap,
                        position = position
                )
                emit(ApiResult.success(item))
            }
                    .flowOn(Dispatchers.IO)
                    .onStart { emit(ApiResult.loading()) }
                    .onCompletion { emit(ApiResult.loaded()) }
                    .catch { e -> emit(ApiResult.error(e)) }
                    .collect { _attachmentResult.value = it }
        }
    }
}