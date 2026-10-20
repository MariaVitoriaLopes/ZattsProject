
package com.zattsproject.mobile.Network

import android.util.Log
import com.zattsproject.mobile.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    val instance: Retrofit by lazy {

        Log.d(
            "ZATTS_API",
            "BASE_URL: ${BuildConfig.BASE_URL}"
        )

        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        instance.create(ApiService::class.java)
    }
}
