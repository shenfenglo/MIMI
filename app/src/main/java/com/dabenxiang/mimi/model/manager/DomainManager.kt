package com.dabenxiang.mimi.model.manager

import antiblock.Antiblock
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.PROJECT_NAME
import com.dabenxiang.mimi.model.api.AdRepository
import com.dabenxiang.mimi.model.api.AdService
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ApiService
import com.dabenxiang.mimi.model.vo.domain.DomainInputItem
import com.dabenxiang.mimi.model.vo.domain.DomainOutputItem
import com.dabenxiang.mimi.model.vo.domain.DomainOutputListItem
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import tw.gov.president.manager.submanager.logmoniter.di.SendLogManager

class DomainManager(private val gson: Gson, private val okHttpClient: OkHttpClient) {

    companion object {
        const val MIMI_PROJECT_ID = "815e22a6"
        const val DOWNLOAD_SERVER_PROJECT_ID = "WaumJF6y"
        const val PROMO_CODE = ""
        const val VALIDATION_URL = "/v1/Members/ValidateEmail"
        const val PARAM_SIGNUP_CODE = "/?signupCode="
        const val PARAM_RESET_CODE = "/?resetCode="
        const val FLAVOR_DEV = "dev"
        const val FLAVOR_SIT = "sit"
        const val BUILDTYPE_DEV = "dev"
        const val BUILDTYPE_SIT = "sit"
    }

    private var _goLibVersion = ""
    private var currentDomainIndex = 0
    private val domainList: ArrayList<String> = arrayListOf()
    private var domainOutputs: List<DomainOutputItem> = arrayListOf()

    val goLibVersion
        get() = _goLibVersion

    private val apiRepo by lazy {
        val apiService = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .baseUrl(getApiDomain())
            .build()
            .create(ApiService::class.java)
        ApiRepository(apiService)
    }

    fun getApiRepository(): ApiRepository {
        return apiRepo
    }

    fun getAdRepository(): AdRepository {
        val adService = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .baseUrl(getAdDomain())
            .build()
            .create(AdService::class.java)
        return AdRepository(adService)
    }

    fun changeApiDomainIndex() {
        if (domainList.isNotEmpty()) {
            currentDomainIndex++
        }
    }

    fun getApiDomain(): String {
        return if (BuildConfig.BUILD_TYPE.contains(BUILDTYPE_DEV)) {
            BuildConfig.API_HOST
        } else {
            val domains = getDomain()
            if (domains.isEmpty()) {
                BuildConfig.API_HOST
            } else {
                StringBuilder("https://api.").append(getDomain()).toString()
            }
        }
    }

    fun getAdDomain(): String {
        return if (BuildConfig.BUILD_TYPE.contains(BUILDTYPE_DEV)) {
            BuildConfig.AD_API_HOST
        } else {
            val domains = getDomain()
            if (domains.isEmpty()) {
                BuildConfig.AD_API_HOST
            } else {
                StringBuilder("https://ad-api.").append(getDomain()).toString()
            }
        }
    }

    fun getWebDomain(): String {
        return when {
            BuildConfig.BUILD_TYPE.contains(BUILDTYPE_DEV) -> {
                BuildConfig.WEB_HOST
            }
            BuildConfig.BUILD_TYPE.contains(BUILDTYPE_SIT) -> {
                BuildConfig.WEB_HOST
            }
            else -> {
                StringBuilder("https://www.").append(getDomain()).toString()
            }
        }
    }

    fun getMqttDomain(isReconnection:Boolean =false): String {
        return when {
            BuildConfig.BUILD_TYPE.contains(BUILDTYPE_DEV) -> {
                BuildConfig.MQTT_HOST
            }
            BuildConfig.BUILD_TYPE.contains(BUILDTYPE_SIT) -> {
                BuildConfig.MQTT_HOST
            }
            else -> {
                if(isReconnection) changeApiDomainIndex()
                val url = getDomain()
                Timber.i("reconnection currentDomainIndex =$currentDomainIndex  url=$url")
                StringBuilder("wss://mqtt.").append(url).toString()
            }
        }
    }

    fun getStorageDomain(): String {
        return if (BuildConfig.BUILD_TYPE.contains(BUILDTYPE_DEV)) {
            BuildConfig.STORAGE_HOST
        } else {
            val domains = getDomain()
            if (domains.isEmpty()) {
                BuildConfig.STORAGE_HOST
            } else {
                StringBuilder("https://storage.").append(getDomain()).toString()
            }
        }
    }

    fun getOldDriverUrl(): String {
        return getWebDomain() + "/chat"
    }

    fun getDomain(): String {
        return when {
            currentDomainIndex < domainList.size -> domainList[currentDomainIndex]
            else -> {
                val domains = getDomains()
                if (domains.isNullOrEmpty()) {
                    ""
                } else {
                    domains[currentDomainIndex]
                }
            }
        }
    }

    private fun getDomains(): List<String> {
        clearData()
        domainOutputs = fetchDomainItem().domainOutputs
        domainOutputs.forEach { domainList.add(it.domain) }
        return domainList
    }

    private fun fetchDomainItem(): DomainOutputListItem {
        val domainInputItem =
            DomainInputItem(
                if(BuildConfig.DEBUG) 1 else 7,
                MIMI_PROJECT_ID,
                App.applicationContext().filesDir.path,
                GeneralUtils.getLibEnv()
            )

        val input = gson.toJson(domainInputItem)
        Timber.i("mimi getDomains input: $input")

        val output = try {
            Antiblock.getDomains(input)
        }catch (e:Exception){
            Timber.i("getDomains Exception: $e")
            SendLogManager.e( PROJECT_NAME, "getDomains Exception: $e")
            null
        }
        Timber.i("mimi getDomains output: $output")

        return if (output.isNullOrEmpty()) {
            DomainOutputListItem()
        } else {
            val result = gson.fromJson(output, DomainOutputListItem::class.java)
            _goLibVersion = result.version
            result
        }
    }

    private fun clearData() {
        currentDomainIndex = 0
        domainList.clear()
    }

}
