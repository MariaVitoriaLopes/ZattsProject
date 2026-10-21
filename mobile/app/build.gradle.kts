
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.zattsproject.mobile"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.zattsproject.mobile"
        minSdk = 24
        targetSdk = 37

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }


    buildTypes {
        debug {
            buildConfigField(
                "String",
                "BASE_URL",
                "\"http://127.0.0.1:8081/\""
            )
        }

        release {
            optimization {
                enable = false
            }

            buildConfigField(
                "String",
                "BASE_URL",
                "\"http://127.0.0.1:8081/\""
            )
        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Coroutines integradas ao ciclo de vida das Activities
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
