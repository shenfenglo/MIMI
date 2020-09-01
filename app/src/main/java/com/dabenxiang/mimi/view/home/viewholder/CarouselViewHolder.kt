package com.dabenxiang.mimi.view.home.viewholder

import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.model.vo.BannerIdContentItem
import com.dabenxiang.mimi.model.vo.CarouselHolderItem
import com.dabenxiang.mimi.view.adapter.HomeAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import com.google.gson.Gson
import kotlinx.android.synthetic.main.nested_item_carousel.view.*

class CarouselViewHolder(
    itemView: View,
    private val listener: HomeAdapter.EventListener,
    isAdult: Boolean,
    private val memberPostFuncItem: MemberPostFuncItem
) :
    BaseAnyViewHolder<CarouselHolderItem>(itemView) {
    private val ivPoster = itemView.iv_poster

    init {
        ivPoster.setOnClickListener {
            // TODO: 設定影片來源
            listener.onClickBanner(data as CarouselHolderItem)
        }
    }

    override fun updated() {
        data?.also {
            val banner = Gson().fromJson(it.content, BannerIdContentItem::class.java)
            memberPostFuncItem.getImageResource(banner.app) { url ->
                getImage(url)
            }
        }
    }

    private fun getImage(url: GlideUrl) {
        Glide.with(itemView.context).load(url)
            .into(ivPoster)
    }
}