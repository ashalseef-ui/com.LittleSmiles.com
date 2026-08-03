# --- Little Buds Academy Production ProGuard Rules ---

# Preserve line numbers for Crashlytics
-keepattributes SourceFile,LineNumberTable

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep interface dagger.hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class *
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}

# Firebase & Play Services
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Gson / JSON Models
-keep class com.LittleSmiles.com.core.domain.model.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Jetpack Compose
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# Billing
-keep class com.android.billingclient.** { *; }

# Lottie
-keep class com.airbnb.lottie.** { *; }
