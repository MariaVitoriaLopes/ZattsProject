package com.zattsproject.mobile.Model

data class Consumo(
    val id: String? = null,
    val dispositivoId: String? = null,
    val correnteAmperes: Double = 0.0,
    val voltagem: Double = 0.0,
    val potenciaWatts: Double = 0.0,
    val dataHora: String? = null
)