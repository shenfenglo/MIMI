package com.dabenxiang.mimi.view.search.post

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.adapter.viewHolder.*
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.search.video.SearchVideoAdapter
import kotlinx.coroutines.CoroutineScope

class SearchPostAdapter(
    val context: Context,
    private val myPostListener: MyPostListener,
    private val viewModelScope: CoroutineScope,
    private val getSearchText: () -> String,
    private val getSearchTag: () -> String
) : PagingDataAdapter<MemberPostItem, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        const val UPDATE_LIKE = 0
        const val UPDATE_FAVORITE = 1
        const val UPDATE_INTERACTIVE = 2

        const val VIEW_TYPE_CLIP = 0
        const val VIEW_TYPE_PICTURE = 1
        const val VIEW_TYPE_TEXT = 2
        const val VIEW_TYPE_AD = 4

        private val diffCallback = object : DiffUtil.ItemCallback<MemberPostItem>() {
            override fun areItemsTheSame(
                oldItem: MemberPostItem,
                newItem: MemberPostItem
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: MemberPostItem,
                newItem: MemberPostItem
            ): Boolean = oldItem == newItem

            override fun getChangePayload(oldItem: MemberPostItem, newItem: MemberPostItem): Any? {
                return when {
                    oldItem.likeType != newItem.likeType || oldItem.likeCount != newItem.likeCount || oldItem.isFavorite != newItem.isFavorite || oldItem.favoriteCount != newItem.favoriteCount -> UPDATE_INTERACTIVE
                    else -> null
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when (item?.type) {
            PostType.VIDEO -> VIEW_TYPE_CLIP
            PostType.IMAGE -> VIEW_TYPE_PICTURE
            PostType.AD -> VIEW_TYPE_AD
            else -> VIEW_TYPE_TEXT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            VIEW_TYPE_AD -> {
                AdHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_ad, parent, false)
                )
            }
            VIEW_TYPE_CLIP -> {
                MyPostClipPostHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_clip_post, parent, false)
                )
            }
            VIEW_TYPE_PICTURE -> {
                MyPostPicturePostHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_picture_post, parent, false)
                )
            }
            VIEW_TYPE_TEXT -> {
                MyPostTextPostHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_text_post, parent, false)
                )
            }
            else -> {
                DeletedItemViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_deleted, parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val item = getItem(position)
        item?.also {
            when (holder) {
                is AdHolder -> {
                    holder.onBind(item.adItem ?: AdItem())
                }
                is MyPostClipPostHolder -> {
                    if (payloads.size == 1) {
                        when (payloads[0]) {
                            UPDATE_LIKE -> holder.updateLike(item)
                            UPDATE_FAVORITE -> holder.updateFavorite(item)
                            UPDATE_INTERACTIVE -> holder.updateInteractive(item)
                        }
                    } else {
                        holder.onBind(
                            it,
                            position,
                            myPostListener,
                            viewModelScope,
                            getSearchText.invoke(),
                            getSearchTag.invoke(),
                            SearchPostMediator.AD_GAP
                        )
                    }
                }
                is MyPostPicturePostHolder -> {
                    if (payloads.size == 1) {
                        when (payloads[0]) {
                            UPDATE_LIKE -> holder.updateLike(item)
                            UPDATE_FAVORITE -> holder.updateFavorite(item)
                            UPDATE_INTERACTIVE -> holder.updateInteractive(item)
                        }
                    } else {
                        holder.pictureRecycler.tag = position
                        holder.onBind(
                            it,
                            position,
                            myPostListener,
                            viewModelScope,
                            getSearchText.invoke(),
                            getSearchTag.invoke(),
                            SearchPostMediator.AD_GAP
                        )
                    }
                }
                is MyPostTextPostHolder -> {
                    if (payloads.size == 1) {
                        when (payloads[0]) {
                            UPDATE_LIKE -> holder.updateLike(item)
                            UPDATE_FAVORITE -> holder.updateFavorite(item)
                            UPDATE_INTERACTIVE -> holder.updateInteractive(item)
                        }
                    } else {
                        holder.onBind(
                            it,
                            position,
                            myPostListener,
                            viewModelScope,
                            getSearchText.invoke(),
                            getSearchTag.invoke(),
                            SearchPostMediator.AD_GAP
                        )
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

}