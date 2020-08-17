package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.enums.PostType
import com.google.gson.annotations.SerializedName
import java.util.*

data class VideoItem(

    @SerializedName("availablePoint")
    val availablePoint: Long = 0,

    @SerializedName("categories")
    val categories: List<String> = arrayListOf(),

    @SerializedName("commentCount")
    val commentCount: Long = 0,

    @SerializedName("country")
    val country: String = "",

    @SerializedName("cover")
    val cover: String? = "",

    @SerializedName("deducted")
    val deducted: Boolean? = false,

    @SerializedName("description")
    val description: String? = "",

    @SerializedName("favorite")
    var favorite: Boolean? = false,

    @SerializedName("favoriteCount")
    var favoriteCount: Long? = 0,

    @SerializedName("id")
    val id: Long? = 0,

    @SerializedName("like")
    var like: Boolean? = false,

    @SerializedName("likeCount")
    var likeCount: Long? = 0,

    @SerializedName("point")
    val point: Long? = 0,

    @SerializedName("source")
    val source: String? = "",

    @SerializedName("sources")
    val sources: List<Source>? = arrayListOf(),

    @SerializedName("tags")
    val tags: Any? = null,

    @SerializedName("title")
    val title: String? = "",

    @SerializedName("updateTime")
    val updateTime: Date? = Date(),

    @SerializedName("years")
    val years: Long? = 0,

    var isAdult: Boolean = false,
    var searchingTag: String = "", // 搜尋的 TAG
    var searchingStr: String = "", // 搜尋的 Name

    var type: PostType? = null,
    val adItem: AdItem? = null
)

data class Source(
    @SerializedName("name")
    val name: String? = "",

    @SerializedName("videoEpisodes")
    val videoEpisodes: List<VideoEpisode>? = arrayListOf()
)

data class VideoEpisode(
    @SerializedName("episode")
    val episode: String? = "",

    @SerializedName("id")
    val id: Long? = 0,

    @SerializedName("source")
    val source: String? = "",

    @SerializedName("videoStreams")
    val videoStreams: List<VideoStream>? = arrayListOf()
)

data class VideoStream(
    @SerializedName("episodePublishTime")
    val episodePublishTime: String? = "",

    @SerializedName("id")
    val id: Long? = 0,

    @SerializedName("streamName")
    val streamName: String? = ""
)