package com.zattsproject.mobile.Model

data class PrivacidadeConfiguracao(
    val compartilharDadosUso: Boolean = true,
    val recomendacoesPersonalizadas: Boolean = true,
    val comparacaoResidencias: Boolean = false
)

data class ApagarHistoricoResponse(
    val mensagem: String? = null,
    val registrosExcluidos: Long = 0
)