package com.dabenxiang.mimi.view.player

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.adapter.BaseTabAdapter
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder

class SelectEpisodeAdapter(
    private val listener: BaseIndexViewHolder.IndexViewHolderListener
) :
    BaseTabAdapter<String, SelectEpisodeHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectEpisodeHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_episode, parent, false)
        return SelectEpisodeHolder(view, listener)
    }

    override fun onBindViewHolder(holder: SelectEpisodeHolder, position: Int) {
        holder.bind(tabList?.get(position) ?: "", position)
        holder.setSelected(lastSelected == position)
    }
}