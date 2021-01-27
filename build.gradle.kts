buildscript {
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
        classpath("com.android.tools.build:gradle:4.1.2")
        classpath("de.mannodermaus.gradle.plugins:android-junit5:1.7.0.0")
    }
}


group = "com.github.takahirom"
version = "0.1.1"
