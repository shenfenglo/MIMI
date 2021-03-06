package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

class BalanceItem (
    @SerializedName("allCount")
    val allCount: Long?,

    @SerializedName("merchant2UserCount")
    val merchant2UserCount: Long?,

    @SerializedName("user2Online")
    val user2Online: Long?
)