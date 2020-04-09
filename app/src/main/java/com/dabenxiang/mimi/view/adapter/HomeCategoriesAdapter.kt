package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.home.HomeTemplate
import com.dabenxiang.mimi.view.home.VideoViewHolder

class HomeCategoriesAdapter(private val nestedListener: HomeAdapter.EventListener) : RecyclerView.Adapter<VideoViewHolder>() {

    private var data: HomeTemplate.Categories? = null

    fun setDataSrc(src: HomeTemplate.Categories) {
        data = src

        updated()
    }

    fun updated() {

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.nested_item_home_categories, parent, false)
        return VideoViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 10
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.tvResolution.text = "720P"
        holder.tvInfo.text = "全30集"
        holder.tvTitle.text =
            "標題${position}標題${position}標題${position}標題${position}標題${position}標題${position}標題${position}標題${position}標題${position}標題${position}標題${position}標題${position}"
    }
}