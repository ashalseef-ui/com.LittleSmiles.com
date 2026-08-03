plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    val appPackageId: String = project.findProperty("app_package_id")?.toString() ?: "com.Shabbir.BabyBrush"
    
    namespace = "com.LittleSmiles.com"
    compileSdk = 37

    defaultConfig {
        applicationId = appPackageId
        minSdk = 28
        targetSdk = 37
        versionCode = 10
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Package ID Guard: Ensures applicationId matches google-services.json
tasks.register("verifyPackageId") {
    val currentAppId = android.defaultConfig.applicationId
    val configFile = file("google-services.json")
    
    doLast {
        if (configFile.exists()) {
            val json = groovy.json.JsonSlurper().parseText(configFile.readText()) as Map<*, *>
            val client = (json["client"] as List<*>)[0] as Map<*, *>
            val clientInfo = client["client_info"] as Map<*, *>
            val androidClientInfo = clientInfo["android_client_info"] as Map<*, *>
            val registeredPackageName = androidClientInfo["package_name"].toString()
            
            if (currentAppId != registeredPackageName) {
                throw GradleException(
                    "\n\n[Little Buds Academy GUARD] CONFIGURATION MISMATCH!\n" +
                    "Your applicationId ($currentAppId) does NOT match the one registered in google-services.json ($registeredPackageName).\n" +
                    "Firebase services will fail at runtime. Please fix your build.gradle.kts or update your google-services.json.\n"
                )
            }
        }
    }
}

tasks.named("preBuild") {
    dependsOn("verifyPackageId")
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(project(":core:navigation"))
    implementation(project(":data"))
    implementation(project(":feature:parent-hub"))
    implementation(project(":feature:learning"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:games"))
    implementation(project(":feature:menu"))
    implementation(project(":feature:loading"))

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.functions)
    implementation(libs.firebase.crashlytics)
    implementation(libs.lottie.compose)
    implementation(libs.play.services.auth)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.gson)
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
