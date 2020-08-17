package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.vo.BaseVideoItem
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.home.viewholder.VideoViewHolder

class HomeStatisticsAdapter(
    private val nestedListener: HomeAdapter.EventListener,
    private val isAdult: Boolean
) :
    RecyclerView.Adapter<BaseViewHolder>() {

    private var list: List<BaseVideoItem.Video>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.nested_item_home_statistics, parent, false)
        return VideoViewHolder(view, videoViewHolderListener)
    }

    override fun getItemCount(): Int {
        val count = list?.count()
        return when {
            count == null -> 0
            count < 2 -> count
            else -> Int.MAX_VALUE
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder as VideoViewHolder

        var resetSuccess = false

        val realPosition = getRealPosition(position)

        list?.also { data ->
            val item = data[realPosition]
            holder.bind(item, realPosition)
            resetSuccess = true
        }

        if (!resetSuccess) {
            holder.bind(null, -1)
        }
    }

    fun submitList(submit: List<BaseVideoItem.Video>?) {
        list = submit
        notifyDataSetChanged()
    }

    private fun getRealPosition(position: Int): Int {
        val count = list?.count()!!
        return when {
            count == 0 -> 0
            position > count - 1 -> position % count
            else -> position
        }
    }

    private val videoViewHolderListener by lazy {
        object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                if (index > -1) {
                    list?.get(index)?.also {
                        nestedListener.onVideoClick(view, PlayerItem.parser(it, isAdult))
                    }
                }
            }
        }
    }

}