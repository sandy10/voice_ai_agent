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
# --- Agora Proguard Rules ---
-keep class io.agora.**{*;}
-dontwarn io.agora.**

# --- Retrofit and Gson Rules ---
# Retrofit does reflection, require the following rules
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson specific classes
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Keep App Data Models for Gson serialization/deserialization
-keep class com.sandeep.agoraai.data.** { *; }
-keep class com.sandeep.agoraai.model.** { *; }
-keep class com.sandeep.agoraai.mood.** { *; }

# Prevent R8 from stripping generic signatures from TypeToken subclasses
-keep class * extends com.google.gson.reflect.TypeToken

# Keep Compose classes safe
-keep class androidx.compose.** { *; }


# --- Google Play Core In-App Updates ---
-keep class com.google.android.play.core.** { *; }
-dontwarn com.google.android.play.core.**
-keep class com.google.android.gms.tasks.** { *; }
-dontwarn com.google.android.gms.tasks.**
