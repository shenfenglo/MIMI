package com.dabenxiang.mimi.view.adapter.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.BaseItemListener
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.model.enums.ClickType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.LoadImageUtils
import kotlinx.android.synthetic.main.item_follow_club.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class ClubFollowViewHolder(
        itemView: View
) : BaseViewHolder(itemView), KoinComponent {
    private val ivPhoto: ImageView = itemView.iv_photo
    private val tvName: TextView = itemView.tv_name
    private val tvSubTitle: TextView = itemView.tv_sub_title
    private val tvClubFollow: TextView = itemView.tv_club_follow
    private val tvClubPost: TextView = itemView.tv_club_post
    private val clFollow: ConstraintLayout = itemView.cl_follow

    fun onBind(
            item: ClubFollowItem,
            listener: BaseItemListener,
            viewModelScope: CoroutineScope
    )
    {
        tvName.text = item.name ?: ""
        tvSubTitle.text = item.description
        tvClubFollow.text = item.followerCount.toString()
        tvClubPost.text = item.postCount.toString()
        clFollow.setOnClickListener {
            listener.onItemClick(item, ClickType.TYPE_FOLLOW)
        }
        tvName.setOnClickListener {
            listener.onItemClick(item, ClickType.TYPE_ITEM)
        }
        tvClubPost.setOnClickListener {
            listener.onItemClick(item, ClickType.TYPE_ITEM)
        }
        tvSubTitle.setOnClickListener {
            listener.onItemClick(item, ClickType.TYPE_ITEM)
        }
        ivPhoto.setOnClickListener {
            listener.onItemClick(item, ClickType.TYPE_ITEM)
        }
        viewModelScope.launch {
            LoadImageUtils.loadImage(item.avatarAttachmentId, ivPhoto, LoadImageType.AVATAR)
        }
    }
}