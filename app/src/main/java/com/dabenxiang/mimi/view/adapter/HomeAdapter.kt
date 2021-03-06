package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.HomeItemType
import com.dabenxiang.mimi.model.vo.CarouselHolderItem
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.club.adapter.ClubFuncItem
import com.dabenxiang.mimi.view.home.HomeTemplate
import com.dabenxiang.mimi.view.home.viewholder.*
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.item_home_statistics.view.*

class HomeAdapter(
    val context: Context,
    private val listener: EventListener,
    private val isAdult: Boolean,
    private val memberPostFuncItem: MemberPostFuncItem,
    private val clubFuncItem: ClubFuncItem
) : ListAdapter<HomeTemplate, BaseViewHolder>(DIFF_CALLBACK) {

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
                    ), listener, isAdult, memberPostFuncItem
                )
            }
            HomeItemType.STATISTICS -> {
                val view = layoutInflater.inflate(
                    R.layout.item_home_statistics,
                    parent,
                    false
                )
                val params = view.recyclerview_statistics.layoutParams as ConstraintLayout.LayoutParams
                if(isAdult) params.height =  (((165.0 / 640.0)) * GeneralUtils.getWindowsHeight()).toInt()
                else params.height = (((190.0 / 640.0)) * GeneralUtils.getWindowsHeight()).toInt()
                HomeStatisticsViewHolder(
                    view, listener, isAdult
                )
            }
            HomeItemType.CLIP -> {
                HomeClipViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_home_clip,
                        parent,
                        false
                    ), listener, isAdult, memberPostFuncItem
                )
            }
            HomeItemType.PICTURE -> {
                HomePictureViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_home_picture,
                        parent,
                        false
                    ), listener, isAdult, memberPostFuncItem
                )
            }
            HomeItemType.CLUB -> {
                HomeClubViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_home_club,
                        parent,
                        false
                    ), listener, isAdult, clubFuncItem
                )
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val template = getItem(position)
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

    interface EventListener {
        fun onHeaderItemClick(view: View, item: HomeTemplate.Header)
        fun onVideoClick(view: View, item: PlayerItem)
        fun onClipClick(view: View, item: List<MemberPostItem>, position: Int)
        fun onPictureClick(view: View, item: MemberPostItem)
        fun onClubClick(view: View, item: MemberClubItem)
        fun onLoadBannerViewHolder(vh: HomeBannerViewHolder)
        fun onLoadStatisticsViewHolder(vh: HomeStatisticsViewHolder, src: HomeTemplate.Statistics)
        fun onLoadCarouselViewHolder(vh: HomeCarouselViewHolder, src: HomeTemplate.Carousel)
        fun onLoadClipViewHolder(vh: HomeClipViewHolder)
        fun onLoadPictureViewHolder(vh: HomePictureViewHolder)
        fun onLoadClubViewHolder(vh: HomeClubViewHolder)
        fun onClickBanner(item: CarouselHolderItem);
    }
}
