package com.zattsproject.mobile

import android.app.Application
import android.util.Log
import com.zattsproject.mobile.Network.RetrofitClient

class ZattsApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        RetrofitClient.inicializar(this)

        Log.d("ZATTS_INIT", "RetrofitClient inicializado!")
    }
}