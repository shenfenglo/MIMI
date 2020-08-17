package com.dabenxiang.mimi.view.myfollow

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.MyFollowPagingCallback
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ClubFollowListDataSource constructor(
    private val viewModelScope: CoroutineScope,
    private val domainManager: DomainManager,
    private val pagingCallback: MyFollowPagingCallback
) : PageKeyedDataSource<Long, ClubFollowItem>() {

    companion object {
        const val PER_LIMIT = "10"
        val PER_LIMIT_LONG = PER_LIMIT.toLong()
    }

    private data class InitResult(val list: List<ClubFollowItem>, val nextKey: Long?)

    override fun loadInitial(
        params: LoadInitialParams<Long>,
        callback: LoadInitialCallback<Long, ClubFollowItem>
    ) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getMyClubFollow("0", PER_LIMIT)
                if (!result.isSuccessful) throw HttpException(result)
                val item = result.body()
                val clubs = item?.content

                val nextPageKey = when {
                    hasNextPage(
                        item?.paging?.count ?: 0,
                        item?.paging?.offset ?: 0,
                        clubs?.size ?: 0
                    ) -> PER_LIMIT_LONG
                    else -> null
                }
                emit(InitResult(clubs ?: arrayListOf(), nextPageKey))
            }
                .flowOn(Dispatchers.IO)
                .onStart { pagingCallback.onLoading() }
                .catch { e -> pagingCallback.onThrowable(e) }
                .onCompletion { pagingCallback.onLoaded() }
                .collect { response ->
                    pagingCallback.onTotalCount(response.list.size.toLong(), true)
                    val idList = ArrayList<Long>()
                    response.list.forEach {
                        idList.add(it.clubId)
                    }
                    pagingCallback.onIdList(idList, true)
                    callback.onResult(response.list, null, response.nextKey)
                }
        }
    }

    override fun loadBefore(
        params: LoadParams<Long>,
        callback: LoadCallback<Long, ClubFollowItem>
    ) {}

    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<Long, ClubFollowItem>) {
        val next = params.key
        viewModelScope.launch {
            flow {
                val result =
                    domainManager.getApiRepository().getMyClubFollow(next.toString(), PER_LIMIT)
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
                            pagingCallback.onTotalCount(list.size.toLong(), false)
                            val idList = ArrayList<Long>()
                            list.forEach {
                                idList.add(it.clubId)
                            }
                            pagingCallback.onIdList(idList, false)
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