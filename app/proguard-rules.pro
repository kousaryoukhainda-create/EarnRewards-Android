# ProGuard configuration for EarnRewards
# This file specifies the rules for code obfuscation, optimization, and shrinking

# Keep the main application class
-keep public class com.ykapps.earnrewards.MainActivity
-keep public class com.ykapps.earnrewards.EarnRewardsApplication

# Keep all View classes (required by Android framework)
-keep public class android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep native method names
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Activity classes
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends androidx.fragment.app.Fragment

# Keep Flutter-related classes
-keep class io.flutter.** { *; }
-keep class com.google.android.material.** { *; }
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep interface com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep interface com.google.android.gms.** { *; }

# Keep AppLovin MAX
-keep class com.applovin.** { *; }
-keep interface com.applovin.** { *; }

# Keep Ad Network SDKs
-keep class com.facebook.ads.** { *; }
-keep interface com.facebook.ads.** { *; }

-keep class com.bytedance.sdk.** { *; }
-keep interface com.bytedance.sdk.** { *; }

-keep class com.ironsource.** { *; }
-keep interface com.ironsource.** { *; }

# Keep AppsFlyer
-keep class com.appsflyer.** { *; }
-keep interface com.appsflyer.** { *; }

# Keep OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepnames class okhttp3.internal.names.**

# Keep Retrofit
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Keep Gson
-keep class com.google.gson.** { *; }
-keep interface com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Kotlin
-keep class kotlin.** { *; }
-keep interface kotlin.** { *; }

# Keep method parameters for Annotation processing
-keepattributes *Annotation*

# Keep line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep R classes
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep methods used by reflection
-keepclasseswithmembernames class * {
    public <init>(java.lang.String);
}

# Keep custom application classes
-keep class com.ykapps.earnrewards.** { *; }
-keep interface com.ykapps.earnrewards.** { *; }

# Keep interfaces that define on-screen views
-keep public interface com.ykapps.earnrewards.platform.** { *; }
-keep public class com.ykapps.earnrewards.platform.** { *; }

# Keep model classes
-keep class com.ykapps.earnrewards.models.** { *; }
-keep interface com.ykapps.earnrewards.models.** { *; }

# Optimization settings
-optimizationpasses 5
-dontusemixedcaseclassnames
-verbose

# Obfuscation settings
-obfuscationdictionary obfuscation.txt
-packageobfuscationdictionary obfuscation.txt
-classarithmetic

# Optimization
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# Reflection warnings
-dontnote java.io.File
-dontnote javax.crypto.**
-dontwarn java.lang.invoke.**
-dontwarn javax.annotation.**
-dontwarn sun.misc.**
-dontwarn com.sun.**

# Ad Network specific rules
-dontwarn com.facebook.ads.**
-dontwarn com.bytedance.sdk.**
-dontwarn com.ironsource.**

# Firebase specific warnings
-dontwarn com.google.firebase.analytics.ktx.analytics
-dontwarn com.google.firebase.remoteconfig.ktx.remoteConfig

# Kotlin specific
-dontwarn kotlin.reflect.**
-dontwarn java.lang.reflect.**
