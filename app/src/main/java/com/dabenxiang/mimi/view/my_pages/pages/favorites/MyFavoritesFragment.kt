package com.dabenxiang.mimi.view.my_pages.pages.favorites

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.club.base.PostItemAdapter
import com.dabenxiang.mimi.view.club.pic.ClubPicFragment
import com.dabenxiang.mimi.view.club.text.ClubTextFragment
import com.dabenxiang.mimi.view.dialog.clean.CleanDialogFragment
import com.dabenxiang.mimi.view.dialog.clean.OnCleanDialogListener
import com.dabenxiang.mimi.view.my_pages.base.MyPagesPostMediator
import com.dabenxiang.mimi.view.my_pages.base.MyPagesType
import com.dabenxiang.mimi.view.my_pages.base.MyPagesViewModel
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.player.ui.ClipPlayerFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_my_collection_favorites.*
import kotlinx.android.synthetic.main.fragment_my_collection_favorites.id_empty_group
import kotlinx.android.synthetic.main.fragment_my_collection_favorites.img_page_empty
import kotlinx.android.synthetic.main.fragment_my_collection_favorites.layout_refresh
import kotlinx.android.synthetic.main.fragment_my_collection_favorites.text_page_empty
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber

class MyFavoritesFragment(
    val tab: Int,
    val myPagesType: MyPagesType,
    val isLike: Boolean = false
) : BaseFragment() {

    private val viewModel: MyFavoritesViewModel by viewModels()
    private val myPagesViewModel: MyPagesViewModel by viewModels({ requireParentFragment() })

    private val adapter: PostItemAdapter by lazy {
        PostItemAdapter(requireActivity(), postListener, viewModel.viewModelScope, adClickListener = adClickListener)
    }

    val pageCode = MyPagesPostMediator::class.simpleName + myPagesType.toString()

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun getLayoutId() = R.layout.fragment_my_collection_favorites

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Timber.i("MyFollowInterestFragment onAttach")
        viewModel.showProgress.observe(this, {
            layout_refresh.isRefreshing = it
        })

        myPagesViewModel.deleteAll.observe(this, {
            if(tab == it){
                viewModel.deleteFavorites(adapter.snapshot().items)
            }
        })

        viewModel.cleanResult.observe(this, {
            when (it) {
                is ApiResult.Empty -> {
                    emptyPageToggle(true)
                    myPagesViewModel.changeDataIsEmpty(tab, true)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.likePostResult.observe(this){
            when (it) {
                is ApiResult.Success -> {
                    adapter.notifyItemChanged(it.result, PostItemAdapter.UPDATE_INTERACTIVE)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        }

        viewModel.favoriteResult.observe(this, {
            when (it) {
                is ApiResult.Success -> {
                    adapter.notifyItemChanged(it.result, PostItemAdapter.UPDATE_INTERACTIVE)
                    if(adapter.snapshot().items.size <=1) {
                        viewModel.viewModelScope.launch {
                            val dbSize=viewModel.checkoutItemsSize(pageCode)
                            if(dbSize<=0) {
                                emptyPageToggle(true)
                                myPagesViewModel.changeDataIsEmpty(tab, true)
                            }
                        }
                    }
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }

        })

        viewModel.adWidth = GeneralUtils.getAdSize(requireActivity()).first
        viewModel.adHeight = GeneralUtils.getAdSize(requireActivity()).second
    }

    override fun onResume() {
        super.onResume()
        if(adapter.snapshot().items.isEmpty()) adapter.refresh()

        else adapter.notifyDataSetChanged()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        @OptIn(ExperimentalCoroutinesApi::class)
        viewModel.viewModelScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                layout_refresh?.isRefreshing = loadStates.refresh is LoadState.Loading
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        viewModel.viewModelScope.launch {
            viewModel.posts(pageCode, myPagesType).flowOn(Dispatchers.IO).collectLatest {
                adapter.submitData(it)
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        viewModel.viewModelScope.launch {
            @OptIn(FlowPreview::class)
            adapter.loadStateFlow
                .distinctUntilChangedBy { it.refresh }
                .filter { it.refresh is LoadState.NotLoading }
                .onEach { delay(1000) }
                .collect {
                    if(adapter.snapshot().items.isEmpty() && timeout > 0) {
                        timeout--
                        adapter.refresh()
                    }
                }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler_view.adapter = adapter

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            adapter.refresh()
        }

        viewModel.postCount.observe(viewLifecycleOwner, {
            emptyPageToggle(it<=0)
            myPagesViewModel.changeDataIsEmpty(tab, it<=0)
        })

        viewModel.showProgress.observe(viewLifecycleOwner,{
            layout_refresh.isRefreshing = it
        })

        text_page_empty.text =
            if (isLike) getString(R.string.like_empty_msg) else getString(R.string.follow_empty_msg)
        img_page_empty.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(), R.drawable.img_love_empty
            )
        )

    }

    private fun emptyPageToggle(isHide:Boolean){
        if (isHide) {
            timeout = 0
            id_empty_group.visibility = View.VISIBLE
            text_page_empty.text = getText(R.string.empty_post)
            recycler_view?.visibility = View.INVISIBLE
        } else {
            id_empty_group.visibility = View.GONE
            recycler_view?.visibility = View.VISIBLE

        }
        layout_refresh.isRefreshing = false

    }

    private val postListener = object : MyPostListener {

        override fun onLoginClick() {

        }

        override fun onRegisterClick() {

        }

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
            checkStatus { viewModel.likePost(item, position, isLike) }
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
            checkStatus {
                when (adultTabType) {
                    AdultTabType.PICTURE -> {
                        val bundle = ClubPicFragment.createBundle(item, 1)
                        navigateTo(
                            NavigateItem.Destination(
                                R.id.action_to_clubPicFragment,
                                bundle
                            )
                        )
                    }
                    AdultTabType.TEXT -> {
                        val bundle = ClubTextFragment.createBundle(item, 1)
                        navigateTo(
                            NavigateItem.Destination(
                                R.id.action_to_clubTextFragment,
                                bundle
                            )
                        )
                    }
                    AdultTabType.CLIP -> {
                        val bundle = ClipPlayerFragment.createBundle(item.id, 1)
                        navigateTo(
                            NavigateItem.Destination(
                                R.id.action_to_clipPlayerFragment,
                                bundle
                            )
                        )
                    }
                }
            }
        }

        override fun onFavoriteClick(
            item: MemberPostItem,
            position: Int,
            isFavorite: Boolean,
            type: AttachmentType
        ) {
            val dialog = CleanDialogFragment.newInstance(object : OnCleanDialogListener {
                override fun onClean() {
                    viewModel.favoritePost(item, position, isFavorite)
                }

                override fun onCancel() {
                    item.isFavorite = true
                    item.favoriteCount++
                    adapter.notifyItemChanged(position)
                }
            })

            dialog.setMsg(
                if (isLike) getString(R.string.like_delete_favorite_message)
                else getString(R.string.follow_delete_favorite_message)
            )

            dialog.show(
                requireActivity().supportFragmentManager,
                CleanDialogFragment::class.java.simpleName
            )
        }

        override fun onFollowClick(items: List<MemberPostItem>, position: Int, isFollow: Boolean) {

        }

        override fun onMoreClick(item: MemberPostItem, position: Int) {
            Timber.i("MoreDialogFragment onMoreClick=$item")
            onMoreClick(item, position) {
                it as MemberPostItem

                val bundle = Bundle()
                item.id
                bundle.putBoolean(MyPostFragment.EDIT, true)
                bundle.putString(BasePostFragment.PAGE, BasePostFragment.FAVORITE)
                bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)

                when (it.type) {
                    PostType.TEXT -> {
                        findNavController().navigate(
                            R.id.action_to_postArticleFragment,
                            bundle
                        )
                    }
                    PostType.IMAGE -> {
                        findNavController().navigate(
                            R.id.action_to_postPicFragment,
                            bundle
                        )
                    }
                    PostType.VIDEO -> {
                        findNavController().navigate(
                            R.id.action_to_postVideoFragment,
                            bundle
                        )
                    }
                }
            }
        }

        override fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType) {

            when (adultTabType) {
                AdultTabType.PICTURE -> {
                    val bundle = ClubPicFragment.createBundle(item, 0)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_to_clubPicFragment,
                            bundle
                        )
                    )
                }
                AdultTabType.TEXT -> {
                    val bundle = ClubTextFragment.createBundle(item, 0)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_to_clubTextFragment,
                            bundle
                        )
                    )
                }
                AdultTabType.CLIP -> {
                    val bundle = ClipPlayerFragment.createBundle(item.id)
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_to_clipPlayerFragment,
                            bundle
                        )
                    )
                }
                else -> {
                }
            }
        }

        override fun onClipItemClick(item: List<MemberPostItem>, position: Int) {}

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {}

        override fun onChipClick(type: PostType, tag: String) {
            val item = SearchPostItem(type = PostType.FOLLOWED, tag = tag)
            val bundle = SearchPostFragment.createBundle(item)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_to_searchPostFragment,
                    bundle
                )
            )
        }

        override fun onAvatarClick(userId: Long, name: String) {
            val bundle = MyPostFragment.createBundle(
                userId, name,
                isAdult = true,
                isAdultTheme = true
            )
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_to_myPostFragment,
                    bundle
                )
            )
        }
    }
}
