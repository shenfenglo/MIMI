package com.dabenxiang.mimi.model.api.vo

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AdItem(
    @SerializedName("href")
    val href: String = "",

    @SerializedName("targetType")
    val targetType: Int = 0,

    @SerializedName("target")
    val target: String = "",

    @SerializedName("custom")
    val custom: String = ""
) : Serializable

class AdItemConverters {

//    @TypeConverter
//    fun adItemToJson(item: AdItem?): String = item?.let{
//        Gson().toJson(it)
//    } ?: Gson().toJson("")
//
//    @TypeConverter
//    fun jsonToAdItem(value: String):AdItem? = value.takeIf { it.isEmpty() }?.let{
//        Gson().fromJson(value, AdItem::class.java)
//    } ?: run {
//        null
//    }

    @TypeConverter
    fun adItemToJson(item: AdItem?): String = if(item ==null) "" else Gson().toJson(item)

    @TypeConverter
    fun jsonToAdItem(value: String):AdItem? = if(value.isEmpty()) null
        else Gson().fromJson(value, AdItem::class.java)?: null
}