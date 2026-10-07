plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
}

// Apply to the app's runtime resolution as well as the shared library's compile classpath.
subprojects {
    val mapLibreAndroidVersion = providers.gradleProperty("mapLibreAndroidVersion").get()
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution {
            substitute(module("org.maplibre.gl:android-sdk"))
                .using(module("org.maplibre.gl:android-sdk-opengl:$mapLibreAndroidVersion"))
                .because("Runtime GeoJSON layers do not render correctly with Vulkan on target Android devices")
        }
    }
}
