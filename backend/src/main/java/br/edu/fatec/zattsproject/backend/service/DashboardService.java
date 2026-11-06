package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.dto.DashboardDTO;
import br.edu.fatec.zattsproject.backend.model.Configuracao;
import br.edu.fatec.zattsproject.backend.model.Consumo;
import br.edu.fatec.zattsproject.backend.repository.ConfiguracaoRepository;
import br.edu.fatec.zattsproject.backend.repository.ConsumoRepository;
import br.edu.fatec.zattsproject.backend.service.CalculadoraTarifaService.BandeiraTarifaria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ConsumoRepository consumoRepository;
    private final ConfiguracaoRepository configuracaoRepository;
    private final CalculadoraTarifaService calculadoraTarifaService;

    public DashboardDTO obterDadosDashboard(String usuarioId) {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioHoje = LocalDate.now().atStartOfDay();
        LocalDateTime fimHoje = LocalDate.now().atTime(23, 59, 59);

        // 1. Configuração do usuário
        Configuracao config = configuracaoRepository.findByUsuarioId(usuarioId)
                .orElse(Configuracao.builder()
                        .metaConsumoKwh(250.0)
                        .metaGastoReais(300.0)
                        .bandeiraAtual(BandeiraTarifaria.VERDE)
                        .build());

        BandeiraTarifaria bandeira = config.getBandeiraAtual() != null ?
                config.getBandeiraAtual() : BandeiraTarifaria.VERDE;

        // 2. CONSUMO ACUMULADO DO MÊS ATUAL (Do dia 1º até o segundo atual)
        LocalDateTime inicioMesAtual = YearMonth.now().atDay(1).atStartOfDay();
        List<Consumo> consumosMesAtual = consumoRepository.findByUsuarioIdAndDataHoraBetween(usuarioId, inicioMesAtual, agora);

        double consumoAcumuladoMesKwh = consumosMesAtual.stream()
                .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0)
                .sum();

        // CONSUMO DO DIA ATUAL (Das 00:00 de hoje até agora)
        List<Consumo> consumosHoje = consumoRepository.findByUsuarioIdAndDataHoraBetween(usuarioId, inicioHoje, fimHoje);
        double consumoTotalHojeKwh = consumosHoje.stream()
                .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0)
                .sum();

        // Custo financeiro direto somente de hoje
        double custoDiarioDiretoReais = calculadoraTarifaService.calcularCustoDiretoAFerroEFogo(consumoTotalHojeKwh);

        // CONTA DE LUZ ACUMULATIVA (ESTILO FATURA DE CARTÃO DE CRÉDITO)
        // Calcula o valor acumulado em R$ com base no consumo total do mês até o exato momento
        double valorContaAcumuladoMesReais = calculadoraTarifaService.calcularEstimativaOficialContaMensal(
                consumoAcumuladoMesKwh,
                bandeira
        );

        // 3. GRÁFICO 1: Consumo Minuto a Minuto (Últimos 15 minutos em tempo real)
        List<DashboardDTO.PontoGraficoMinuto> pontosMinuto = new ArrayList<>();
        DateTimeFormatter fmtMinuto = DateTimeFormatter.ofPattern("HH:mm");

        for (int i = 14; i >= 0; i--) {
            LocalDateTime minInicio = agora.minusMinutes(i).withSecond(0).withNano(0);
            LocalDateTime minFim = minInicio.plusSeconds(59);

            List<Consumo> consumosMinuto = consumoRepository.findByUsuarioIdAndDataHoraBetween(usuarioId, minInicio, minFim);
            double kwhMinuto = consumosMinuto.stream()
                    .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0)
                    .sum();

            pontosMinuto.add(new DashboardDTO.PontoGraficoMinuto(minInicio.format(fmtMinuto), Math.round(kwhMinuto * 1000.0) / 1000.0));
        }

        // 4. GRÁFICO 2: Consumo Hora a Hora (Janela móvel das últimas 12 horas corridas)
        List<DashboardDTO.PontoGraficoHora> pontosHora = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            LocalDateTime horaAlvo = agora.minusHours(i);
            LocalDateTime inicioHora = horaAlvo.withMinute(0).withSecond(0);
            LocalDateTime fimHora = horaAlvo.withMinute(59).withSecond(59);

            List<Consumo> consumosDaHora = consumoRepository.findByUsuarioIdAndDataHoraBetween(usuarioId, inicioHora, fimHora);
            double consumoHoraKwh = consumosDaHora.stream()
                    .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0)
                    .sum();

            pontosHora.add(new DashboardDTO.PontoGraficoHora(inicioHora.getHour() + "h", Math.round(consumoHoraKwh * 100.0) / 100.0));
        }

        // 5. GRÁFICO 3: Histórico dos Últimos 6 Meses em R$
        List<DashboardDTO.PontoGraficoMes> resumoComparativoKwh = new ArrayList<>();
        List<DashboardDTO.PontoGraficoMes> historicoContasReais = new ArrayList<>();

        Locale brasil = Locale.forLanguageTag("pt-BR");
        YearMonth mesAtual = YearMonth.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth mesAlvo = mesAtual.minusMonths(i);
            LocalDateTime inicioMes = mesAlvo.atDay(1).atStartOfDay();
            LocalDateTime fimMes = mesAlvo.atEndOfMonth().atTime(23, 59, 59);

            String nomeMes = mesAlvo.getMonth().getDisplayName(TextStyle.SHORT, brasil);
            nomeMes = nomeMes.substring(0, 1).toUpperCase() + nomeMes.substring(1).replace(".", "");

            List<Consumo> consumosDoMes = consumoRepository.findByUsuarioIdAndDataHoraBetween(usuarioId, inicioMes, fimMes);
            double consumoKwhMes = consumosDoMes.stream()
                    .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0)
                    .sum();

            double valorContaMes;
            if (i == 0) {
                // Mês corrente exibe a fatura parcial acumulada em tempo real
                valorContaMes = valorContaAcumuladoMesReais;
            } else {
                valorContaMes = calculadoraTarifaService.calcularEstimativaOficialContaMensal(consumoKwhMes, bandeira);
            }

            resumoComparativoKwh.add(new DashboardDTO.PontoGraficoMes(nomeMes, Math.round(consumoKwhMes * 10.0) / 10.0));
            historicoContasReais.add(new DashboardDTO.PontoGraficoMes(nomeMes, Math.round(valorContaMes * 100.0) / 100.0));
        }

        double metaLimiteKwh = config.getMetaConsumoKwh() != null ? config.getMetaConsumoKwh() : 250.0;

        // Percentual calculado sobre o consumo acumulado do mês
        double percentualMeta = metaLimiteKwh > 0 ? (consumoAcumuladoMesKwh / metaLimiteKwh) * 100 : 0.0;

        return DashboardDTO.builder()
                .consumoTotalDiarioKwh(Math.round(consumoTotalHojeKwh * 10.0) / 10.0)
                .variacaoDiariaPorcentagem(8.0)
                .custoDiarioReais(Math.round(custoDiarioDiretoReais * 100.0) / 100.0)
                .statusCustoDiario("Custo direto proporcional em R$")
                .metaGastoKwh(metaLimiteKwh)
                .percentualMetaAtingida(Math.round(percentualMeta * 10.0) / 10.0)

                // FATURA ACUMULATIVA EM TEMPO REAL: atualiza os centavos a cada publicação MQTT
                .valorContaEstimadoMesReais(Math.round(valorContaAcumuladoMesReais * 100.0) / 100.0)
                .statusEstimativaConta("Estimativa oficial CPFL (com impostos e CIP)")

                .estaDentroDaMeta(consumoAcumuladoMesKwh <= metaLimiteKwh)
                .mensagemStatusMeta(consumoAcumuladoMesKwh <= metaLimiteKwh ? "Você está dentro da sua meta estipulada!" : "Atenção: Meta estipulada ultrapassada!")
                .consumoTempoReal(pontosMinuto)
                .consumoDiarioHoras(pontosHora)
                .resumoComparativoKwh(resumoComparativoKwh)
                .valorContaEstimadoReais(historicoContasReais)

                // CONSUMO MENSAL ACUMULATIVO: exibe o somatório total acumulado no mês
                .metaConsumoMensalAtualKwh(Math.round(consumoAcumuladoMesKwh * 10.0) / 10.0)
                .metaConsumoMensalLimiteKwh(metaLimiteKwh)
                .build();
    }
}