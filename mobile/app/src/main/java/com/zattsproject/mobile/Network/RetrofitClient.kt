package com.zattsproject.mobile.Network

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log

import com.zattsproject.mobile.BuildConfig
import com.zattsproject.mobile.LoginActivity

import java.util.concurrent.atomic.AtomicBoolean

import okhttp3.OkHttpClient

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private lateinit var appContext: Context

    private val redirecionandoParaLogin =
        AtomicBoolean(false)

    fun inicializar(context: Context) {

        appContext =
            context.applicationContext
    }

    private val cookieJar: PersistentCookieJar by lazy {

        PersistentCookieJar(appContext)
    }

    private val httpClient: OkHttpClient by lazy {

        OkHttpClient.Builder()

            .cookieJar(cookieJar)

            .addInterceptor { chain ->

                val requisicao =
                    chain.request()
                        .newBuilder()
                        .header(
                            "X-Dispositivo",
                            nomeDispositivo()
                        )
                        .build()

                val resposta =
                    chain.proceed(requisicao)

                if (
                    resposta.code == 401 &&
                    deveEncerrarSessao(
                        requisicao.url.encodedPath
                    )
                ) {

                    encerrarSessaoLocal()
                }

                resposta
            }

            .build()
    }

    val instance: Retrofit by lazy {

        Log.d(
            "ZATTS_API",
            "BASE_URL: ${BuildConfig.BASE_URL}"
        )

        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(httpClient)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }

    val apiService: ApiService by lazy {

        instance.create(
            ApiService::class.java
        )
    }

    fun limparCookies() {

        cookieJar.limparCookies()
    }

    fun marcarSessaoAutenticada() {

        redirecionandoParaLogin.set(false)
    }

    private fun deveEncerrarSessao(
        caminho: String
    ): Boolean {

        if (
            caminho == "/api/mobile/auth/login" ||
            caminho == "/api/mobile/auth/cadastro" ||
            caminho == "/api/mobile/auth/sessao" ||
            caminho.startsWith(
                "/api/recuperarSenha"
            )
        ) {

            return false
        }

        return true
    }

    private fun encerrarSessaoLocal() {

        if (
            !redirecionandoParaLogin
                .compareAndSet(false, true)
        ) {

            return
        }

        limparCookies()

        Handler(
            Looper.getMainLooper()
        ).post {

            val intent = Intent(
                appContext,
                LoginActivity::class.java
            )

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            appContext.startActivity(intent)
        }
    }

    private fun nomeDispositivo(): String {

        val fabricante =
            Build.MANUFACTURER.trim()

        val modelo =
            Build.MODEL.trim()

        if (
            fabricante.isBlank() &&
            modelo.isBlank()
        ) {

            return "Android"
        }

        if (fabricante.isBlank()) {

            return modelo
        }

        if (modelo.isBlank()) {

            return fabricante
        }

        if (
            modelo.startsWith(
                fabricante,
                ignoreCase = true
            )
        ) {

            return modelo
        }

        return "$fabricante $modelo"
    }
}