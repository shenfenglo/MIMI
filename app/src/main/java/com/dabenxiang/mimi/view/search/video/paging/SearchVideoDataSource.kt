package com.dabenxiang.mimi.view.search.video.paging

import androidx.paging.PagingSource
import com.dabenxiang.mimi.callback.SearchPagingCallback
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.VideoType
import com.dabenxiang.mimi.model.manager.DomainManager
import retrofit2.HttpException
import kotlin.math.ceil

class SearchVideoDataSource(
    private val domainManager: DomainManager,
    private val pagingCallback: SearchPagingCallback,
    private val category: String = "",
    private val tag: String? = null,
    private val keyword: String? = null,
    private val adWidth: Int,
    private val adHeight: Int,
    private val videoType: VideoType? = null
) : PagingSource<Long, VideoItem>() {

    companion object {
        const val PER_LIMIT = 10
    }

    private var adIndex = 0
    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, VideoItem> {
        val offset = params.key ?: 0
        return try {

            val result =
                domainManager.getApiRepository().searchHomeVideos(
                    q = keyword,
                    tag = tag,
                    category = category,
                    type = videoType,
                    offset = offset.toString(),
                    limit = PER_LIMIT.toString()
                )
            if (!result.isSuccessful) throw HttpException(result)
            val memberPostItems = result.body()?.content?.videos

            val list = arrayListOf<VideoItem>()
            if (offset == 0L) {
                val topAdItem =
                    domainManager.getAdRepository().getAD("search_top", adWidth, adHeight)
                        .body()?.content?.get(0)?.ad?.first() ?: AdItem()
                list.add(VideoItem(type = PostType.AD, adItem = topAdItem))
            }
            adIndex = 0
            val adCount = ceil((memberPostItems?.size ?: 0).toFloat() / 5).toInt()
            val adItems =
                domainManager.getAdRepository().getAD("search", adWidth, adHeight, adCount)
                    .body()?.content?.get(0)?.ad ?: arrayListOf()
            memberPostItems?.forEachIndexed { index, item ->
                list.add(item)
                if (index % 5 == 4) list.add(getAdItem(adItems))
            }
            if ((memberPostItems?.size ?: 0) % 5 != 0) list.add(getAdItem(adItems))

            val hasNext = hasNextPage(
                result.body()?.paging?.count ?: 0,
                result.body()?.paging?.offset ?: 0,
                memberPostItems?.size ?: 0
            )
            val nextKey = if (hasNext) offset + PER_LIMIT else null
            pagingCallback.onTotalCount(result.body()?.paging?.count ?: 0)
            LoadResult.Page(list, null, nextKey)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < PER_LIMIT -> false
            offset >= total -> false
            else -> true
        }
    }

    private fun getAdItem(adItems: ArrayList<AdItem>): VideoItem {
        if (adIndex + 1 > adItems.size) adIndex = 0
        val adItem =
            if (adItems.isEmpty()) AdItem()
            else adItems[adIndex]
        adIndex++
        return VideoItem(type = PostType.AD, adItem = adItem)
    }
}