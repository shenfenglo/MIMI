package com.dabenxiang.mimi.view.mypost

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dabenxiang.mimi.model.db.DBRemoteKey
import com.dabenxiang.mimi.model.db.MemberPostWithPostDBItem
import com.dabenxiang.mimi.model.db.MiMiDB
import com.dabenxiang.mimi.model.db.PostDBItem
import com.dabenxiang.mimi.model.enums.ClubTabItemType
import com.dabenxiang.mimi.model.manager.DomainManager
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class MyPostMediator(
        private val database: MiMiDB,
        private val domainManager: DomainManager,
        private val userId: Long
) : RemoteMediator<Int, MemberPostWithPostDBItem>() {

    companion object {
        const val PER_LIMIT = 10
    }
    private val pageCode = MyPostMediator::class.simpleName + userId.toString()

    override suspend fun load(
            loadType: LoadType,
            state: PagingState<Int, MemberPostWithPostDBItem>
    ): MediatorResult {
        try {
            val offset = when (loadType) {
                LoadType.REFRESH -> {
                    database.remoteKeyDao().insertOrReplace(DBRemoteKey(pageCode, null))
                    null
                }
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKey = database.withTransaction {
                        database.remoteKeyDao().remoteKeyByPageCode(pageCode)
                    }

                    if (remoteKey?.offset == null) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }
                    remoteKey.offset

                }
            }?.toInt() ?: 0

            Timber.i("MyPostMediator pageName=$pageCode offset=$offset userId=$userId")

            val result =
                    if (userId == MyPostViewModel.USER_ID_ME) domainManager.getApiRepository().getMyPost(offset = offset, limit = PER_LIMIT)
                    else domainManager.getApiRepository().getMembersPost(
                            offset = offset,
                            limit = PER_LIMIT,
                            creatorId = userId,
                            isAdult = true
                    )

            Timber.i("MyPostMediator pageName=$pageCode result=$result ")
            if (!result.isSuccessful) throw HttpException(result)

            val body = result.body()
            val memberPostItems = body?.content

            val hasNext = hasNextPage(
                    result.body()?.paging?.count ?: 0,
                    result.body()?.paging?.offset ?: 0,
                    memberPostItems?.size ?: 0
            )

            database.withTransaction {
                if(loadType == LoadType.REFRESH){
                    database.postDBItemDao().getPostDBIdsByPageCode(pageCode)?.forEach {id->
                        database.postDBItemDao().getPostDBItems(id).takeIf {
                            it.isNullOrEmpty() || it.size <=1
                        }?.let {
                            database.postDBItemDao().deleteMemberPostItem(id)
                        }

                    }
                    database.postDBItemDao().deleteItemByPageCode(pageCode)
                    database.remoteKeyDao().deleteByPageCode(pageCode)
                }
                val nextKey = if (hasNext) offset + PER_LIMIT else null

                database.remoteKeyDao().insertOrReplace(DBRemoteKey(pageCode, nextKey?.toLong()))

                memberPostItems?.map { item->
                    item.deducted = true
                    item
                }?.let {
                    val postDBItems = it.mapIndexed { index, item ->
                        val oldItem = database.postDBItemDao().getPostDBItem(pageCode, item.id)

                        when(oldItem) {
                            null-> PostDBItem(
                                    postDBId = item.id,
                                    postType = item.type,
                                    pageCode= pageCode,
                                    timestamp = System.nanoTime(),
                                    index = offset+index

                            )
                            else-> {
                                oldItem.postDBId = item.id
                                oldItem.timestamp = System.nanoTime()
                                oldItem.index = offset+index
                                oldItem
                            }
                        }
                    }
                    database.postDBItemDao().insertMemberPostItemAll(it)
                    database.postDBItemDao().insertAll(postDBItems)
                }

            }

            return MediatorResult.Success(endOfPaginationReached = !hasNext)
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            return MediatorResult.Error(e)
        }
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < PER_LIMIT -> false
            offset >= total -> false
            else -> true
        }
    }

}