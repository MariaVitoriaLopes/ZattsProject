package com.zattsproject.mobile.Model

data class SessaoDispositivo(
    val id: String,
    val nome: String,
    val tipo: String,
    val conectadoEm: String,
    val atual: Boolean
)

data class DesconectarSessaoResponse(
    val atual: Boolean
)