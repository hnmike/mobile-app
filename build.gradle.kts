// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    
    // Add the dependency for the Google services Gradle plugin
    id("com.google.gms.google-services") version "4.4.2" apply false
}
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
    }
}
allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.facebook.com")
        }
    }
}
