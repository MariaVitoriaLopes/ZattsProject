package com.zattsproject.mobile.Repository

import com.zattsproject.mobile.Model.Questionario
import com.zattsproject.mobile.Model.MensagemResponse
import com.zattsproject.mobile.Network.RetrofitClient

import retrofit2.Response

class QuestionarioRepository {

    private val api = RetrofitClient.apiService

    suspend fun salvar(
        questionario: Questionario
    ): Response<MensagemResponse> {

        return api.salvarQuestionario(questionario)
    }
}
