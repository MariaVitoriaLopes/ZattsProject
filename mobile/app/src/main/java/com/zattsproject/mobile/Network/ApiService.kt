package com.zattsproject.mobile.Network

import com.zattsproject.mobile.Model.Cadastro
import com.zattsproject.mobile.Model.Login
import com.zattsproject.mobile.Model.MensagemResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/mobile/auth/cadastro")
    suspend fun cadastrar(
        @Body cadastro: Cadastro
    ): Response<MensagemResponse>

    @POST("api/mobile/auth/login")
    suspend fun login(
        @Body login: Login
    ): Response<MensagemResponse>
}