package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.enums.PaymentType
import com.google.gson.annotations.SerializedName

data class CreateOrderRequest(
    @SerializedName("paymentType")
    val paymentType: Int? = PaymentType.BANK.value,

    @SerializedName("packageId")
    val packageId: Long = 0
)