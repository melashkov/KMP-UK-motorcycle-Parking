import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.ui)
    compileOnly(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.melashkov.mcparking"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.melashkov.mcparking"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = providers.gradleProperty("releaseVersionCode")
            .map(String::toInt)
            .getOrElse(1678468730)
        versionName = "3.0.2"
        // Only advertise ABIs that can load both MapLibre native libraries.
        ndk.abiFilters += setOf("arm64-v8a", "armeabi-v7a", "x86_64")
    }
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            versionNameSuffix = "-dev"
            manifestPlaceholders["apiEnvironment"] = "development"
        }
        create("prod") {
            dimension = "environment"
            manifestPlaceholders["apiEnvironment"] = "production"
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    val releaseKeystore = providers.environmentVariable("RELEASE_KEYSTORE").orNull
    val releaseStorePassword = providers.environmentVariable("RELEASE_STORE_PASSWORD").orNull
    val releaseKeyAlias = providers.environmentVariable("RELEASE_KEY_ALIAS").orNull
    val releaseKeyPassword = providers.environmentVariable("RELEASE_KEY_PASSWORD").orNull
    val releaseCredentials = listOf(releaseKeystore, releaseStorePassword, releaseKeyAlias, releaseKeyPassword)
    val releaseSigning = if (releaseCredentials.any { !it.isNullOrBlank() }) {
        require(releaseCredentials.none { it.isNullOrBlank() }) {
            "Set all four RELEASE_* signing variables for a signed Android release."
        }
        signingConfigs.create("release") {
            storeFile = releaseKeystore?.let(::file)
            storePassword = releaseStorePassword
            keyAlias = releaseKeyAlias
            keyPassword = releaseKeyPassword
        }
    } else {
        null
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            ndk.debugSymbolLevel = "FULL"
            signingConfig = releaseSigning
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
    buildFeatures {
        compose = true
    }
}

androidComponents {
    beforeVariants(
        selector()
            .withBuildType("release")
            .withFlavor("environment" to "dev"),
    ) { variant ->
        variant.enable = false
    }
}
