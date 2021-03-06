package com.dabenxiang.mimi.view.player

import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.model.api.vo.MembersPostCommentItem
import com.dabenxiang.mimi.view.base.JFPageDataSource
import retrofit2.HttpException
import timber.log.Timber

class CommentDataSource(
    private val postId: Long,
    private val sorting: Int,
    private val domainManager: DomainManager
) :
    JFPageDataSource<Long, List<MembersPostCommentItem>>() {

    companion object {
        const val PER_LIMIT = "20"
        val PER_LIMIT_LONG = PER_LIMIT.toLong()
    }

    override suspend fun load(params: Long?): JFLoadResult {
        try {
            val nowOffset = params ?: 0L

            val resp = domainManager.getApiRepository()
                .getMembersPostComment(
                    postId = postId,
                    sorting = sorting,
                    offset = nowOffset.toString(),
                    limit = PER_LIMIT
                )
            if (!resp.isSuccessful) throw HttpException(resp)

            val body = resp.body()!!
            val paging = body.paging
            val nextPageKey = when {
                hasNextPage(
                    paging.count,
                    paging.offset,
                    body.content?.size ?: 0
                ) -> nowOffset + PER_LIMIT_LONG
                else -> null
            }

            return JFLoadResult.Page(nextPageKey, body.content)
        } catch (e: Exception) {
            Timber.e(e)
            return JFLoadResult.PageError(e)
        }
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < PER_LIMIT_LONG -> false
            offset >= total -> false
            offset + currentSize.toLong() == total -> false
            else -> true
        }
    }
}