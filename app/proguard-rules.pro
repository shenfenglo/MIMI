# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#noinspection ShrinkerUnresolvedReference
-keepattributes SourceFile,LineNumberTable

# for enum
-keepclassmembers enum * { *; }

# for All Throwable
-keep class * extends java.lang.Throwable

# for app vo
-keep class com.dabenxiang.mimi.model.vo.** {*;}
-keep class com.dabenxiang.mimi.model.api.vo.** {*;}

# for okhttp (from okhttp3.pro)
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*
# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform

# for retrofit
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod
# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**
# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit
# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
#-dontwarn retrofit2.KotlinExtensions$*

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.dubai.fa.model.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

#coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# AndroidX
-dontwarn com.google.android.material.**
-keep class com.google.android.material.** { *; }
-dontwarn androidx.**
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-keep class * extends androidx.fragment.app.Fragment{}

# support design
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }

# LiveEventBus
-dontwarn com.jeremyliao.liveeventbus.**
-keep class com.jeremyliao.liveeventbus.** { *; }

# MQTT
-keep class org.eclipse.paho.clent.mqttv3.** {*;}
-keep class org.eclipse.paho.client.mqttv3.*$* { *; }
-keep class org.eclipse.paho.client.mqttv3.logging.JSR47Logger { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

-keep class com.google.crypto.tink.** { *; }
-keepclassmembers class * extends com.google.crypto.tink.shaded.protobuf.GeneratedMessageLite {
  <fields>;
}

# BaseManager
-keep class tw.gov.president.manager.data.** {*;}
-keep class tw.gov.president.manager.BaseDomainManager.** {*;}

# DeviceInfoProvider
-keep class tw.gov.president.provider.device.info.** {*;}
-keep class tw.gov.president.provider.device.di.** {*;}

# Device
-keep class tw.gov.president.general.data.info.** {*;}
-keep class tw.gov.president.general.data.info.device.** {*;}
-keep class tw.gov.president.general.data.info.request.** {*;}

# LogManager
-keep class tw.gov.president.manager.submanager.logmoniter.** {*;}
-keep class tw.gov.president.manager.submanager.logmoniter.di.** {*;}
-keep class tw.gov.president.manager.submanager.logmoniter.api.** {*;}

# LogLevel
-keep class tw.gov.president.general.data.loglevel.** {*;}

# UpdateManager
-keep class tw.gov.president.manager.submanager.update.** {*;}
-keep class tw.gov.president.manager.submanager.update.di.** {*;}
-keep class tw.gov.president.manager.submanager.update.data.** {*;}
-keep class tw.gov.president.manager.submanager.update.callback.** {*;}
-keep class tw.gov.president.manager.submanager.update.api.** {*;}
-keep class tw.gov.president.manager.submanager.update.worker.** {*;}

# ApiResult
-keep class tw.gov.president.utils.general.utils.api.result.** {*;}

-keep class go.** {*;}
-keep class libs.** {*;}
