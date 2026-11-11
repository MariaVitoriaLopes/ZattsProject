package com.zattsproject.mobile.Model

data class DashboardDTO(
    val consumoTotalDiarioKwh: Double = 0.0,
    val variacaoDiariaPorcentagem: Double = 0.0,
    val custoDiarioReais: Double = 0.0,
    val statusCustoDiario: String? = null,
    val metaGastoKwh: Double = 0.0,
    val percentualMetaAtingida: Double = 0.0,
    val valorContaEstimadoMesReais: Double = 0.0,
    val statusEstimativaConta: String? = null,
    val estaDentroDaMeta: Boolean = true,
    val mensagemStatusMeta: String? = null,
    val metaConsumoMensalAtualKwh: Double = 0.0,
    val metaConsumoMensalLimiteKwh: Double = 0.0,
    val consumoTempoReal: List<PontoGraficoMinuto> = emptyList(),
    val consumoDiarioHoras: List<PontoGraficoHora> = emptyList(),
    val resumoComparativoKwh: List<PontoGraficoMes> = emptyList(),
    val valorContaEstimadoReais: List<PontoGraficoMes> = emptyList()
) {
    data class PontoGraficoMinuto(
        val hora: String = "",
        val consumoKwh: Double = 0.0
    )

    data class PontoGraficoHora(
        val hora: String = "",
        val consumoKwh: Double = 0.0
    )

    data class PontoGraficoMes(
        val mes: String = "",
        val valor: Double = 0.0
    )
}