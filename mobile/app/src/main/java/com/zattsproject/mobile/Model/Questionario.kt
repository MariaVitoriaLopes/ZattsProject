
package com.zattsproject.mobile.Model

data class Questionario(

    val qtdPessoas: Int,
    val concessionaria: String,
    val vencimentoUltimaConta: String,

    val valorMes1: String,
    val valorMes2: String?,
    val valorMes3: String?,

    val consumoMes1: String,
    val consumoMes2: String?,
    val consumoMes3: String?,

    val definirMeta: String,
    val tipoMeta: String?,
    val valorMeta: String?
)
