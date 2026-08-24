sed -i '/\[versions\]/a splashscreen = "1.0.1"' gradle/libs.versions.toml
sed -i '/\[libraries\]/a androidx-core-splashscreen = { group = "androidx.core", name = "core-splashscreen", version.ref = "splashscreen" }' gradle/libs.versions.toml
