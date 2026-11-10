package com.zattsproject.mobile.Repository

import com.zattsproject.mobile.Model.Notificacao
import com.zattsproject.mobile.Network.RetrofitClient
import retrofit2.Response

class NotificacaoRepository {

    private val api =
        RetrofitClient.apiService

    suspend fun listar(): Response<List<Notificacao>> {
        return api.buscarNotificacoes()
    }
}