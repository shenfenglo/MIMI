package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.adapter.viewHolder.ClubFollowViewHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.ClubLikeViewHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.DeletedItemViewHolder

class ClubLikeAdapter(
    val listener: EventListener
) : PagingDataAdapter<MemberPostItem, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private const val VIEW_TYPE_NORMAL = 0
        private const val VIEW_TYPE_DELETED = 1

        private val diffCallback = object : DiffUtil.ItemCallback<MemberPostItem>() {
            override fun areItemsTheSame(
                oldItem: MemberPostItem,
                newItem: MemberPostItem
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: MemberPostItem,
                newItem: MemberPostItem
            ): Boolean = oldItem == newItem
        }
    }

    interface EventListener {
        fun onDetail(item: MemberPostItem)
        fun onGetAttachment(id: Long, view:ImageView)
        fun onCancelFollow(clubId: Long, position: Int)
    }

    var removedPosList = ArrayList<Int>()

    override fun getItemViewType(position: Int): Int {
        return if (removedPosList.contains(position)) {
            VIEW_TYPE_DELETED
        } else {
            VIEW_TYPE_NORMAL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_NORMAL -> ClubLikeViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_clip_post, parent, false), listener
            )
            else -> DeletedItemViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_deleted, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is ClubLikeViewHolder -> holder.bind(item, position)
        }
    }

    fun update(position: Int) {
        notifyItemChanged(position)
    }
}