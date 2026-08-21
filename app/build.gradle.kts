import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun localOrEnv(vararg names: String, default: String = ""): String {
    names.forEach { name ->
        localProperties.getProperty(name)?.let { return it }
        System.getenv(name)?.let { return it }
    }
    return default
}

fun quote(value: String): String {
    val escaped = value.replace("\\", "\\\\").replace("\"", "\\\"")
    return "\"$escaped\""
}

android {
    namespace = "com.sandeep.agoraai"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.sandeep.agoraai"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        buildConfigField(
            "String",
            "QUICKSTART_SERVER_URL",
            quote(localOrEnv("QUICKSTART_SERVER_URL"))
        )
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystorePath = localOrEnv("KEYSTORE_PATH", "release.jks")
            val keystoreFile = file(keystorePath)
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = localOrEnv("KEYSTORE_PASSWORD")
                keyAlias = localOrEnv("KEY_ALIAS")
                keyPassword = localOrEnv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            val keystorePath = localOrEnv("KEYSTORE_PATH", "release.jks")
            val keystoreFile = file(keystorePath)
            if (keystoreFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    packaging {
        jniLibs {
            pickFirsts += listOf(
                "**/libaosl.so",
                "**/libagora_rtc_sdk.so",
                "**/libagora_core.so",
                "**/libagora_fdkaac.so",
                "**/libagora_soundtouch.so"
            )
        }
    }
}

dependencies {
    implementation(libs.agora.rtc)
    implementation(libs.agora.rtm)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
