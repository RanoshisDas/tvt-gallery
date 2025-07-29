plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.ranoshisdas.app.tvtgallery"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ranoshisdas.app.tvtgallery"
        minSdk = 27
        targetSdk = 35
        versionCode = 5
        versionName = "1.3.0"

        ndk {
            debugSymbolLevel = "FULL"
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    lint {
        checkReleaseBuilds = true
        abortOnError = true
    }
}

dependencies {
    // Core Android libraries
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Image loading and preview
    implementation(libs.squareup.picasso) // Optional if Glide covers all use cases
    implementation(libs.glide)
    annotationProcessor(libs.compiler)

    // ViewPager2 for image swiping
    implementation(libs.viewpager2)

    // Biometric authentication (note: alpha version)
    implementation(libs.biometric)

    //video player
    implementation (libs.exoplayer)

    implementation (libs.conscrypt.android)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
