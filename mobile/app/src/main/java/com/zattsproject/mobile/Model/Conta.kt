package com.zattsproject.mobile.Model

data class Conta(
    val nome: String?,
    val email: String?,
    val telefone: String?,
    val fusoHorario: String?,
    val cep: String?,
    val logradouro: String?,
    val numero: String?,
    val complemento: String?,
    val bairro: String?,
    val cidade: String?,
    val uf: String?,
    val novaSenha: String? = null,
    val confirmarSenha: String? = null
)