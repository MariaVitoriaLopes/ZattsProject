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

    //  Consumo Total Diário
    private Double consumoTotalDiarioKwh;
    private Double variacaoDiariaPorcentagem;

    //sem taxas/impostos
    private Double custoDiarioReais;
    private String statusCustoDiario;            // Ex: "Custo direto de consumo"

    // --- Card 3: Meta de Gasto (kWh) ---
    private Double metaGastoKwh;
    private Double percentualMetaAtingida;

    // ) ---
    private Double valorContaEstimadoMesReais;   // Ex: R$ 290.50 (Calculado com CPFL + ICMS + PIS/COFINS + Bandeira + CIP)
    private String statusEstimativaConta;        // Ex: "Projeção mensal com impostos e CIP (PG)"

    // --- Banner Status Topo ---
    private Boolean estaDentroDaMeta;            // Ex: true ("Você está dentro da sua meta!")
    private String mensagemStatusMeta;           // Ex: "Baseado na meta mensal definida"

    // --- Gráfico 1: Consumo de Energia em Tempo Real (kWh por hora) ---
    private List<PontoGraficoHora> consumoTempoReal;

    // --- Meta de Consumo Mensal (Barra de Progresso) ---
    private Double metaConsumoMensalAtualKwh;    // Ex: 90 kWh
    private Double metaConsumoMensalLimiteKwh;   // Ex: 250 kWh

    // --- Gráfico 2: Resumo Comparativo (kWh dos últimos 6 meses) ---
    private List<PontoGraficoMes> resumoComparativoKwh;

    // --- Gráfico 3: Histórico e Estimativa de Contas (R$ dos últimos meses) ---
    private List<PontoGraficoMes> valorContaEstimadoReais;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PontoGraficoHora {
        private String hora;      // Ex: "5h", "6h", "15h"
        private Double consumoKwh;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PontoGraficoMes {
        private String mes;    // Ex: "Abril", "Maio", "Setembro"
        private Double valor;  // Valor em kWh ou R$
    }
}