package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostType
import com.google.gson.annotations.SerializedName
import java.util.*

data class PostFavoriteItem(
    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("postId")
    val postId: Long = 0,

    @SerializedName("posterId")
    val posterId: Long = 0,

    @SerializedName("posterName")
    val posterName: String = "",

    @SerializedName("posterAvatarAttachmentId")
    val posterAvatarAttachmentId: Long = 0,

    @SerializedName("postDate")
    val postDate: Date = Date(),

    @SerializedName("type")
    val type: Int = 0,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("content")
    val content: String = "",

    @SerializedName("tag")
    val tag: String = "",

    @SerializedName("tags")
    val tags: ArrayList<String>? = arrayListOf(),

    @SerializedName("category")
    val category: String = "",

    @SerializedName("likeCount")
    var likeCount: Int = 0,

    @SerializedName("dislikeCount")
    var dislikeCount: Int = 0,

    @SerializedName("favoriteCount")
    val favoriteCount: Int = 0,

    @SerializedName("isFollow")
    var isFollow: Boolean = false,

    @SerializedName("reported")
    var reported: Boolean = false,
    // 0 = like, 1= dislike
    @SerializedName("likeType")
    var likeType: Int? = null,

    @SerializedName("commentCount")
    val commentCount: Int = 0,

    @SerializedName("cover")
    var cover: String = "",

    @SerializedName("isFavorite")
    var isFavorite: Boolean = true,

    var position: Int = -1

) : BaseItem() {
    fun toMemberPostItem(): MemberPostItem {
        return MemberPostItem(
            id = postId,
            postId = id,
            title = title,
            postContent = content,
            type = PostType.getTypeByValue(type),
            creationDate = postDate,
            isFavorite = isFavorite,
            reported = reported,
            likeCount = likeCount,
            likeType = LikeType.getByValue(likeType),
            dislikeCount = dislikeCount,
            favoriteCount = favoriteCount,
            commentCount = commentCount,
            tags = tags,
            avatarAttachmentId = posterAvatarAttachmentId,
            creatorId = posterId,
            isFollow = isFollow,
            postFriendlyName = posterName,
            cover = cover
        )
    }

    fun toPlayItem(): PlayItem {
        return PlayItem(
            videoId = postId,
            title = title,
            favorite = isFavorite,
            likeCount = likeCount,
            favoriteCount = favoriteCount,
            commentCount = commentCount,
            tags = tags,
            like = likeType == 0,
            cover = cover,
        )
    }
}