package com.dabenxiang.mimi.model.pref

import com.blankj.utilcode.util.ConvertUtils
import com.dabenxiang.mimi.model.api.vo.DecryptSettingItem
import com.dabenxiang.mimi.model.vo.ProfileItem
import com.dabenxiang.mimi.model.vo.SearchHistoryItem
import com.dabenxiang.mimi.model.vo.TokenItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.lang.reflect.Type

class Pref(private val gson: Gson, preferenceFileName: String, isDebug: Boolean) :
    AbstractPref(preferenceFileName, isDebug) {

    private val tokenPref = StringPref("TOKEN")
    private val memberTokenPref = StringPref("MEMBER_TOKEN")
    private val profilePref = StringPref("PROFILE")
    private val keepAccountPref = BooleanPref("KEEP_ACCOUNT")
    private var searchHistoryPref = StringPref("SEARCH_HISTORY")
    private var meAvatarPref = StringPref("ME_AVATAR")
    private var decryptSettingPref = StringPref("DECRYPT_SETTING")

    private var cachedPublicToken: TokenItem? = null
    private var cachedMemberToken: TokenItem? = null
    private var cachedDecryptSettingArray: ArrayList<DecryptSettingItem> = arrayListOf()

    var decryptSettingArray: ArrayList<DecryptSettingItem>
        get() =
            try {
                if (cachedDecryptSettingArray.size == 0) {
                    val listType: Type = object : TypeToken<List<DecryptSettingItem?>?>() {}.type
                    cachedDecryptSettingArray = gson.fromJson(decryptSettingPref.get(), listType)
                }
                cachedDecryptSettingArray
            } catch (e: Exception) {
                arrayListOf()
            }
        set(value) {
            cachedDecryptSettingArray.addAll(value)
            decryptSettingPref.set(gson.toJson(value))
        }

    var publicToken: TokenItem
        get() =
            try {
                if (cachedPublicToken == null) {
                    cachedPublicToken = gson.fromJson(tokenPref.get(), TokenItem::class.java)
                }

                cachedPublicToken ?: TokenItem()
            } catch (e: Exception) {
                TokenItem()
            }
        set(value) {
            cachedPublicToken = value
            tokenPref.set(gson.toJson(value))
        }

    var memberToken: TokenItem
        get() =
            try {
                if (cachedMemberToken == null) {
                    cachedMemberToken = gson.fromJson(memberTokenPref.get(), TokenItem::class.java)
                }

                cachedMemberToken ?: TokenItem()
            } catch (e: Exception) {
                TokenItem()
            }
        set(value) {
            cachedMemberToken = value
            memberTokenPref.set(gson.toJson(value))
        }

    var profileItem: ProfileItem
        get() =
            try {
                gson.fromJson(profilePref.get(), ProfileItem::class.java)
            } catch (e: Exception) {
                Timber.i("signUpGuest profileItem Exception=$e")
                ProfileItem()
            }
        set(value) {
            profilePref.set(gson.toJson(value))
        }

    var searchHistoryItem: SearchHistoryItem
        get() =
            try {
                gson.fromJson(searchHistoryPref.get(), SearchHistoryItem::class.java)
            } catch (e: Exception) {
                SearchHistoryItem()
            }
        set(value) {
            searchHistoryPref.set(gson.toJson(value))
        }

    var keepAccount: Boolean
        get() = keepAccountPref.get()
        set(value) = keepAccountPref.set(value)

    var meAvatar: ByteArray?
        get() = if (meAvatarPref.get() != null) ConvertUtils.hexString2Bytes(meAvatarPref.get()!!) else null
        set(value) = meAvatarPref.set(
            if (value == null) ""
            else ConvertUtils.bytes2HexString(value)
        )

    fun clearMemberToken() {
        cachedMemberToken = null
        memberTokenPref.remove()
    }

    fun clearProfile() {
        profilePref.remove()
    }

    fun clearToken() {
        memberTokenPref.remove()
    }

    fun clearSearchHistory() {
        searchHistoryPref.remove()
    }
}
