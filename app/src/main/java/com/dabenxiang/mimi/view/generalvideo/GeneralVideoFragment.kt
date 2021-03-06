package com.dabenxiang.mimi.view.generalvideo

import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.GeneratedAdapter
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.category.CategoriesFragment
import com.dabenxiang.mimi.view.generalvideo.GeneralVideoAdapter.Companion.VIEW_TYPE_VIDEO
import com.dabenxiang.mimi.view.pagingfooter.withMimiLoadStateFooter
import com.dabenxiang.mimi.view.player.ui.PlayerV2Fragment
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.view.GridSpaceItemDecoration
import com.dabenxiang.mimi.widget.view.GridSpaceItemWithAdDecoration
import kotlinx.android.synthetic.main.fragment_actor_videos.layout_empty_data
import kotlinx.android.synthetic.main.fragment_actor_videos.layout_refresh
import kotlinx.android.synthetic.main.fragment_actor_videos.rv_video
import kotlinx.android.synthetic.main.fragment_actor_videos.tv_empty_data
import kotlinx.android.synthetic.main.fragment_general_video.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class GeneralVideoFragment(val category: String) : BaseFragment() {

    private val viewModel: GeneralVideoViewModel by viewModels()

    private val videoFuncItem by lazy {
        GeneralVideoFuncItem(
            { source -> viewModel.getDecryptSetting(source) },
            { videoItem, decryptSettingItem, function ->
                viewModel.decryptCover(
                    videoItem,
                    decryptSettingItem,
                    function
                )
            }
        )
    }

    private val generalVideoAdapter by lazy {
        GeneralVideoAdapter(onItemClick, videoFuncItem, adClickListener)
    }

    override fun setupFirstTime() {
        super.setupFirstTime()
        viewModel.adWidth = GeneralUtils.getAdSize(requireActivity()).first
        viewModel.adHeight = GeneralUtils.getAdSize(requireActivity()).second

        rv_video.visibility = View.INVISIBLE

        tv_search.text = String.format(
            getString(R.string.text_search_classification),
            category
        )

        tv_search.setOnClickListener {
            navToSearch()
        }

        tv_filter.setOnClickListener {
            navToCategory()
        }

        layout_refresh.setOnRefreshListener {
            generalVideoAdapter.refresh()
        }

        generalVideoAdapter.addLoadStateListener(loadStateListener)

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
            .also { it.spanSizeLookup = gridLayoutSpanSizeLookup }

        rv_video.also {
            it.layoutManager = gridLayoutManager
            it.adapter = generalVideoAdapter.withMimiLoadStateFooter { generalVideoAdapter.retry() }
            it.setHasFixedSize(true)
            val itemDecoration = GridSpaceItemWithAdDecoration(
                GeneralUtils.dpToPx(requireContext(), 10),
                GeneralUtils.dpToPx(requireContext(), 20)
            )
            it.addItemDecoration(itemDecoration)
        }

        lifecycleScope.launch {
            viewModel.getVideoByCategory(category)
                .collectLatest { generalVideoAdapter.submitData(it) }
        }

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_general_video
    }

    private val onItemClick: (StatisticsItem) -> Unit = {
        navToPlayer(PlayerItem(it.id))
    }

    private val loadStateListener = { loadStatus: CombinedLoadStates ->
        when (loadStatus.refresh) {
            is LoadState.Error -> {
                Timber.e("Refresh Error: ${(loadStatus.refresh as LoadState.Error).error.localizedMessage}")
                onApiError((loadStatus.refresh as LoadState.Error).error)

                layout_empty_data?.run { this.visibility = View.VISIBLE }
                tv_empty_data?.run { this.text = getString(R.string.error_video) }
                rv_video?.run { this.visibility = View.INVISIBLE }
                layout_refresh?.run { this.isRefreshing = false }
            }
            is LoadState.Loading -> {
                layout_empty_data?.run { this.visibility = View.VISIBLE }
                tv_empty_data?.run { this.text = getString(R.string.load_video) }
                rv_video?.run { this.visibility = View.INVISIBLE }
                layout_refresh?.run { this.isRefreshing = true }
            }
            is LoadState.NotLoading -> {
                if (generalVideoAdapter.isDataEmpty()) {
                    layout_empty_data?.run { this.visibility = View.VISIBLE }
                    tv_empty_data?.run { this.text = getString(R.string.empty_video) }
                    rv_video?.run { this.visibility = View.INVISIBLE }
                } else {
                    layout_empty_data?.run { this.visibility = View.INVISIBLE }
                    rv_video?.run { this.visibility = View.VISIBLE }
                }

                layout_refresh?.run { this.isRefreshing = false }
            }
        }

        when (loadStatus.append) {
            is LoadState.Error -> {
                Timber.e("Append Error:${(loadStatus.append as LoadState.Error).error.localizedMessage}")
            }
            is LoadState.Loading -> {
                Timber.d("Append Loading endOfPaginationReached:${(loadStatus.append as LoadState.Loading).endOfPaginationReached}")
            }
            is LoadState.NotLoading -> {
                Timber.d("Append NotLoading endOfPaginationReached:${(loadStatus.append as LoadState.NotLoading).endOfPaginationReached}")
            }
        }
    }

    private val gridLayoutSpanSizeLookup =
        object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position >= generalVideoAdapter.itemCount) 2
                    else when (generalVideoAdapter.getItemViewType(position)) {
                    VIEW_TYPE_VIDEO -> 1
                    else -> 2
                }
            }
        }

    private fun navToCategory(
        orderByType: Int = StatisticsOrderType.LATEST.value
    ) {
        val bundle = CategoriesFragment.createBundle(category, orderByType)
        navigateTo(
            NavigateItem.Destination(
                R.id.action_to_categoriesFragment,
                bundle
            )
        )
    }

    private fun navToSearch() {
        val bundle = SearchVideoFragment.createBundle(category = category)
        navigateTo(
            NavigateItem.Destination(
                R.id.action_to_searchVideoFragment,
                bundle
            )
        )
    }

    private fun navToPlayer(item: PlayerItem) {
        val bundle = PlayerV2Fragment.createBundle(item)
        navigateTo(
            NavigateItem.Destination(
                R.id.action_to_navigation_player,
                bundle
            )
        )
    }
}