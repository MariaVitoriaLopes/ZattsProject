package com.zattsproject.mobile.Model

data class ConfiguracaoNotificacoes(
    val notifGerais: Boolean = true,
    val notifConta: Boolean = true,
    val notifConsumo: Boolean = true,
    val notifMeta: Boolean = true,
    val notifAlerta: Boolean = true
)