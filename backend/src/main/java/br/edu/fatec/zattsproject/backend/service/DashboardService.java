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
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    private static final ZoneId ZONE_SP = ZoneId.of("America/Sao_Paulo");
    private static final ZoneId ZONE_UTC = ZoneId.of("UTC");

    public DashboardDTO obterDadosDashboard(String usuarioId) {
        // Momento atual no fuso de São Paulo
        ZonedDateTime agoraSP = ZonedDateTime.now(ZONE_SP);

        // Intervalo do dia atual em São Paulo convertido para UTC
        LocalDateTime inicioHoje = agoraSP.toLocalDate().atStartOfDay(ZONE_SP)
                .withZoneSameInstant(ZONE_UTC).toLocalDateTime();
        LocalDateTime fimHoje = agoraSP.toLocalDate().atTime(23, 59, 59).atZone(ZONE_SP)
                .withZoneSameInstant(ZONE_UTC).toLocalDateTime();

        // 1. Configuração do usuário dinâmica vinda do MongoDB
        Configuracao config = configuracaoRepository.findByUsuarioId(usuarioId)
                .orElse(Configuracao.builder()
                        .usuarioId(usuarioId)
                        .metaConsumoKwh(250.0) // Padrão se o usuário nunca definiu
                        .metaGastoReais(300.0)
                        .bandeiraAtual(BandeiraTarifaria.VERDE)
                        .build());

        BandeiraTarifaria bandeira = config.getBandeiraAtual() != null ?
                config.getBandeiraAtual() : BandeiraTarifaria.VERDE;

        // Meta escolhida pelo usuário (dinâmica)
        double metaLimiteKwh = (config.getMetaConsumoKwh() != null && config.getMetaConsumoKwh() > 0)
                ? config.getMetaConsumoKwh()
                : 250.0;

        // 2. CONSUMO ACUMULADO DO MÊS ATUAL
        LocalDateTime inicioMesAtual = YearMonth.from(agoraSP).atDay(1).atStartOfDay(ZONE_SP)
                .withZoneSameInstant(ZONE_UTC).toLocalDateTime();

        List<Consumo> consumosMesAtual = consumoRepository.findByUsuarioIdAndDataHoraBetween(
                usuarioId, inicioMesAtual, agoraSP.withZoneSameInstant(ZONE_UTC).toLocalDateTime());

        double consumoAcumuladoMesKwh = consumosMesAtual.stream()
                .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0)
                .sum();

        // CONSUMO DO DIA ATUAL
        List<Consumo> consumosHoje = consumoRepository.findByUsuarioIdAndDataHoraBetween(usuarioId, inicioHoje, fimHoje);
        double consumoTotalHojeKwh = consumosHoje.stream()
                .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0)
                .sum();

        // Custo financeiro direto de hoje
        double custoDiarioDiretoReais = calculadoraTarifaService.calcularCustoDiretoAFerroEFogo(consumoTotalHojeKwh);

        // CONTA DE LUZ ACUMULATIVA DO MÊS
        double valorContaAcumuladoMesReais = calculadoraTarifaService.calcularEstimativaOficialContaMensal(
                consumoAcumuladoMesKwh,
                bandeira
        );

        // 3. GRÁFICO 1: Consumo Minuto a Minuto (Últimos 15 minutos em tempo real)
        List<DashboardDTO.PontoGraficoMinuto> pontosMinuto = new ArrayList<>();
        DateTimeFormatter fmtMinuto = DateTimeFormatter.ofPattern("HH:mm");

        for (int i = 14; i >= 0; i--) {
            ZonedDateTime minInicioZoned = agoraSP.minusMinutes(i).withSecond(0).withNano(0);
            LocalDateTime minInicioUtc = minInicioZoned.withZoneSameInstant(ZONE_UTC).toLocalDateTime();
            LocalDateTime minFimUtc = minInicioZoned.plusSeconds(59).withZoneSameInstant(ZONE_UTC).toLocalDateTime();

            List<Consumo> consumosMinuto = consumoRepository.findByUsuarioIdAndDataHoraBetween(usuarioId, minInicioUtc, minFimUtc);
            double kwhMinuto = consumosMinuto.stream()
                    .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0)
                    .sum();

            pontosMinuto.add(new DashboardDTO.PontoGraficoMinuto(minInicioZoned.format(fmtMinuto), Math.round(kwhMinuto * 1000.0) / 1000.0));
        }

        // 4. GRÁFICO 2: Consumo Hora a Hora (Janela móvel das últimas 12 horas corridas)
        List<DashboardDTO.PontoGraficoHora> pontosHora = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            ZonedDateTime horaAlvoZoned = agoraSP.minusHours(i);
            LocalDateTime inicioHoraUtc = horaAlvoZoned.withMinute(0).withSecond(0).withZoneSameInstant(ZONE_UTC).toLocalDateTime();
            LocalDateTime fimHoraUtc = horaAlvoZoned.withMinute(59).withSecond(59).withZoneSameInstant(ZONE_UTC).toLocalDateTime();

            List<Consumo> consumosDaHora = consumoRepository.findByUsuarioIdAndDataHoraBetween(usuarioId, inicioHoraUtc, fimHoraUtc);
            double consumoHoraKwh = consumosDaHora.stream()
                    .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0)
                    .sum();

            pontosHora.add(new DashboardDTO.PontoGraficoHora(horaAlvoZoned.getHour() + "h", Math.round(consumoHoraKwh * 100.0) / 100.0));
        }

        // 5. GRÁFICO 3: Histórico dos Últimos 6 Meses em R$
        List<DashboardDTO.PontoGraficoMes> resumoComparativoKwh = new ArrayList<>();
        List<DashboardDTO.PontoGraficoMes> historicoContasReais = new ArrayList<>();

        Locale brasil = Locale.forLanguageTag("pt-BR");
        YearMonth mesAtual = YearMonth.from(agoraSP);

        for (int i = 5; i >= 0; i--) {
            YearMonth mesAlvo = mesAtual.minusMonths(i);
            LocalDateTime inicioMesUtc = mesAlvo.atDay(1).atStartOfDay(ZONE_SP).withZoneSameInstant(ZONE_UTC).toLocalDateTime();
            LocalDateTime fimMesUtc = mesAlvo.atEndOfMonth().atTime(23, 59, 59).atZone(ZONE_SP).withZoneSameInstant(ZONE_UTC).toLocalDateTime();

            String nomeMes = mesAlvo.getMonth().getDisplayName(TextStyle.SHORT, brasil);
            nomeMes = nomeMes.substring(0, 1).toUpperCase() + nomeMes.substring(1).replace(".", "");

            List<Consumo> consumosDoMes = consumoRepository.findByUsuarioIdAndDataHoraBetween(usuarioId, inicioMesUtc, fimMesUtc);
            double consumoKwhMes = consumosDoMes.stream()
                    .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0)
                    .sum();

            double valorContaMes;
            if (i == 0) {
                valorContaMes = valorContaAcumuladoMesReais;
            } else {
                valorContaMes = calculadoraTarifaService.calcularEstimativaOficialContaMensal(consumoKwhMes, bandeira);
            }

            resumoComparativoKwh.add(new DashboardDTO.PontoGraficoMes(nomeMes, Math.round(consumoKwhMes * 10.0) / 10.0));
            historicoContasReais.add(new DashboardDTO.PontoGraficoMes(nomeMes, Math.round(valorContaMes * 100.0) / 100.0));
        }

        double percentualMeta = metaLimiteKwh > 0 ? (consumoAcumuladoMesKwh / metaLimiteKwh) * 100 : 0.0;

        return DashboardDTO.builder()
                .consumoTotalDiarioKwh(Math.round(consumoTotalHojeKwh * 100.0) / 100.0)
                .variacaoDiariaPorcentagem(8.0)
                .custoDiarioReais(Math.round(custoDiarioDiretoReais * 100.0) / 100.0)
                .statusCustoDiario("Custo direto proporcional em R$")
                .metaGastoKwh(metaLimiteKwh)
                .percentualMetaAtingida(Math.round(percentualMeta * 10.0) / 10.0)

                .valorContaEstimadoMesReais(Math.round(valorContaAcumuladoMesReais * 100.0) / 100.0)
                .statusEstimativaConta("Estimativa oficial CPFL (com impostos e CIP)")

                .estaDentroDaMeta(consumoAcumuladoMesKwh <= metaLimiteKwh)
                .mensagemStatusMeta(consumoAcumuladoMesKwh <= metaLimiteKwh ? "Você está dentro da sua meta estipulada!" : "Atenção: Meta estipulada ultrapassada!")
                .consumoTempoReal(pontosMinuto)
                .consumoDiarioHoras(pontosHora)
                .resumoComparativoKwh(resumoComparativoKwh)
                .valorContaEstimadoReais(historicoContasReais)

                .metaConsumoMensalAtualKwh(Math.round(consumoAcumuladoMesKwh * 10.0) / 10.0)
                .metaConsumoMensalLimiteKwh(metaLimiteKwh)
                .build();
    }
}