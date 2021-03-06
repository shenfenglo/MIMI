package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.home.viewholder.HomeTabHolder

class TopTabAdapter(
    private val listener: BaseIndexViewHolder.IndexViewHolderListener
) :
    BaseTabAdapter<String, HomeTabHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeTabHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tab, parent, false)
        return HomeTabHolder(
            view,
            listener)
    }

    override fun onBindViewHolder(holder: HomeTabHolder, position: Int) {
        holder.bind(tabList?.get(position) ?: "", position)
        holder.setSelected(lastSelected == position)
    }
}