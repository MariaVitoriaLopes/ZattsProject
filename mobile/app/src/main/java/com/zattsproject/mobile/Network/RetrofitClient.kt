package com.zattsproject.mobile.Network

import android.content.Context
import android.util.Log
import com.zattsproject.mobile.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private lateinit var appContext: Context

    fun inicializar(context: Context) {
        appContext = context.applicationContext
    }

    private val cookieJar: PersistentCookieJar by lazy {
        PersistentCookieJar(appContext)
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
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
        instance.create(ApiService::class.java)
    }

    fun limparCookies() {
        cookieJar.limparCookies()
    }
}
