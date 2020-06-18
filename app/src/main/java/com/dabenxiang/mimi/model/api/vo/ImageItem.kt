package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ImageItem (
    @SerializedName("id")
    val id: String?,

    @SerializedName("ext")
    val ext: String?
)