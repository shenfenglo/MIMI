package com.dabenxiang.mimi.view.club.post

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_comment_content.view.*

class CommentContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val commentRecycler: RecyclerView = itemView.recycler_comment
    val noCommentLayout: View = itemView.layout_no_comment
}
