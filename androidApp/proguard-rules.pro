# Preserve source locations for retracing production crashes with mapping.txt.
-keepattributes SourceFile,LineNumberTable

# MapLibre JNI and Kotlin serialization keep rules are supplied by those libraries.
# Add targeted rules here only for app reflection or native callbacks that need them.
