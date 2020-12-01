package com.dabenxiang.mimi.view.club.item

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.observe
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.*
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.club.pic.ClubPicFragment
import com.dabenxiang.mimi.view.club.text.ClubTextFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.player.ui.ClipPlayerFragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_club_short.*
import kotlinx.android.synthetic.main.fragment_club_short.id_empty_group
import kotlinx.android.synthetic.main.fragment_club_short.id_not_login_group
import kotlinx.android.synthetic.main.fragment_club_short.layout_ad
import kotlinx.android.synthetic.main.fragment_club_short.layout_refresh
import kotlinx.android.synthetic.main.item_ad.view.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class ClubItemFragment(val type: ClubTabItemType) : BaseFragment() {
    private val viewModel: ClubItemViewModel by viewModels()
    private val accountManager: AccountManager by inject()

    private val adapter: ClubItemAdapter by lazy {
        ClubItemAdapter(requireContext(), postListener, attachmentListener, memberPostFuncItem)
    }

    override fun getLayoutId() = R.layout.fragment_club_short

    companion object {
        const val KEY_DATA = "data"
        fun createBundle(item: MemberPostItem): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }
    }

    private val memberPostFuncItem by lazy {
        MemberPostFuncItem(
                {},
                { id, view, type -> },
                { item, items, isFollow, func -> },
                { item, isLike, func -> },
                { item, isFavorite, func -> }
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (viewModel.adWidth * 0.142).toInt()

        viewModel.adResult.observe(this) {
            when (it) {
                is ApiResult.Success -> {
                    it.result?.let { item ->
                        Glide.with(requireContext()).load(item.href).into(layout_ad.iv_ad)
                        layout_ad.iv_ad.setOnClickListener {
                            GeneralUtils.openWebView(requireContext(), item.target ?: "")
                        }
                    }
                }
                is ApiResult.Error -> {
                    layout_ad.visibility = View.GONE
                    onApiError(it.throwable)
                }

                else -> {
                    layout_ad.visibility = View.GONE
                    onApiError(Exception("Unknown Error!"))
                }
            }
        }

        viewModel.showProgress.observe(this) {
            layout_refresh.isRefreshing = it
        }

        viewModel.postCount.observe(this) {
            Timber.i("postCount= $it")
            if (it == 0) {
                id_empty_group.visibility = View.VISIBLE
                list_short.visibility = View.INVISIBLE
            } else {
                id_empty_group.visibility = View.GONE
                list_short.visibility = View.VISIBLE
            }
            layout_refresh.isRefreshing = false
        }

        viewModel.followResult.observe(this, Observer {
            when (it) {
                is ApiResult.Empty -> {
                    adapter?.notifyItemRangeChanged(
                            0,
                            viewModel.totalCount,
                            ClubItemAdapter.PAYLOAD_UPDATE_FOLLOW
                    )
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.likePostResult.observe(this, Observer {
            when (it) {
                is ApiResult.Success -> {
                    adapter?.notifyItemChanged(
                            it.result,
                            ClubItemAdapter.PAYLOAD_UPDATE_LIKE
                    )
                }
                is ApiResult.Error -> Timber.e(it.throwable)
            }
        })

        viewModel.favoriteResult.observe(this, Observer {
            when (it) {
                is ApiResult.Success -> {
                    adapter?.notifyItemChanged(
                            it.result,
                            ClubItemAdapter.PAYLOAD_UPDATE_FAVORITE
                    )
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list_short.adapter = adapter

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            viewModel.getData(adapter, type)
        }
    }

    override fun initSettings() {

    }

    private fun loginPageToggle(isLogin: Boolean) {
        if (isLogin) {
            id_not_login_group.visibility = View.GONE
            layout_refresh.visibility = View.VISIBLE
        } else {
            id_not_login_group.visibility = View.VISIBLE
            layout_refresh.visibility = View.INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        //According to specs, this page does not need to log in currently
        loginPageToggle(true)

        if (viewModel.postCount.value ?: -1 <= 0) {
            viewModel.getData(adapter, type)
        }
        viewModel.getAd()
    }


    private val attachmentListener = object : AttachmentListener {
        override fun onGetAttachment(id: Long?, view: ImageView, type: LoadImageType) {
            viewModel.loadImage(id, view, type)
        }

        override fun onGetAttachment(id: String, parentPosition: Int, position: Int) {
        }
    }

    private val postListener = object : MyPostListener {
        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
            checkStatus { viewModel.likePost(item, position, isLike) }
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
            Timber.d("onCommentClick = ${adultTabType}")
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

        override fun onFavoriteClick(item: MemberPostItem, position: Int, isFavorite: Boolean, type: AttachmentType) {
            checkStatus {
                viewModel.favoritePost(item, position, isFavorite)
            }
        }

        override fun onFollowClick(items: List<MemberPostItem>, position: Int, isFollow: Boolean) {

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

        override fun onMoreClick(item: MemberPostItem, position: Int) {
            onMoreClick(item, position) {
                it as MemberPostItem


                val bundle = Bundle()
                item.id
                bundle.putBoolean(MyPostFragment.EDIT, true)
                bundle.putString(BasePostFragment.PAGE, BasePostFragment.TAB)
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
            Timber.i("onItemClick:$item")
            if (!accountManager.isLogin()) {
                loginPageToggle(false)
                return
            }
            Timber.d("onItemClick =${adultTabType}")
            when (adultTabType) {
                AdultTabType.TEXT -> {
                    val bundle = ClubTextFragment.createBundle(item)
                    navigateTo(
                            NavigateItem.Destination(
                                    R.id.action_to_clubTextFragment,
                                    bundle
                            )
                    )
                }
                AdultTabType.PICTURE -> {
                    val bundle = ClubPicFragment.createBundle(item)
                    navigateTo(
                            NavigateItem.Destination(
                                    R.id.action_to_clubPicFragment,
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

        override fun onClipItemClick(item: List<MemberPostItem>, position: Int) {
            Timber.d("onClipItemClick")
        }

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {}

        override fun onChipClick(type: PostType, tag: String) {
            Timber.d("onChipClick")
            val item = SearchPostItem(type, tag = tag)
            val bundle = SearchPostFragment.createBundle(item)
            navigateTo(
                    NavigateItem.Destination(
                            R.id.action_to_searchPostFragment,
                            bundle
                    )
            )
        }
    }
}