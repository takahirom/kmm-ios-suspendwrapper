import java.util.Properties

plugins {
    kotlin("multiplatform")
    id("java-library")
    id("maven-publish")
}

repositories {
    jcenter()
}

kotlin {

    jvm()
    ios()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2-native-mt")
            }
        }
    }
}

publishing {
    publications.all {
        version = rootProject.version
        group = rootProject.group
    }
}
