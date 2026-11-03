package com.zattsproject.mobile.Model

data class MensagemResponse(
    val mensagem: String? = null,
    val erro: String? = null,
    val usuarioId: String? = null,
    val email: String? = null,
    val questionarioRespondido: Boolean = false
)