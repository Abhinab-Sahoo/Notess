// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.dagger.hilt.android") version "2.56.2" apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

buildscript {

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
//        classpath ("com.android.tools.build:gradle:8.9")
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
        classpath ("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.6")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}