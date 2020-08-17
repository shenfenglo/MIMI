package com.dabenxiang.mimi.view.favroite

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.FavoritePagingCallback
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.model.api.vo.PlayItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class FavoritePlayListDataSource constructor(
    private val viewModelScope: CoroutineScope,
    private val domainManager: DomainManager,
    private val pagingCallback: FavoritePagingCallback,
    private val playlistType: Int = 1,
    private val isAdult: Boolean = false
) : PageKeyedDataSource<Long, Any>() {

    companion object {
        const val PER_LIMIT = "10"
        val PER_LIMIT_LONG = PER_LIMIT.toLong()
    }

    private data class InitResult(val list: List<Any>, val nextKey: Long?)

    override fun loadInitial(
        params: LoadInitialParams<Long>,
        callback: LoadInitialCallback<Long, Any>
    ) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                    .getPlaylist(playlistType, isAdult, "0", PER_LIMIT)
                if (!result.isSuccessful) throw HttpException(result)
                val item = result.body()
                val items = item?.content

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
                .collect { response ->
                    pagingCallback.onTotalCount(response.list.size)
                    val ids = ArrayList<Long>()
                    response.list.forEach {
                        (it as PlayItem).videoId?.let { it1 -> ids.add(it1) }
                    }
                    pagingCallback.onTotalVideoId(ids, true)
                    callback.onResult(response.list, null, response.nextKey)
                }
        }
    }

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Long, Any>) {}

    override fun loadAfter(
        params: LoadParams<Long>,
        callback: LoadCallback<Long, Any>
    ) {
        val next = params.key
        viewModelScope.launch {
            flow {
                val result =
                    domainManager.getApiRepository().getPlaylist(
                        playlistType,
                        isAdult,
                        next.toString(),
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
                            val ids = ArrayList<Long>()
                            list.forEach {
                                it.videoId?.let { it1 -> ids.add(it1) }
                            }
                            pagingCallback.onTotalVideoId(ids,false)

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