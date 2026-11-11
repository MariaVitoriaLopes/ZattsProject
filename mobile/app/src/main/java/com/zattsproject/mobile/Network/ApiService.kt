package com.zattsproject.mobile.Network

import com.zattsproject.mobile.Model.Cadastro
import com.zattsproject.mobile.Model.Login
import com.zattsproject.mobile.Model.AutenticacaoResponse
import com.zattsproject.mobile.Model.MensagemResponse
import com.zattsproject.mobile.Model.Questionario
import com.zattsproject.mobile.Model.Conta
import com.zattsproject.mobile.Model.DashboardDTO

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Header
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded

interface ApiService {

    // DASHBOARD MOBILE (Integração com DashboardMobileApiController)
    @GET("api/v1/mobile/dashboard/resumo")
    suspend fun obterDadosDashboard(
        @Header("usuarioId") usuarioId: String
    ): Response<DashboardDTO>

    // CADASTRO
    @POST("api/mobile/auth/cadastro")
    suspend fun cadastrar(
        @Body cadastro: Cadastro
    ): Response<MensagemResponse>

    // LOGIN
    @POST("api/mobile/auth/login")
    suspend fun login(
        @Body login: Login
    ): Response<AutenticacaoResponse>

    // VERIFICAR SESSÃO
    @GET("api/mobile/auth/sessao")
    suspend fun verificarSessao(): Response<AutenticacaoResponse>

    // LOGOUT
    @POST("api/mobile/auth/logout")
    suspend fun logout(): Response<MensagemResponse>

    @DELETE("api/mobile/auth/conta")
    suspend fun excluirConta(): Response<MensagemResponse>

    // RECUPERAR SENHA
    @FormUrlEncoded
    @POST("api/recuperarSenha")
    suspend fun recuperarSenha(
        @Field("email") email: String
    ): Response<Unit>

    // SALVAR QUESTIONÁRIO
    @POST("api/mobile/questionario")
    suspend fun salvarQuestionario(
        @Body questionario: Questionario
    ): Response<MensagemResponse>

    @GET("api/mobile/conta")
    suspend fun buscarConta(): Response<Conta>

    @PUT("api/mobile/conta")
    suspend fun atualizarConta(
        @Body conta: Conta
    ): Response<Conta>
}