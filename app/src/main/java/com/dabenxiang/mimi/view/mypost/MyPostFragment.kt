package com.dabenxiang.mimi.view.mypost

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Success
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.club.base.PostItemAdapter
import com.dabenxiang.mimi.view.club.pic.ClubPicFragment
import com.dabenxiang.mimi.view.club.text.ClubTextFragment
import com.dabenxiang.mimi.view.login.LoginFragment
import com.dabenxiang.mimi.view.mypost.MyPostViewModel.Companion.USER_ID_ME
import com.dabenxiang.mimi.view.pagingfooter.withMimiLoadStateFooter
import com.dabenxiang.mimi.view.player.ui.ClipPlayerFragment
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.MY_POST
import com.dabenxiang.mimi.view.post.BasePostFragment.Companion.PAGE
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import kotlinx.android.synthetic.main.fragment_club_item.*
import kotlinx.android.synthetic.main.fragment_my_collection_videos.*
import kotlinx.android.synthetic.main.fragment_my_post.*
import kotlinx.android.synthetic.main.fragment_my_post.layout_refresh
import kotlinx.android.synthetic.main.item_follow_no_data.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber


class MyPostFragment : BaseFragment() {

    private val adapter: PostItemAdapter by lazy {
        PostItemAdapter(
                requireContext(),
                myPostListener,
                viewModel.viewModelScope,
            adClickListener = adClickListener
        )
    }

    private val viewModel: MyPostViewModel by viewModels()

    private var userId: Long = USER_ID_ME
    private var userName: String = ""
    private var isAdult: Boolean = true

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    companion object {
        const val EDIT = "edit"
        const val MEMBER_DATA = "member_data"
        const val TYPE_PIC = "type_pic"
        const val IS_NEED_REFRESH = "is_need_refresh"

        private const val KEY_USER_ID = "KEY_USER_ID"
        private const val KEY_USER_NAME = "KEY_USER_NAME"
        private const val KEY_IS_ADULT = "KEY_IS_ADULT"
        private const val KEY_IS_ADULT_THEME = "KEY_IS_ADULT_THEME"

        fun createBundle(
            userId: Long,
            userName: String,
            isAdult: Boolean,
            isAdultTheme: Boolean
        ): Bundle {
            return Bundle().also {
                it.putLong(KEY_USER_ID, userId)
                it.putString(KEY_USER_NAME, userName)
                it.putBoolean(KEY_IS_ADULT, isAdult)
                it.putBoolean(KEY_IS_ADULT_THEME, isAdultTheme)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(adapter.snapshot().items.isEmpty()) adapter.refresh()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_my_post
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.let {
            userId = it.getLong(KEY_USER_ID, USER_ID_ME)
            userName = it.getString(KEY_USER_NAME, "")
            isAdult = it.getBoolean(KEY_IS_ADULT, true)
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        @OptIn(ExperimentalCoroutinesApi::class)
        viewModel.viewModelScope.launch {
            adapter.loadStateFlow.collectLatest { loadStatus ->
                when (loadStatus.refresh) {
                    is LoadState.Error -> {
                        Timber.e("Refresh Error: ${(loadStatus.refresh as LoadState.Error).error.localizedMessage}")
                        onApiError((loadStatus.refresh as LoadState.Error).error)

                        v_no_data?.run { this.visibility = View.VISIBLE }
                        recyclerView?.run { this.visibility = View.INVISIBLE }
                        layout_refresh?.run { this.isRefreshing = false }
                    }
                    is LoadState.Loading -> {
                        v_no_data?.run { this.visibility = View.INVISIBLE }
                        recyclerView?.run { this.visibility = View.INVISIBLE }
                        layout_refresh?.run { this.isRefreshing = true }
                    }
                    is LoadState.NotLoading -> {
                        if (adapter.itemCount == 0) {
                            v_no_data?.run { this.visibility = View.VISIBLE }
                            recyclerView?.run { this.visibility = View.INVISIBLE }
                        } else {
                            v_no_data?.run { this.visibility = View.INVISIBLE }
                            recyclerView?.run { this.visibility = View.VISIBLE }
                        }

                        layout_refresh?.run { this.isRefreshing = false }
                    }
                }
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        viewModel.viewModelScope.launch {
            viewModel.posts(userId).flowOn(Dispatchers.IO).collectLatest {
                adapter.submitData(it)
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()

    }

    override fun initSettings() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter.withMimiLoadStateFooter { adapter.retry() }

        tv_title.text = if (userId == USER_ID_ME) getString(R.string.personal_my_post) else userName
        iv_icon.setImageResource(R.drawable.img_conment_empty)
        tv_text.text = getString(R.string.my_post_no_data)
    }

    override fun navigationToText(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_myPostFragment_to_clubTextFragment,
                bundle
            )
        )
    }

    override fun navigationToPicture(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_myPostFragment_to_clubPicFragment,
                bundle
            )
        )
    }

    private fun navigationToVideo(bundle: Bundle) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_myPostFragment_to_clipPlayerFragment,
                bundle
            )
        )
    }

    override fun setupListeners() {
        View.OnClickListener { btnView ->
            when (btnView.id) {
                R.id.tv_back -> {
                    if (mainViewModel?.isFromPlayer == true)
                        activity?.onBackPressed()
                    else navigateTo(NavigateItem.Up)
                }
            }
        }.also {
            tv_back.setOnClickListener(it)
        }

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            adapter.refresh()
        }
    }

    private val myPostListener = object : MyPostListener {
        override fun onLoginClick() {
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_to_loginFragment,
                    LoginFragment.createBundle(LoginFragment.TYPE_LOGIN)
                )
            )
        }

        override fun onRegisterClick() {
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_to_loginFragment,
                    LoginFragment.createBundle(LoginFragment.TYPE_REGISTER)
                )
            )
        }

        override fun onMoreClick(item: MemberPostItem, position: Int) {
            onMoreClick(item, position, isFromPostPage = true) {
                it as MemberPostItem
                when (item.type) {
                    PostType.TEXT -> {
                        val bundle = Bundle()
                        bundle.putBoolean(EDIT, true)
                        bundle.putString(PAGE, MY_POST)
                        bundle.putSerializable(MEMBER_DATA, item)
                        findNavController().navigate(
                            R.id.action_myPostFragment_to_postArticleFragment,
                            bundle
                        )
                    }
                    PostType.IMAGE -> {
                        val bundle = Bundle()
                        bundle.putBoolean(EDIT, true)
                        bundle.putString(PAGE, MY_POST)
                        bundle.putSerializable(MEMBER_DATA, item)
                        findNavController().navigate(
                            R.id.action_myPostFragment_to_postPicFragment,
                            bundle
                        )
                    }
                    PostType.VIDEO -> {
                        val bundle = Bundle()
                        bundle.putBoolean(EDIT, true)
                        bundle.putString(PAGE, MY_POST)
                        bundle.putSerializable(MEMBER_DATA, item)
                        findNavController().navigate(
                            R.id.action_myPostFragment_to_postVideoFragment,
                            bundle
                        )
                    }
                }
            }
        }

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
            checkStatus { viewModel.likePost(item, position, isLike) }
        }

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {
            checkStatus {
                val bundle = ClipFragment.createBundle(ArrayList(mutableListOf(item[position])), 0)
                navigationToVideo(bundle)
            }
        }

        override fun onClipItemClick(item: List<MemberPostItem>, position: Int) {
            checkStatus {
                val bundle = ClipFragment.createBundle(ArrayList(mutableListOf(item[position])), 0)
                navigationToVideo(bundle)
            }
        }

        override fun onChipClick(type: PostType, tag: String) {
            val item = SearchPostItem(type = PostType.TEXT_IMAGE_VIDEO, tag = tag)
            val bundle = SearchPostFragment.createBundle(item)
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_myPostFragment_to_searchPostFragment,
                    bundle
                )
            )
        }

        override fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType) {
            checkStatus {
                when (adultTabType) {
                    AdultTabType.PICTURE -> {
                        val bundle = ClubPicFragment.createBundle(item)
                        navigationToPicture(bundle)
                    }
                    AdultTabType.TEXT -> {
                        val bundle = ClubTextFragment.createBundle(item)
                        navigationToText(bundle)
                    }
                    AdultTabType.CLIP -> {
                        val bundle = ClipPlayerFragment.createBundle(item.id)
                        navigationToVideo(bundle)
                    }
                }
            }
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
            checkStatus {
                when (adultTabType) {
                    AdultTabType.PICTURE -> {
                        val bundle = ClubPicFragment.createBundle(item, 1)
                        navigationToPicture(bundle)
                    }
                    AdultTabType.TEXT -> {
                        val bundle = ClubTextFragment.createBundle(item, 1)
                        navigationToText(bundle)
                    }
                    AdultTabType.CLIP -> {
                        val bundle = ClipPlayerFragment.createBundle(item.id, 1)
                        navigationToVideo(bundle)
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
            checkStatus { viewModel.favoritePost(item, position, isFavorite) }
        }

        override fun onFollowClick(
            items: List<MemberPostItem>,
            position: Int,
            isFollow: Boolean
        ) {
        }

        override fun onAvatarClick(userId: Long, name: String) {
        }
    }

}