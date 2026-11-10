package com.zattsproject.mobile.Model

data class Notificacao(
    val id: String,
    val categoria: String,
    val titulo: String,
    val mensagem: String? = null,
    val criadaEm: String,
    val lida: Boolean = false,
    val alertaId: String? = null
)