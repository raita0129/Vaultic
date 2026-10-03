import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // AGP 9 起內建 Kotlin 編譯,不再需要另外套用 org.jetbrains.kotlin.android
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

// 簽章設定檔放在專案外,路徑由 ~/.gradle/gradle.properties 的 vaulticSigningProperties 指定,
// 避免密碼留在專案資料夾內;未設定或檔案不存在時略過(CI 只建 debug 版,不需要簽章)
val keystoreProperties = Properties().apply {
    val path = providers.gradleProperty("vaulticSigningProperties").orNull
    val propertiesFile = path?.let { file(it) }
    if (propertiesFile != null && propertiesFile.exists()) {
        propertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.raita.vaultic"
    // sqlcipher-android 4.19.0 要求至少 compileSdk 37 才能編譯,
    // targetSdk 維持 36(符合 Play 政策門檻即可,不必跟著採用更新的執行期行為變更)
    compileSdk = 37

    defaultConfig {
        applicationId = "com.raita.vaultic"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"
    }

    signingConfigs {
        create("release") {
            if (keystoreProperties.containsKey("storeFile")) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    // 依 res/values-* 自動產生 localeConfig,讓 Android 13+ 可在系統設定中個別切換 App 語言
    androidResources {
        generateLocaleConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        // MainActivity 需要 BuildConfig.DEBUG 判斷是否開啟防截圖
        buildConfig = true
    }
}

// 輸出檔名帶上版本號,例如 vaultic-1.0.0-release.aab,方便區分各版本
base {
    archivesName = "vaultic-${android.defaultConfig.versionName}"
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
    }
}

dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // 資料庫:Room + SQLCipher
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")
    // net.zetetic:android-database-sqlcipher 已停止維護且不支援 16KB page size,
    // 改用官方後繼的 sqlcipher-android
    implementation("net.zetetic:sqlcipher-android:4.19.0")
    implementation("androidx.sqlite:sqlite:2.7.1")

    // 加密
    implementation("com.lambdapioneer.argon2kt:argon2kt:1.6.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // Test
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
}