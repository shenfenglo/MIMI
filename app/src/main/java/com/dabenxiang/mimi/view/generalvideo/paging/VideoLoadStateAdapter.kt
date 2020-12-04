package com.dabenxiang.mimi.view.generalvideo.paging

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import com.dabenxiang.mimi.view.generalvideo.GeneralVideoAdapter
import com.dabenxiang.mimi.view.pagingfooter.PagingLoadStateViewHolder

class VideoLoadStateAdapter(private val adapter: GeneralVideoAdapter) :
    LoadStateAdapter<PagingLoadStateViewHolder>() {

    override fun onBindViewHolder(holder: PagingLoadStateViewHolder, loadState: LoadState) {
        holder.bindTo(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): PagingLoadStateViewHolder {
        return PagingLoadStateViewHolder(parent) { adapter.retry() }
    }

}
