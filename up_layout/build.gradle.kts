import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Zip
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    `maven-publish`
}

// Maven coordinates for the published library.
// Group mirrors the library's Kotlin package (com.mdsw.uplayout) and Android namespace.
// ArtifactIds keep the Kotlin Multiplatform defaults derived from the up_layout
// module name (up_layout, up_layout-android, up_layout-iosarm64, ...).
// The version is supplied externally by the release workflow (e.g. -Pversion=0.1.0
// derived from tag v0.1.0). Local builds default to a snapshot and are never released.
group = "com.mdsw.uplayout"
version = providers.gradleProperty("version").orElse("0.0.0-SNAPSHOT").get()

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "UpLayout"
            isStatic = true
        }
    }

    android {
        namespace = "com.mdsw.uplayout"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// Standard Kotlin Multiplatform publishing: the KMP plugin creates the root
// kotlinMultiplatform publication plus one publication per target (android,
// iosArm64, iosSimulatorArm64) once `maven-publish` is applied.
// See https://kotlinlang.org/docs/multiplatform/multiplatform-publish-lib-setup.html
publishing {
    publications.withType<MavenPublication> {
        pom {
            name = "up-layout"
            description = "Compose Multiplatform container with user-positionable children (drag, rotate, resize)."
            url = "https://github.com/maks45/up-layout"
            licenses {
                license {
                    name = "Apache License, Version 2.0"
                    url = "https://www.apache.org/licenses/LICENSE-2.0"
                }
            }
        }
    }
    repositories {
        // File-based repository used to assemble the GitHub Release archive.
        // No external credentials required: CI publishes here, then zips it.
        maven {
            name = "release"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

// Self-contained library archive for the GitHub Release, e.g. up-layout-0.1.0.zip.
// Contains only the publishable up_layout Maven artifacts (no repo sources, no demo apps).
tasks.register<Zip>("releaseArchive") {
    group = "release"
    description = "Publishes all up_layout publications to the file-based release repo and zips it."
    dependsOn("publishAllPublicationsToReleaseRepository")
    from(layout.buildDirectory.dir("repo"))
    archiveBaseName = "up-layout"
    archiveVersion = project.version.toString()
    destinationDirectory = layout.buildDirectory.dir("distributions")
}
