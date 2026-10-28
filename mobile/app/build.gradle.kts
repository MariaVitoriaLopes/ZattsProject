import java.io.File
import java.util.Properties

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
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")

    // Jackson ou Gson para converter o JSON recebido num objeto Kotlin
    implementation("com.google.code.gson:gson:2.10.1")
    // Coroutines integradas ao ciclo de vida das Activities
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}


val sdkPathAdb = providers.environmentVariable("ANDROID_HOME")
    .orElse(providers.environmentVariable("ANDROID_SDK_ROOT"))
    .orElse(
        providers.fileContents(
            rootProject.layout.projectDirectory.file("local.properties")
        ).asText.map { conteudo ->
            Properties().apply {
                load(conteudo.reader())
            }.getProperty("sdk.dir")
        }
    )

tasks.register("configurarAdbReverse") {
    group = "development"
    description = "Configura ADB reverse para todos os dispositivos"

    val caminhoSdk = sdkPathAdb

    doLast {
        val sdkPath = caminhoSdk.orNull
            ?: throw GradleException(
                "Android SDK não encontrado."
            )

        val adb = File(
            sdkPath,
            "platform-tools/adb.exe"
        )

        if (!adb.exists()) {
            throw GradleException(
                "ADB não encontrado: ${adb.absolutePath}"
            )
        }

        val lista = ProcessBuilder(
            adb.absolutePath,
            "devices"
        )
            .redirectErrorStream(true)
            .start()

        val saida = lista.inputStream
            .bufferedReader()
            .readText()

        if (lista.waitFor() != 0) {
            throw GradleException(
                "Erro ao listar dispositivos: $saida"
            )
        }

        val dispositivos = saida
            .lines()
            .filter { it.endsWith("\tdevice") }
            .map { it.substringBefore("\t") }

        if (dispositivos.isEmpty()) {
            println("Nenhum dispositivo Android conectado.")
        }

        dispositivos.forEach { dispositivo ->
            val processo = ProcessBuilder(
                adb.absolutePath,
                "-s",
                dispositivo,
                "reverse",
                "tcp:8081",
                "tcp:8081"
            )
                .inheritIO()
                .start()

            if (processo.waitFor() != 0) {
                throw GradleException(
                    "Erro ao configurar: $dispositivo"
                )
            }

            println(
                "ADB reverse configurado: $dispositivo"
            )
        }
    }
}

tasks.matching {
    it.name == "assembleDebug"
}.configureEach {
    finalizedBy("configurarAdbReverse")
}
