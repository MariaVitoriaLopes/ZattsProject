
package com.zattsproject.mobile.Repository

import com.zattsproject.mobile.Model.Cadastro
import com.zattsproject.mobile.Model.Login
import com.zattsproject.mobile.Model.MensagemResponse
import com.zattsproject.mobile.Model.AutenticacaoResponse
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
    ): Response<AutenticacaoResponse> {

        val resposta = api.login(login)

        if (resposta.isSuccessful) {

            RetrofitClient.marcarSessaoAutenticada()

            resposta.body()?.usuarioId?.let { id ->
                if (id.isNotBlank()) {
                    RetrofitClient.salvarUsuarioId(id)
                }
            }
        }

        return resposta
    }

    suspend fun verificarSessao(
    ): Response<AutenticacaoResponse> {

        val resposta = api.verificarSessao()

        if (resposta.isSuccessful) {

            resposta.body()?.usuarioId?.let { id ->
                if (id.isNotBlank()) {
                    RetrofitClient.salvarUsuarioId(id)
                }
            }
        }

        return resposta
    }

    suspend fun logout(): Response<MensagemResponse> {

        val resposta = api.logout()

        if (resposta.isSuccessful) {
            RetrofitClient.limparCookies()
            RetrofitClient.limparUsuarioId()
        }

        return resposta
    }
}
