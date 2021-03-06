package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class PostStatisticsItem(

    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("type")
    val type: Int = 0,

    @SerializedName("cover")
    val cover: String = "",

    @SerializedName("content")
    val content: String = "",

    @SerializedName("source")
    val source: String = "",

    @SerializedName("avatarAttachmentId")
    val avatarAttachmentId: Long = 0,

    @SerializedName("count")
    val count: Int = 0,

    @SerializedName("creatorId")
    val creatorId: Long = 0,

    var detail: MemberPostItem? = null
)