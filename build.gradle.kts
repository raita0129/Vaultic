plugins {
    // AGP 9 起內建 Kotlin 編譯,不再需要另外套用 org.jetbrains.kotlin.android
    id("com.android.application") version "9.4.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.3.12" apply false
}