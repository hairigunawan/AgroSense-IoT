# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep data models
-keep class com.example.it_project_2.model.** { *; }

# Keep Retrofit/Gson models
-keep class com.example.it_project_2.*Response { *; }

# Keep Firebase
-keep class com.google.firebase.** { *; }

# Preserve line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# Hide original source file name.
-renamesourcefileattribute SourceFile