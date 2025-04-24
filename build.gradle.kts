// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    extra.apply {
        set("kotlin_version", "1.8.10")
        set("compose_version", "1.4.3")
        set("hilt_version", "2.45")
        set("room_version", "2.5.2")
        set("firebase_version", "32.2.0")
        set("camerax_version", "1.2.3")
        set("zxing_version", "3.5.1")
    }

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.9.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${project.extra["kotlin_version"]}")
        classpath("com.google.gms:google-services:4.3.15")
        classpath("com.google.dagger:hilt-android-gradle-plugin:${project.extra["hilt_version"]}")
    }
}

// Plugin configuration for all projects
plugins {
    id("com.android.application") version "8.9.2" apply false
    id("com.android.library") version "8.9.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("com.google.dagger.hilt.android") version "2.48.1" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    alias(libs.plugins.compose.compiler) apply false
}

// Clean task
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}