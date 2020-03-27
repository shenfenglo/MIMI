package com.dabenxiang.mimi

import android.app.Application
import android.util.Log
import com.dabenxiang.mimi.di.apiModule
import com.dabenxiang.mimi.di.appModule
import com.dabenxiang.mimi.di.viewModelModule
import com.dabenxiang.mimi.model.pref.Pref
import com.facebook.stetho.Stetho
import com.flurry.android.FlurryAgent
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application() {

    companion object {
        lateinit var self: Application
        var pref: Pref? = null
        fun applicationContext(): Application {
            return self
        }
    }

    init {
        self = this
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Stetho.initializeWithDefaults(this)
        } else {
//            FlurryAgent.Builder()
//                .withLogEnabled(true)
//                .withCaptureUncaughtExceptions(true)
//                .withContinueSessionMillis(10000)
//                .withLogLevel(Log.VERBOSE)
//                .build(this, FLURRY_API_KEY)
        }

        val module = listOf(
            appModule,
            apiModule,
            viewModelModule
        )

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(module)
        }
    }

}