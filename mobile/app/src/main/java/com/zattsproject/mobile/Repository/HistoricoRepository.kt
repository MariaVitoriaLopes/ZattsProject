package com.zattsproject.mobile.Repository

import com.zattsproject.mobile.Model.ConsumoDiario
import com.zattsproject.mobile.Network.RetrofitClient
import retrofit2.Response

class HistoricoRepository {

    private val api = RetrofitClient.apiService

    suspend fun buscarTotaisDiarios(
        inicio: String,
        fim: String
    ): Response<List<ConsumoDiario>> {

        return api.buscarHistoricoDiario(
            inicio = inicio,
            fim = fim
        )
    }
}