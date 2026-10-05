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

// Apply to the final app as well as shared: dependency resolution rules from a
// library project do not propagate to its consumers' runtime configurations.
subprojects {
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution {
            substitute(module("org.maplibre.gl:android-sdk"))
                .using(module("org.maplibre.gl:android-sdk-opengl:13.0.2"))
                .because("Use OpenGL for correct GeoJSON rendering and support devices without Vulkan")
        }
    }
}
