package com.dabenxiang.mimi.view.adapter.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.BaseItemListener
import com.dabenxiang.mimi.model.api.vo.MemberFollowItem
import com.dabenxiang.mimi.model.enums.ClickType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.LoadImageUtils
import kotlinx.android.synthetic.main.item_follow_member.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class MemberFollowViewHolder(
    itemView: View
) : BaseViewHolder(itemView), KoinComponent {
    private val ivPhoto: ImageView = itemView.iv_photo
    private val tvName: TextView = itemView.tv_name
    private val tvSubTitle: TextView = itemView.tv_sub_title
    private val clFollow: ConstraintLayout = itemView.cl_follow

    fun onBind(
        item: MemberFollowItem,
        listener: BaseItemListener,
        viewModelScope: CoroutineScope
    ) {
        tvName.text = item.friendlyName
        tvSubTitle.text = item.friendlyName
        clFollow.setOnClickListener {
            listener.onItemClick(item, ClickType.TYPE_FOLLOW)
        }
        tvName.setOnClickListener {
            listener.onItemClick(item, ClickType.TYPE_ITEM)
        }
        ivPhoto.setOnClickListener {
            listener.onItemClick(item, ClickType.TYPE_ITEM)
        }
        tvSubTitle.setOnClickListener {
            listener.onItemClick(item, ClickType.TYPE_ITEM)
        }
        tvSubTitle.visibility = View.INVISIBLE

        viewModelScope.launch {
            LoadImageUtils.loadImage(item.avatarAttachmentId, ivPhoto, LoadImageType.AVATAR)
        }
    }
}