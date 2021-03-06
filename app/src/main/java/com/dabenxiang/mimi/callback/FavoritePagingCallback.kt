package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.api.vo.MemberPostItem

interface FavoritePagingCallback : PagingCallback {
    fun onTotalCount(count: Int)
    fun onTotalVideoId(ids: ArrayList<Long>, clear: Boolean)
    fun onReceiveResponse(response: ArrayList<MemberPostItem>)
}