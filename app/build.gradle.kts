plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.bookappkotlin"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.bookappkotlin"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packagingOptions {
        exclude("META-INF/INDEX.LIST")
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/DEPENDENCIES.txt")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/license.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/notice.txt")
        exclude("META-INF/ASL2.0")
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures{
        viewBinding =  true
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    //implementation("io.github.afreakyelf:Pdf-Viewer:2.1.1")
    implementation("com.github.barteksc:android-pdf-viewer:2.8.2")

    implementation ("com.github.bumptech.glide:glide:4.16.0")

    implementation ("com.google.android.material:material:1.4.0")
    implementation ("androidx.appcompat:appcompat:1.3.1'")

    implementation("com.pspdfkit:pspdfkit:2024.3.0")


    implementation("com.google.cloud:google-cloud-texttospeech:2.4.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.7.0")
    implementation ("io.grpc:grpc-okhttp:1.42.0") // veya grpc-netty, grpc-netty-shaded
    implementation ("io.grpc:grpc-protobuf:1.42.0")
    implementation ("io.grpc:grpc-stub:1.42.0")

    androidTestImplementation(libs.androidx.espresso.core)
}