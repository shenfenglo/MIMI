package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ArticleItem (
    @SerializedName("text")
    val text: String
)