package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ReportRequest(
    @SerializedName("content")
    val content: String?,

    @SerializedName("id")
    val id: Long = 0
)