package com.zattsproject.mobile.Model

data class AjudaContatoRequest(
    val assunto: String,
    val mensagem: String
)

data class AjudaContatoResponse(
    val mensagem: String? = null,
    val erro: String? = null
)