# Preserve line numbers and source file names for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve annotations and signatures for reflection/serialization
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Retrofit 2
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowoptimization interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# OkHttp
-keepattributes *Annotation*
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Moshi
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep class * extends com.squareup.moshi.JsonAdapter {
    <init>(...);
}
-keep class **JsonAdapter {
    <init>(...);
}
-keep @com.squareup.moshi.JsonClass class * {
    <init>(...);
}
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}

# Room Database
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class * extends androidx.room.EntityDeletionOrUpdateAdapter { *; }
-keep class * extends androidx.room.EntityInsertionAdapter { *; }

# Data Models & Entities (com.example.data)
-keep class com.example.data.** { *; }

# DataStore
-keep class androidx.datastore.** { *; }

# Kotlin Coroutines
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }

# iText 7 PDF Generation
-dontwarn com.itextpdf.**
-keep class com.itextpdf.** { *; }

