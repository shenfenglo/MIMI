package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostType
import com.google.gson.annotations.SerializedName
import java.util.*

data class PostFollowItem(
    @SerializedName("id")
    val id: Long?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("content")
    val content: String?,

    @SerializedName("type")
    val type: PostType = PostType.TEXT,

    @SerializedName("postFriendlyName")
    val postFriendlyName: String = "",

    @SerializedName("creationDate")
    val creationDate: String?,

    @SerializedName("likeType")
    var likeType: LikeType = LikeType.DISLIKE,

    @SerializedName("isFavorite")
    val isFavorite: Boolean? = false,

    @SerializedName("likeCount")
    val likeCount: Int? = 0,

    @SerializedName("dislikeCount")
    val dislikeCount: Int? = 0,

    @SerializedName("favoriteCount")
    val favoriteCount: Int? = 0,

    @SerializedName("commentCount")
    val commentCount: Int? = 0,

    @SerializedName("tags")
    val tags: ArrayList<String>? = arrayListOf(),

    @SerializedName("category")
    val category: String? = "string",

    @SerializedName("clickThroughCount")
    val clickThroughCount: Int? = 0,

    @SerializedName("avatarAttachmentId")
    val avatarAttachmentId: Int? = 0,

    @SerializedName("creatorId")
    val creatorId: Int? = 0,

    @SerializedName("isFollow")
    val isFollow: Boolean? = false,

    @SerializedName("reported")
    val reported: Boolean? = false

) : BaseMemberPostItem()