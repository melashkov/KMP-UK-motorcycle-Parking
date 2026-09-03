import io.github.frankois944.spmForKmp.swiftPackageConfig
import org.gradle.api.tasks.Sync
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.spmForKmp) // Add this
    alias(libs.plugins.koin.compiler)
}

configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(module("org.maplibre.gl:android-sdk"))
            .using(module("org.maplibre.gl:android-sdk-opengl:13.0.2"))
            .because("Runtime GeoJSON layers do not render correctly with Vulkan on target Android devices")
    }
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.swiftPackageConfig {
            dependency {
                remotePackageVersion(
                    url = URI("https://github.com/maplibre/maplibre-gl-native-distribution.git"),
                    products = { add("MapLibre", exportToKotlin = true) },
                    packageName = "maplibre-gl-native-distribution",
                    version = "6.25.1",
                )
            }
        }

        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    android {
        namespace = "com.melashkov.mcparking.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.backhandler)
            implementation(libs.compose.components.resources)
            implementation(libs.material.icons.extended)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.navigation3.runtime)
            implementation(libs.navigation3.ui)
            implementation(libs.maplibre.compose)
            implementation(libs.kotlinx.collections.immutable)
            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.annotations)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            //implementation(libs.koin.compose.navigation3)
            //Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

val copyMapLibreFrameworkForIosSimulatorTests = tasks.register<Sync>(
    "copyMapLibreFrameworkForIosSimulatorTests",
) {
    dependsOn("SwiftPackageConfigAppleIosSimulatorArm64CompileSwiftPackageIosSimulatorArm64")
    from(
        layout.buildDirectory.dir(
            "spmKmpPlugin/iosSimulatorArm64/scratch/arm64-apple-ios-simulator/release/MapLibre.framework",
        ),
    )
    into(
        layout.buildDirectory.dir(
            "bin/iosSimulatorArm64/debugTest/Frameworks/MapLibre.framework",
        ),
    )
}

tasks.named("iosSimulatorArm64Test") {
    dependsOn(copyMapLibreFrameworkForIosSimulatorTests)
}
