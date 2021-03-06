package com.dabenxiang.mimi.view.topup

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.TopUpPagingCallback
import com.dabenxiang.mimi.model.api.vo.AgentItem
import com.dabenxiang.mimi.model.manager.DomainManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class TopUpProxyPayListDataSource constructor(
    private val viewModelScope: CoroutineScope,
    private val domainManager: DomainManager,
    private val pagingCallback: TopUpPagingCallback
) : PageKeyedDataSource<Long, AgentItem>() {

    companion object {
        const val PER_LIMIT = 10
        val PER_LIMIT_LONG = PER_LIMIT.toLong()
    }

    private data class InitResult(val list: List<AgentItem>, val nextKey: Long?)
    override fun loadInitial(
        params: LoadInitialParams<Long>,
        callback: LoadInitialCallback<Long, AgentItem>
    ) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                    .getAgent(0, PER_LIMIT)
                if (!result.isSuccessful) throw HttpException(result)
                val item = result.body()
                val items = item?.content
                pagingCallback.listEmpty(items?.size?:0 == 0)
                val nextPageKey = when {
                    hasNextPage(
                        item?.paging?.count ?: 0,
                        item?.paging?.offset ?: 0,
                        items?.size ?: 0
                    ) -> PER_LIMIT_LONG
                    else -> null
                }

                emit(InitResult(items ?: arrayListOf(), nextPageKey))
            }
                .flowOn(Dispatchers.IO)
                .onStart { pagingCallback.onLoading() }
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .collect { response -> callback.onResult(response.list, null, response.nextKey) }
        }
    }

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Long, AgentItem>) {}

    override fun loadAfter(
        params: LoadParams<Long>,
        callback: LoadCallback<Long, AgentItem>
    ) {
        val next = params.key
        viewModelScope.launch {
            flow {
                val result =
                    domainManager.getApiRepository().getAgent(
                        next.toInt(),
                        PER_LIMIT
                    )
                if (!result.isSuccessful) throw HttpException(result)
                emit(result)
            }
                .flowOn(Dispatchers.IO)
                .onStart { pagingCallback.onLoading() }
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .collect { response ->
                    response.body()?.also { item ->
                        item.content?.also { list ->
                            val nextPageKey = when {
                                hasNextPage(
                                    item.paging.count,
                                    item.paging.offset,
                                    list.size
                                ) -> next + PER_LIMIT_LONG
                                else -> null
                            }
                            callback.onResult(list, nextPageKey)
                        }
                    }
                }
        }
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < PER_LIMIT_LONG -> false
            offset >= total -> false
            else -> true
        }
    }
}