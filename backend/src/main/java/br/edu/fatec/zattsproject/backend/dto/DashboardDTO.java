package br.edu.fatec.zattsproject.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    private Double consumoTotalDiarioKwh;
    private Double variacaoDiariaPorcentagem;
    private Double custoDiarioReais;
    private String statusCustoDiario;
    private Double metaGastoKwh;
    private Double percentualMetaAtingida;
    private Double valorContaEstimadoMesReais;
    private String statusEstimativaConta;
    private Boolean estaDentroDaMeta;
    private String mensagemStatusMeta;

    // Listas dos 3 Gráficos do Figma
    private List<PontoGraficoMinuto> consumoTempoReal;    // Gráfico 1: Minuto a Minuto (Linha)
    private List<PontoGraficoHora> consumoDiarioHoras;     // Gráfico 2: Hora a Hora (Barras)
    private List<PontoGraficoMes> resumoComparativoKwh;   // Histórico Mensal em kWh
    private List<PontoGraficoMes> valorContaEstimadoReais; // Gráfico 3: Histórico Mensal em R$ (Barras)

    private Double metaConsumoMensalAtualKwh;
    private Double metaConsumoMensalLimiteKwh;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PontoGraficoMinuto {
        private String hora; // Ex: "15:00", "15:01"
        private Double consumoKwh;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PontoGraficoHora {
        private String hora; // Ex: "5h", "6h", "7h"
        private Double consumoKwh;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PontoGraficoMes {
        private String mes; // Ex: "Jul", "Ago", "Set"
        private Double valor;
    }
}