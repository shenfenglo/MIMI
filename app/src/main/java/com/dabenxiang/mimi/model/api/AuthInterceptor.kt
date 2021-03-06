package com.dabenxiang.mimi.model.api

import android.text.TextUtils
import com.dabenxiang.mimi.model.api.ApiResult.Empty
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.enums.TokenResult
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

class AuthInterceptor(private val pref: Pref) : Interceptor, KoinComponent {

    private val domainManager: DomainManager by inject()
    private val accountManager: AccountManager by inject()

    override fun intercept(chain: Interceptor.Chain): Response {

        var response: Response? = null
        val request = chain.request()
        val url = request.url
        Timber.d("URL: $url")
        if (checkTokenUrl(url.toString())) {
            return chain.proceed(chain.request())
        }

        val hasMemberToken = checkHasMemberToken(url.toString())
        if (hasMemberToken) {
            when (accountManager.getMemberTokenResult()) {
                TokenResult.EXPIRED -> doRefreshToken()
                TokenResult.EMPTY -> logout()
            }
        } else {
            when (accountManager.getPublicTokenResult()) {
                TokenResult.EMPTY,
                TokenResult.EXPIRED -> getPublicToken()
            }
        }

        val newRequest = buildRequest(request, hasMemberToken)

        try {
            response = chain.proceed(newRequest)
            Timber.d("AuthInterceptor Response Code: ${response.code}")
            Timber.d("AuthInterceptor Response  ${response.body}")
            return when (response.code) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    response.close()
                    if (hasMemberToken) {
                        doRefreshToken()
                    } else {
                        getPublicToken()
                    }
                    chain.proceed(buildRequest(newRequest, hasMemberToken))
                }
                else -> response
            }
        } catch (e: Exception) {
            Timber.d("AuthInterceptor Exception  $e")
            response?.close()
            return when (e) {
                is UnknownHostException,
                is SocketTimeoutException,
                is SSLHandshakeException -> {
                    domainManager.changeApiDomainIndex()
                    chain.proceed(buildRequest(request, hasMemberToken))
                }
                else -> chain.proceed(chain.request())
            }
        }
    }

    private fun buildRequest(request: Request, hasMemberToken: Boolean): Request {
        val url = request.url
        val host = url.host

        val newDomain = when {
            url.toString().startsWith("https://ad-api.")
                    || url.toString().startsWith("http://ad-api.")
                    || url.toString().contains("http://api.promotion.dev-121.silkrode.io") -> {
                domainManager.getAdDomain()
            }
            else -> {
                domainManager.getApiDomain()
            }
        }

        val newHost = newDomain.toHttpUrlOrNull()?.host
        Timber.d("host: $host, newHost: $newHost")

        val requestBuilder = when {
            !TextUtils.isEmpty(newHost) && !TextUtils.equals(host, newHost) -> {
                val newUrl = request.url.newBuilder().host(newHost.toString()).build()
                Timber.d("newUrl: $newUrl")
                request.newBuilder().url(newUrl)
            }
            else -> request.newBuilder()
        }

        val accessToken = when {
            hasMemberToken -> pref.memberToken.accessToken
            else -> pref.publicToken.accessToken
        }

        if (!url.toString().contains("/v1/Business") && !url.toString().contains("/v1/business")) {
            val auth = StringBuilder(ApiRepository.BEARER).append(accessToken).toString()
            requestBuilder.addHeader(ApiRepository.AUTHORIZATION, auth)
        }

        requestBuilder.header(ApiRepository.X_DEVICE_ID, GeneralUtils.getAndroidID())
        requestBuilder.header(ApiRepository.X_REQUESTED_FROM, "Android")

        return requestBuilder.build()
    }

    private fun checkTokenUrl(url: String): Boolean {
        return url.endsWith("/token")
    }

    private fun checkHasMemberToken(url: String): Boolean {
        var hasMemberToken = accountManager.hasMemberToken()
        if (hasMemberToken) {
            hasMemberToken = !url.endsWith("/signin")
        }
        return hasMemberToken
    }

    private fun doRefreshToken() {
        runBlocking(Dispatchers.IO) {
            accountManager.refreshToken()
                .collect {
                    when (it) {
                        is Empty -> Timber.d("Refresh token successful!")
                        is Error -> {
                            Timber.e("Refresh token error: $it")
                            logout()
                        }
                    }
                }
        }
    }

    private fun getPublicToken() {
        runBlocking(Dispatchers.IO) {
            accountManager.getPublicToken()
                .collect {
                    when (it) {
                        is Empty -> Timber.d("Get public token successful!")
                        is Error -> {
                            Timber.e("Get public token error: $it")
                            logout()
                        }
                    }
                }
        }
    }

    private fun logout() {
        accountManager.logoutLocal()
        getPublicToken()
    }
}
