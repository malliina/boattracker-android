plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    compileSdk = 33
    defaultConfig {
        applicationId = "com.malliina.boattracker"
        minSdk = 28
        targetSdk = 33
        versionCode = 70
        versionName = "1.4.0"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        val MapboxAccessToken: String by project
        val mapboxAccessToken = System.getenv("MapboxAccessToken") ?: MapboxAccessToken
        buildConfigField("String", "MapboxAccessToken", "\"${mapboxAccessToken}\"")
    }
    signingConfigs {
        create("release") {
            if (System.getenv("CI") == "true") {
                storeFile = rootProject.file("keystore.jks")
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            } else {
                val RELEASE_STORE_FILE: String by project
                storeFile = file(RELEASE_STORE_FILE)
                val RELEASE_STORE_PASSWORD: String by project
                storePassword = RELEASE_STORE_PASSWORD
                val RELEASE_KEY_ALIAS: String by project
                keyAlias = RELEASE_KEY_ALIAS
                val RELEASE_KEY_PASSWORD: String by project
                keyPassword = RELEASE_KEY_PASSWORD
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }
    namespace = "com.malliina.boattracker"
}

repositories {
    mavenCentral()
    google()
    maven {
        url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
        authentication {
            create<BasicAuthentication>("basic")
        }
        credentials() {
            // Do not change the username below.
            // This should always be `mapbox` (not your username).
            username = "mapbox"
            // Use the secret token you stored in gradle.properties as the password
            val MAPBOX_DOWNLOADS_TOKEN: String by project
            password = System.getenv("MapboxDownloadsToken") ?: MAPBOX_DOWNLOADS_TOKEN ?: ""
        }
    }
}

val kotlinVersion: String by project

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2022.12.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui:ui-viewbinding")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
    // Optional - Integration with LiveData
    implementation("androidx.compose.runtime:runtime-livedata")

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.0-alpha04")
    val navigationVersion = "2.5.3"
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")
    implementation("com.google.android.gms:play-services-auth:20.4.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation("com.google.firebase:firebase-messaging:23.1.1")
    implementation("com.jakewharton.timber:timber:5.0.1")
//    implementation("com.mapbox.mapboxsdk:mapbox-android-plugin-annotation-v9:0.9.0")
    implementation("com.mapbox.maps:android:10.11.0")
    val appCenterSdkVersion = "4.3.1"
    implementation("com.microsoft.appcenter:appcenter-analytics:$appCenterSdkVersion")
    implementation("com.microsoft.appcenter:appcenter-crashes:$appCenterSdkVersion")
    implementation("com.neovisionaries:nv-websocket-client:2.9")
    val moshiVersion = "1.14.0"
    implementation("com.squareup.moshi:moshi:$moshiVersion")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("me.zhanghai.android.materialprogressbar:library:1.6.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    val coroutinesVersion = "1.6.4"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation("junit:junit:4.13.2")
}

apply(plugin = "com.google.gms.google-services")
