package com.dabenxiang.mimi.view.club.pic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdClickListener
import com.dabenxiang.mimi.callback.ClubPostFuncItem
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.MediaContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.view.adapter.viewHolder.AdHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

class ClubPicDetailAdapter(
    val context: Context,
    private var memberPostItem: MemberPostItem,
    private val onPictureDetailListener: OnPictureDetailListener,
    private val onPhotoGridItemClickListener: ClubPhotoGridAdapter.OnItemClickListener,
    private var mAdItem: AdItem? = null,
    private val clubPostFuncItem: ClubPostFuncItem = ClubPostFuncItem(),
    private val adClickListener: AdClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), KoinComponent {

    companion object {
        const val VIEW_TYPE_PICTURE_DETAIL = 0

        const val VIEW_TYPE_AD = 3
    }

    private val accountManager: AccountManager by inject()

    private var photoGridAdapter: ClubPhotoGridAdapter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val mView: View

        val holder = when (viewType) {
            VIEW_TYPE_AD -> {
                mView = LayoutInflater.from(context)
                    .inflate(R.layout.item_ad, parent, false)
                AdHolder(mView, adClickListener)
            }
            VIEW_TYPE_PICTURE_DETAIL -> {
                mView = LayoutInflater.from(context)
                    .inflate(R.layout.item_club_picture_detail, parent, false)
                PictureDetailViewHolder(mView)
            }
            else -> {
                mView = LayoutInflater.from(context)
                    .inflate(R.layout.item_picture_detail, parent, false)
                PictureDetailViewHolder(mView)
            }
        }

        return holder
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_AD
            1 -> VIEW_TYPE_PICTURE_DETAIL
            else -> VIEW_TYPE_PICTURE_DETAIL
        }
    }

    override fun getItemCount(): Int {
        return 2
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AdHolder -> {
                holder.onBind(mAdItem?: AdItem())
            }
            is PictureDetailViewHolder -> {
                val contentItem =
                    Gson().fromJson(memberPostItem.postContent, MediaContentItem::class.java)

                holder.posterName.text = memberPostItem.postFriendlyName
                holder.posterTime.text =
                    GeneralUtils.getTimeDiff(memberPostItem.creationDate, Date())
                holder.title.text = memberPostItem.title
                holder.txtLikeCount.text = String.format(
                    context.getString(R.string.club_like_count),
                    memberPostItem.likeCount
                )
                holder.txtDisLikeCount.text = String.format(
                    context.getString(R.string.club_dislike_count),
                    memberPostItem.dislikeCount
                )

                if (memberPostItem.likeType == LikeType.LIKE) {
                    holder.imgLike.setImageResource(R.drawable.ico_nice_s)
                } else {
                    holder.imgLike.setImageResource(R.drawable.ico_nice)
                }

                if (memberPostItem.likeType == LikeType.DISLIKE) {
                    holder.imgDislike.setImageResource(R.drawable.ico_bad_s)
                } else {
                    holder.imgDislike.setImageResource(R.drawable.ico_bad)
                }

                if (memberPostItem.isFavorite) {
                    holder.imgFavorite.setImageResource(R.drawable.btn_favorite_white_s)
                } else {
                    holder.imgFavorite.setImageResource(R.drawable.btn_favorite_white_n)
                }

                onPictureDetailListener.onGetAttachment(
                    memberPostItem.avatarAttachmentId,
                    holder.avatarImg,
                    LoadImageType.AVATAR
                )

                if (accountManager.getProfile().userId != memberPostItem.creatorId) {
                    holder.follow.visibility = View.VISIBLE
                    val isFollow = memberPostItem.isFollow
                    if (isFollow) {
                        holder.follow.text = context.getString(R.string.followed)
                        holder.follow.background =
                            context.getDrawable(R.drawable.bg_white_1_stroke_radius_16)
                        holder.follow.setTextColor(context.getColor(R.color.color_black_1_60))
                    } else {
                        holder.follow.text = context.getString(R.string.follow)
                        holder.follow.background =
                            context.getDrawable(R.drawable.bg_red_1_stroke_radius_16)
                        holder.follow.setTextColor(context.getColor(R.color.color_red_1))
                    }
                    holder.follow.setOnClickListener {
                        onPictureDetailListener.onFollowClick(memberPostItem, position, !isFollow)
                    }
                } else {
                    holder.follow.visibility = View.GONE
                }

                holder.photoGrid.layoutManager = when (contentItem.images?.size) {
                    1 -> GridLayoutManager(context, 1)
                    2 -> GridLayoutManager(context, 2)
                    else -> GridLayoutManager(context, 3)
                }
                photoGridAdapter =
                    ClubPhotoGridAdapter(
                        context,
                        contentItem.images ?: arrayListOf(),
                        onPictureDetailListener,
                        onPhotoGridItemClickListener
                    )
                holder.photoGrid.adapter = photoGridAdapter

                holder.tagChipGroup.removeAllViews()
                memberPostItem.tags?.forEach {
                    val chip = LayoutInflater.from(holder.tagChipGroup.context)
                        .inflate(R.layout.chip_item, holder.tagChipGroup, false) as Chip
                    chip.text = it
                    chip.setTextColor(context.getColor(R.color.color_black_1_60))
                    chip.setOnClickListener { view ->
                        onPictureDetailListener.onChipClick(
                            PostType.IMAGE,
                            (view as Chip).text.toString()
                        )
                    }
                    holder.tagChipGroup.addView(chip)
                }

                holder.avatarImg.setOnClickListener {
                    onPictureDetailListener.onAvatarClick(
                        memberPostItem.creatorId,
                        memberPostItem.postFriendlyName
                    )
                }

                holder.imgLike.setOnClickListener {
                    clubPostFuncItem.onLikeClick(
                        memberPostItem,
                        LikeType.LIKE,
                        memberPostItem.likeType
                    ) { item -> updateLike(item, holder) }
                }
                holder.imgDislike.setOnClickListener {
                    clubPostFuncItem.onLikeClick(
                        memberPostItem,
                        LikeType.DISLIKE,
                        memberPostItem.likeType
                    ) { item -> updateLike(item, holder) }
                }
                holder.imgFavorite.setOnClickListener {
                    val isFavorite = memberPostItem.isFavorite
                    clubPostFuncItem.onFavoriteClick(
                        memberPostItem,
                        !isFavorite
                    ) { favorite, count ->
                        updateFavorite(favorite, count, holder)
                    }
                }
                holder.imgReport.setOnClickListener {
                    onPictureDetailListener.onMoreClick(memberPostItem!!)
                }
                holder.imgShare.setOnClickListener {
                }

            }
        }
    }

    fun updatePhotoGridItem(position: Int) {
        photoGridAdapter?.notifyItemChanged(position)
    }

    fun updateContent(item: MemberPostItem) {
        memberPostItem = item
        notifyItemChanged(1)
    }


    fun setupAdItem(item: AdItem) {
        mAdItem = item
    }

    private fun updateLike(item: MemberPostItem, holder: PictureDetailViewHolder) {
        if (item.likeType == LikeType.LIKE) {
            holder.imgLike.setImageResource(R.drawable.ico_nice_s)
            holder.imgDislike.setImageResource(R.drawable.ico_bad)
        } else if (item.likeType == LikeType.DISLIKE) {
            holder.imgDislike.setImageResource(R.drawable.ico_bad_s)
            holder.imgLike.setImageResource(R.drawable.ico_nice)
        } else {
            holder.imgLike.setImageResource(R.drawable.ico_nice)
            holder.imgDislike.setImageResource(R.drawable.ico_bad)
        }
        holder.txtLikeCount.text =
            String.format(context.getString(R.string.club_like_count), item.likeCount)
        holder.txtDisLikeCount.text =
            String.format(context.getString(R.string.club_dislike_count), item.dislikeCount)
    }

    private fun updateFavorite(isFavorite: Boolean, count: Int, holder: PictureDetailViewHolder) {
        if (isFavorite) {
            holder.imgFavorite.setImageResource(R.drawable.btn_favorite_white_s)
        } else {
            holder.imgFavorite.setImageResource(R.drawable.btn_favorite_white_n)
        }
    }

    interface OnPictureDetailListener {
        fun onGetAttachment(id: Long?, view: ImageView, type: LoadImageType)
        fun onFollowClick(item: MemberPostItem, position: Int, isFollow: Boolean)
        fun onMoreClick(item: MemberPostItem)
        fun onChipClick(type: PostType, tag: String)
        fun onOpenWebView(url: String)
        fun onAvatarClick(userId: Long, name: String)
    }
}