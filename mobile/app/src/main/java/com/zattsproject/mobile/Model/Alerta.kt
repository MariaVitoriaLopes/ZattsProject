package com.zattsproject.mobile.Model

data class Alerta(
    val id: String? = null,
    val nome: String? = null,
    val tipo: String? = null,
    val percentualLimite: Double? = null,
    val valorLimite: Double? = null,
    val ativo: Boolean? = true,
    val icone: String? = null,
    val categoria: String? = null,
    val canalPush: Boolean? = true,
    val canalEmail: Boolean? = true,
    val canalApp: Boolean? = true,
    val lembrete: String? = null,
    val repetir: String? = null
)