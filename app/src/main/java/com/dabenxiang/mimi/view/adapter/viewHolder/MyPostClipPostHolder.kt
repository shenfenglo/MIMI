package com.dabenxiang.mimi.view.adapter.viewHolder

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.vo.MediaContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.*
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.GeneralUtils.getSpanString
import com.dabenxiang.mimi.widget.utility.LoadImageUtils
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_clip_post.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.*

class MyPostClipPostHolder(
    itemView: View
) : BaseViewHolder(itemView), KoinComponent {

    private val accountManager: AccountManager by inject()

    private val clClipPost: ConstraintLayout = itemView.cl_clip_post
    private val ivAvatar: ImageView = itemView.img_avatar
    private val tvName: TextView = itemView.tv_name
    private val tvTime: TextView = itemView.tv_time
    private val tvTitle: TextView = itemView.tv_title
    private val tvTitleMore: TextView = itemView.tv_title_more
    private val tvFollow: TextView = itemView.tv_follow
    private val ivPhoto: ImageView = itemView.iv_photo
    private val tvLength: TextView = itemView.tv_length
    private val tagChipGroup: ChipGroup = itemView.chip_group_tag
    private val ivLike: ImageView = itemView.iv_like
    private val tvLikeCount: TextView = itemView.tv_like_count
    private val ivComment: ImageView = itemView.iv_comment
    private val tvCommentCount: TextView = itemView.tv_comment_count
    private val ivMore: ImageView = itemView.iv_more
    private val ivFavorite: ImageView = itemView.iv_favorite
    private val tvFavoriteCount: TextView = itemView.tv_favorite_count
    private val layoutClip: ConstraintLayout = itemView.layout_clip
    private val vSeparator: View = itemView.v_separator
    private val ivAd: ImageView = itemView.iv_ad

    fun onBind(
        item: MemberPostItem,
        position: Int,
        myPostListener: MyPostListener,
        viewModelScope: CoroutineScope,
        searchStr: String = "",
        searchTag: String = "",
        adGap: Int? = null
    ) {
        clClipPost.setBackgroundColor(App.self.getColor(R.color.color_white_1))
        tvName.setTextColor(App.self.getColor(R.color.color_black_1))
        tvTime.setTextColor(App.self.getColor(R.color.color_black_1_50))
        tvTitle.setTextColor(App.self.getColor(R.color.color_black_1))
        tvLikeCount.setTextColor(App.self.getColor(R.color.color_black_1))
        tvFavoriteCount.setTextColor(App.self.getColor(R.color.color_black_1))
        tvCommentCount.setTextColor(App.self.getColor(R.color.color_black_1))
        ivComment.setImageResource(R.drawable.ico_messege_adult_gray)
        ivMore.setImageResource(R.drawable.btn_more_gray_n)
        vSeparator.setBackgroundColor(App.self.getColor(R.color.color_black_1_05))
        if (adGap != null && position % adGap == adGap - 1) {
            ivAd.visibility = View.VISIBLE
            val options = RequestOptions()
                .priority(Priority.NORMAL)
                .placeholder(R.drawable.img_ad_df)
                .error(R.drawable.img_ad)
            Glide.with(ivAd.context)
                .load(item.adItem?.href)
                .apply(options)
                .into(ivAd)
            ivAd.setOnClickListener {
                GeneralUtils.openWebView(ivAd.context, item.adItem?.target ?: "")
            }
        } else {
            ivAd.visibility = View.GONE
        }

        tvName.text = item.postFriendlyName
        tvTime.text = GeneralUtils.getTimeDiff(item.creationDate, Date())
        item.title.let {
            val title = if (searchStr.isNotBlank()) getSpanString(
                tvTitle.context,
                item.title,
                searchStr
            ).toString() else item.title
            tvTitle.text = title

            Timber.i("title size=${tvTitle.text.length}")
            tvTitleMore.visibility = if (tvTitle.text.length >= 45) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        tvFollow.visibility =
            if (accountManager.getProfile().userId == item.creatorId) View.GONE else View.VISIBLE

        viewModelScope.launch {
            LoadImageUtils.loadImage(item.avatarAttachmentId, ivAvatar, LoadImageType.AVATAR)
        }
        ivAvatar.setOnClickListener {
            myPostListener.onAvatarClick(item.creatorId, item.postFriendlyName)
        }

        tagChipGroup.removeAllViews()
        item.tags?.forEach {
            val chip = LayoutInflater.from(tagChipGroup.context)
                .inflate(R.layout.chip_item, tagChipGroup, false) as Chip
            chip.text = it
            if (it == searchTag || it == searchStr) chip.setTextColor(
                tagChipGroup.context.getColor(
                    R.color.color_red_1
                )
            )
            else chip.setTextColor(tagChipGroup.context.getColor(R.color.color_black_1_50))
            chip.setOnClickListener { view ->
                myPostListener.onChipClick(PostType.VIDEO, (view as Chip).text.toString())
            }
            tagChipGroup.addView(chip)
        }

        val contentItem = Gson().fromJson(item.postContent, MediaContentItem::class.java)

        tvLength.text = contentItem.shortVideo?.length
        contentItem.images?.also { images ->
            Timber.i("images $images")
            if (!TextUtils.isEmpty(images[0].url)) {
                Glide.with(ivPhoto.context)
                    .load(images[0].url).into(ivPhoto)
            } else {
                viewModelScope.launch {
                    LoadImageUtils.loadImage(
                        images[0].id.toLongOrNull(),
                        ivPhoto,
                        LoadImageType.PICTURE_EMPTY
                    )
                }
            }
        }

        tvFollow.visibility = View.GONE

        ivMore.setOnClickListener {
            myPostListener.onMoreClick(item, position)
        }

        updateFavorite(item)
        val onFavoriteClickListener = View.OnClickListener {
//            item.isFavorite = !item.isFavorite
//            item.favoriteCount =
//                if (item.isFavorite) item.favoriteCount + 1 else item.favoriteCount - 1
//            updateFavorite(item)
            myPostListener.onFavoriteClick(
                item,
                position,
                !item.isFavorite,
                AttachmentType.ADULT_HOME_CLIP
            )
        }
        ivFavorite.setOnClickListener(onFavoriteClickListener)
        tvFavoriteCount.setOnClickListener(onFavoriteClickListener)

        updateLike(item)
        val onLikeClickListener = View.OnClickListener {
//            item.likeType = if (item.likeType == LikeType.LIKE) null else LikeType.LIKE
//            item.likeCount =
//                if (item.likeType == LikeType.LIKE) item.likeCount + 1 else item.likeCount - 1
//            updateLike(item)
            myPostListener.onLikeClick(item, position, item.likeType != LikeType.LIKE)
        }
        ivLike.setOnClickListener(onLikeClickListener)
        tvLikeCount.setOnClickListener(onLikeClickListener)

        tvCommentCount.text = item.commentCount.toString()
        val onCommentClickListener = View.OnClickListener {
            item.also { myPostListener.onCommentClick(it, AdultTabType.CLIP) }
        }
        ivComment.setOnClickListener(onCommentClickListener)
        tvCommentCount.setOnClickListener(onCommentClickListener)

        layoutClip.setOnClickListener {
            myPostListener.onItemClick(item, AdultTabType.CLIP)
        }
        tvTitleMore.setOnClickListener {
            myPostListener.onItemClick(item, AdultTabType.CLIP)
        }

    }

    fun updateLike(item: MemberPostItem) {
        tvLikeCount.text = item.likeCount.toString()

        if (item.likeType == LikeType.LIKE) {
            ivLike.setImageResource(R.drawable.ico_nice_s)
        } else {
            ivLike.setImageResource(R.drawable.ico_nice_gray)
        }
    }

    fun updateFollow(item: MemberPostItem) {
        tvFollow.setText(if (item.isFollow) R.string.followed else R.string.follow)
        tvFollow.setBackgroundResource(if (item.isFollow) R.drawable.bg_white_1_stroke_radius_16 else R.drawable.bg_red_1_stroke_radius_16)
        tvFollow.setTextColor(App.self.getColor(if (item.isFollow) R.color.color_black_1_60 else R.color.color_red_1))
    }

    fun updateFavorite(item: MemberPostItem) {
        tvFavoriteCount.text = item.favoriteCount.toString()

        if (item.isFavorite) {
            ivFavorite.setImageResource(R.drawable.btn_favorite_white_s)
        } else {
            ivFavorite.setImageResource(R.drawable.btn_favorite_n)
        }
    }

    fun updateInteractive(item: MemberPostItem) {
        updateFavorite(item)
        updateLike(item)
    }
}