package com.dabenxiang.mimi.model.enums

import com.google.gson.annotations.SerializedName

enum class LayoutType {
    @SerializedName("1")
    RECOMMEND,

    @SerializedName("2")
    GENERAL,

    @SerializedName("3")
    ACTOR
}