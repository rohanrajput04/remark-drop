# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson
-keep class com.inkdrop.app.data.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
