package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.HomeItemType
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.home.HomeTemplate
import com.dabenxiang.mimi.view.home.viewholder.*

class HomeAdapter(
    val context: Context,
    private val listener: EventListener,
    private val isAdult: Boolean,
    private val clubListener: HomeClubAdapter.ClubListener,
    private val attachmentListener: AttachmentListener,
    private val attachmentMap: HashMap<Long, Bitmap>
) :
    ListAdapter<HomeTemplate, BaseViewHolder>(DIFF_CALLBACK) {

    val homeViewHolderMap: HashMap<HomeItemType, BaseViewHolder> = hashMapOf()

    companion object {
        private val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<HomeTemplate>() {
                override fun areItemsTheSame(
                    oldItem: HomeTemplate,
                    newItem: HomeTemplate
                ): Boolean {
                    return oldItem.type == newItem.type
                }

                override fun areContentsTheSame(
                    oldItem: HomeTemplate,
                    newItem: HomeTemplate
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }

    interface EventListener {
        fun onHeaderItemClick(view: View, item: HomeTemplate.Header)
        fun onVideoClick(view: View, item: PlayerData)
        fun onClipClick(view: View, item: List<MemberPostItem>)
        fun onPictureClick(view: View, item: MemberPostItem)
        fun onClubClick(view: View, item: MemberClubItem)
        fun onLoadStatisticsViewHolder(vh: HomeStatisticsViewHolder, src: HomeTemplate.Statistics)
        fun onLoadCarouselViewHolder(vh: HomeCarouselViewHolder, src: HomeTemplate.Carousel)
        fun onLoadClipViewHolder(vh: HomeClipViewHolder)
        fun onLoadPictureViewHolder(vh: HomePictureViewHolder)
        fun onLoadClubViewHolder(vh: HomeClubViewHolder)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (getItem(viewType).type) {
            HomeItemType.HEADER -> {
                HeaderViewHolder(
                    layoutInflater.inflate(R.layout.item_header, parent, false),
                    listener,
                    isAdult
                )
            }
            HomeItemType.BANNER -> {
                HomeBannerViewHolder(
                    layoutInflater.inflate(R.layout.item_banner, parent, false),
                    listener,
                    isAdult
                )
            }
            HomeItemType.CAROUSEL -> {
                HomeCarouselViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_carousel,
                        parent,
                        false
                    ), listener, isAdult
                )
            }
            HomeItemType.STATISTICS -> {
                HomeStatisticsViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_home_statistics,
                        parent,
                        false
                    ), listener, isAdult
                )
            }
            HomeItemType.CLIP -> {
                HomeClipViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_home_clip,
                        parent,
                        false
                    ), listener, isAdult, attachmentListener, attachmentMap
                )
            }
            HomeItemType.PICTURE -> {
                HomePictureViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_home_picture,
                        parent,
                        false
                    ), listener, isAdult, attachmentListener, attachmentMap
                )
            }
            HomeItemType.CLUB -> {
                HomeClubViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_home_club,
                        parent,
                        false
                    ), listener, isAdult, clubListener, attachmentListener, attachmentMap
                )
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val template = getItem(position)
        homeViewHolderMap[template.type] = holder

        when (template.type) {
            HomeItemType.HEADER -> {
                holder as HeaderViewHolder
                holder.bind(template)
            }
            HomeItemType.BANNER -> {
                holder as HomeBannerViewHolder
                holder.bind(template)
            }
            HomeItemType.CAROUSEL -> {
                holder as HomeCarouselViewHolder
                holder.bind(template)
            }
            HomeItemType.STATISTICS -> {
                holder as HomeStatisticsViewHolder
                holder.bind(template)
            }
            HomeItemType.CLIP -> {
                holder as HomeClipViewHolder
                holder.bind(template)
            }
            HomeItemType.PICTURE -> {
                holder as HomePictureViewHolder
                holder.bind(template)
            }
            HomeItemType.CLUB -> {
                holder as HomeClubViewHolder
                holder.bind(template)
            }
        }
    }

    interface AttachmentListener {
        fun onGetAttachment(id: Long, position: Int, type: HomeItemType)
    }

}
