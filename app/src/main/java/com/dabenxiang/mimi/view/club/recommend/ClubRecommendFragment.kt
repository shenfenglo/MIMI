package com.dabenxiang.mimi.view.club.recommend

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.SearchPostItem
import com.dabenxiang.mimi.view.adapter.MyPostPagedAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.clip.ClipFragment
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.dabenxiang.mimi.view.post.BasePostFragment
import com.dabenxiang.mimi.view.search.post.SearchPostFragment
import com.dabenxiang.mimi.view.textdetail.TextDetailFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_club_latest.*
import timber.log.Timber

class ClubRecommendFragment : BaseFragment() {

    private val viewModel: ClubRecommendViewModel by viewModels()
    private var adapter: ClubRecommendAdapter? = null


    override fun getLayoutId() = R.layout.fragment_club_latest

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (viewModel.adWidth * 0.142).toInt()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private val memberPostFuncItem by lazy {
        MemberPostFuncItem(
                {},
                { id, view, type -> },
                { item, items, isFollow, func -> followMember(item, items, isFollow, func) },
                { item, isLike, func -> },
                { item, isFavorite, func -> }
        )
    }

    private val attachmentListener = object : AttachmentListener {
        override fun onGetAttachment(id: Long?, view: ImageView, type: LoadImageType) {
            viewModel.loadImage(id, view, type)
        }

        override fun onGetAttachment(id: String, parentPosition: Int, position: Int) {
        }
    }

    private val postListener = object : MyPostListener {
        override fun onMoreClick(item: MemberPostItem) {
            onMoreClick(item, ArrayList(adapter?.currentList as List<MemberPostItem>), onEdit = {
                it as MemberPostItem
                when (item.type) {
                    PostType.TEXT -> {
                        val bundle = Bundle()
                        item.id
                        bundle.putBoolean(MyPostFragment.EDIT, true)
                        bundle.putString(BasePostFragment.PAGE, BasePostFragment.MY_POST)
                        bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
                        findNavController().navigate(
                                R.id.action_myPostFragment_to_postArticleFragment,
                                bundle
                        )
                    }
                    PostType.IMAGE -> {
                        val bundle = Bundle()
                        bundle.putBoolean(MyPostFragment.EDIT, true)
                        bundle.putString(BasePostFragment.PAGE, BasePostFragment.MY_POST)
                        bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
                        findNavController().navigate(
                                R.id.action_myPostFragment_to_postPicFragment,
                                bundle
                        )
                    }
                    PostType.VIDEO -> {
                        val bundle = Bundle()
                        bundle.putBoolean(MyPostFragment.EDIT, true)
                        bundle.putString(BasePostFragment.PAGE, BasePostFragment.MY_POST)
                        bundle.putSerializable(MyPostFragment.MEMBER_DATA, item)
                        findNavController().navigate(
                                R.id.action_myPostFragment_to_postVideoFragment,
                                bundle
                        )
                    }
                }
            })
        }

        override fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean) {
            checkStatus { viewModel.likePost(item, position, isLike) }
        }

        override fun onClipCommentClick(item: List<MemberPostItem>, position: Int) {
            checkStatus {
                val bundle = ClipFragment.createBundle(ArrayList(mutableListOf(item[position])), 0)
//                navigationToVideo(bundle)
            }
        }

        override fun onClipItemClick(item: List<MemberPostItem>, position: Int) {
            val bundle = ClipFragment.createBundle(ArrayList(mutableListOf(item[position])), 0)
//            navigationToVideo(bundle)
        }

        override fun onChipClick(type: PostType, tag: String) {
            val item = SearchPostItem(type, tag)
            val bundle = SearchPostFragment.createBundle(item)
            navigateTo(
                    NavigateItem.Destination(
                            R.id.action_myPostFragment_to_searchPostFragment,
                            bundle
                    )
            )
        }

        override fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType) {
            when (adultTabType) {
                AdultTabType.PICTURE -> {
                    val bundle = PictureDetailFragment.createBundle(item, 0)
//                    navigationToPicture(bundle)
                }
                AdultTabType.TEXT -> {
                    val bundle = TextDetailFragment.createBundle(item, 0)
//                    navigationToText(bundle)
                }
            }
        }

        override fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType) {
            checkStatus {
                when (adultTabType) {
                    AdultTabType.PICTURE -> {
                        val bundle = PictureDetailFragment.createBundle(item, 1)
//                        navigationToPicture(bundle)
                    }
                    AdultTabType.TEXT -> {
                        val bundle = TextDetailFragment.createBundle(item, 1)
//                        navigationToText(bundle)
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
            checkStatus {
                viewModel.favoritePost(item, position, isFavorite)
            }
        }

        override fun onFollowClick(
                items: List<MemberPostItem>,
                position: Int,
                isFollow: Boolean
        ) {
            checkStatus { viewModel.followPost(ArrayList(items), position, isFollow) }
        }
    }

    override fun initSettings() {
        adapter = ClubRecommendAdapter(requireContext(),
                false,
                postListener,
                attachmentListener,
                memberPostFuncItem)
        recycler_view.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recycler_view.adapter = adapter
    }

    override fun setupFirstTime() {
        initSettings()
        viewModel.getPostItemList()
    }

    override fun setupObservers() {
        viewModel.clubCount.observe(viewLifecycleOwner, Observer {

        })

        viewModel.postItemListResult.observe(viewLifecycleOwner, Observer {
            adapter?.submitList(it)
        })

        viewModel.followResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Empty -> {
                    adapter?.notifyItemRangeChanged(
                            0,
                            viewModel.totalCount,
                            MyPostPagedAdapter.PAYLOAD_UPDATE_FOLLOW
                    )
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.likePostResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    adapter?.notifyItemChanged(
                            it.result,
                            MyPostPagedAdapter.PAYLOAD_UPDATE_LIKE
                    )
                }
                is ApiResult.Error -> Timber.e(it.throwable)
            }
        })

        viewModel.favoriteResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    adapter?.notifyItemChanged(
                            it.result,
                            MyPostPagedAdapter.PAYLOAD_UPDATE_FAVORITE
                    )
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })
    }

    private fun followMember(
            memberPostItem: MemberPostItem,
            items: List<MemberPostItem>,
            isFollow: Boolean,
            update: (Boolean) -> Unit
    ) {
        checkStatus { viewModel.followMember(memberPostItem, ArrayList(items), isFollow, update) }
    }
}