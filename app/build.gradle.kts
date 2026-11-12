plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.rosevoicex"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rosevoicex"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
