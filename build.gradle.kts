buildscript {
    repositories{
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.google.services)
        classpath(libs.gradle)
        classpath(libs.firebase.crashlytics.gradle)

    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {

    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
}