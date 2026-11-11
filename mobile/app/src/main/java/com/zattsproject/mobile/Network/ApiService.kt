package com.zattsproject.mobile.Network

import com.zattsproject.mobile.Model.Cadastro
import com.zattsproject.mobile.Model.Login
import com.zattsproject.mobile.Model.AutenticacaoResponse
import com.zattsproject.mobile.Model.MensagemResponse
import com.zattsproject.mobile.Model.Questionario
import com.zattsproject.mobile.Model.Conta
import com.zattsproject.mobile.Model.ConsumoDiario
import com.zattsproject.mobile.Model.Notificacao
import com.zattsproject.mobile.Model.Alerta
import com.zattsproject.mobile.Model.ConfiguracaoNotificacoes
import com.zattsproject.mobile.Model.SessaoDispositivo
import com.zattsproject.mobile.Model.DesconectarSessaoResponse
import com.zattsproject.mobile.Model.PrivacidadeConfiguracao
import com.zattsproject.mobile.Model.ApagarHistoricoResponse
import com.zattsproject.mobile.Model.AjudaContatoRequest
import com.zattsproject.mobile.Model.AjudaContatoResponse
import okhttp3.ResponseBody
import retrofit2.http.Streaming
import retrofit2.http.Path
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
import retrofit2.http.Query

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

    // HISTÓRICO
    @GET("api/consumos/diario")
    suspend fun buscarHistoricoDiario(
        @Query("inicio") inicio: String,
        @Query("fim") fim: String
    ): Response<List<ConsumoDiario>>

    // NOTIFICAÇÕES
    @GET("notificacoes/api")
    suspend fun buscarNotificacoes(
        @Query("pagina") pagina: Int = 0,
        @Query("tamanho") tamanho: Int = 50
    ): Response<List<Notificacao>>

    // ALERTAS
    @GET("api/mobile/alertas")
    suspend fun buscarAlertas(): Response<List<Alerta>>

    @POST("api/mobile/alertas/{id}/ativar")
    suspend fun ativarAlerta(
        @Path("id") id: String,
        @Query("ativo") ativo: Boolean
    ): Response<Unit>

    @DELETE("api/mobile/alertas/{id}")
    suspend fun excluirAlerta(
        @Path("id") id: String
    ): Response<Unit>

    @POST("api/mobile/alertas")
    suspend fun criarAlerta(
        @Body alerta: Alerta
    ): Response<Unit>

    @PUT("api/mobile/alertas/{id}")
    suspend fun editarAlerta(
        @Path("id") id: String,
        @Body alerta: Alerta
    ): Response<Unit>

    // CONFIGURAÇÕES - NOTIFICAÇÕES
    @GET("api/mobile/configuracoes/notificacoes")
    suspend fun buscarConfiguracaoNotificacoes():
            Response<ConfiguracaoNotificacoes>

    @PUT("api/mobile/configuracoes/notificacoes")
    suspend fun salvarConfiguracaoNotificacoes(
        @Body configuracao: ConfiguracaoNotificacoes
    ): Response<ConfiguracaoNotificacoes>

    // CONFIGURAÇÕES - DISPOSITIVOS
    @GET("api/mobile/configuracoes/dispositivos")
    suspend fun buscarDispositivos():
            Response<List<SessaoDispositivo>>

    @POST("api/mobile/configuracoes/dispositivos/{id}/desconectar")
    suspend fun desconectarDispositivo(
        @Path("id") id: String
    ): Response<DesconectarSessaoResponse>

    // CONFIGURAÇÕES - PRIVACIDADE
    @GET("api/mobile/configuracoes/privacidade")
    suspend fun buscarPrivacidade():
            Response<PrivacidadeConfiguracao>

    @PUT("api/mobile/configuracoes/privacidade")
    suspend fun salvarPrivacidade(
        @Body configuracao: PrivacidadeConfiguracao
    ): Response<PrivacidadeConfiguracao>

    @Streaming
    @GET("api/mobile/configuracoes/privacidade/exportar")
    suspend fun exportarDados():
            Response<ResponseBody>

    @POST("api/mobile/configuracoes/privacidade/apagar-historico")
    suspend fun apagarHistorico():
            Response<ApagarHistoricoResponse>

    // CONFIGURAÇÕES - AJUDA E SUPORTE
    @POST("api/mobile/configuracoes/ajuda/contato")
    suspend fun enviarContatoSuporte(
        @Body dados: AjudaContatoRequest
    ): Response<AjudaContatoResponse>
}
