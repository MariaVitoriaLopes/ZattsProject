
package com.zattsproject.mobile.Repository

import com.zattsproject.mobile.Model.Cadastro
import com.zattsproject.mobile.Model.Login
import com.zattsproject.mobile.Model.MensagemResponse
import com.zattsproject.mobile.Network.RetrofitClient
import retrofit2.Response

class UsuarioRepository {

    private val api = RetrofitClient.apiService

    suspend fun cadastrar(
        cadastro: Cadastro
    ): Response<MensagemResponse> {
        return api.cadastrar(cadastro)
    }

    suspend fun login(
        login: Login
    ): Response<MensagemResponse> {
        return api.login(login)
    }
}
