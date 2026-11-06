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

        // 2. Consumo do dia atual
        List<Consumo> consumosHoje = consumoRepository.findByUsuarioIdAndDataHoraBetween(usuarioId, inicioHoje, fimHoje);
        double consumoTotalHojeKwh = consumosHoje.stream()
                .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0)
                .sum();

        double custoDiarioDiretoReais = calculadoraTarifaService.calcularCustoDiretoAFerroEFogo(consumoTotalHojeKwh);
        double consumoProjetadoMesKwh = consumoTotalHojeKwh > 0 ? (consumoTotalHojeKwh * 30) : 0.0;
        double valorContaEstimadoMes = calculadoraTarifaService.calcularEstimativaOficialContaMensal(consumoProjetadoMesKwh, bandeira);

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

        // 4. GRÁFICO 2: Consumo Diário Hora a Hora (Do início do dia até a hora atual)
        List<DashboardDTO.PontoGraficoHora> pontosHora = new ArrayList<>();
        for (int hora = 0; hora <= agora.getHour(); hora++) {
            LocalDateTime inicioHora = LocalDate.now().atTime(hora, 0, 0);
            LocalDateTime fimHora = LocalDate.now().atTime(hora, 59, 59);

            List<Consumo> consumosDaHora = consumoRepository.findByUsuarioIdAndDataHoraBetween(usuarioId, inicioHora, fimHora);
            double consumoHoraKwh = consumosDaHora.stream()
                    .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0)
                    .sum();

            pontosHora.add(new DashboardDTO.PontoGraficoHora(hora + "h", Math.round(consumoHoraKwh * 100.0) / 100.0));
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
            if (i == 0 && consumoKwhMes == 0.0) {
                consumoKwhMes = consumoProjetadoMesKwh;
                valorContaMes = valorContaEstimadoMes;
            } else {
                valorContaMes = calculadoraTarifaService.calcularEstimativaOficialContaMensal(consumoKwhMes, bandeira);
            }

            resumoComparativoKwh.add(new DashboardDTO.PontoGraficoMes(nomeMes, Math.round(consumoKwhMes * 10.0) / 10.0));
            historicoContasReais.add(new DashboardDTO.PontoGraficoMes(nomeMes, Math.round(valorContaMes * 100.0) / 100.0));
        }

        double metaLimiteKwh = config.getMetaConsumoKwh() != null ? config.getMetaConsumoKwh() : 250.0;
        double percentualMeta = metaLimiteKwh > 0 ? (consumoTotalHojeKwh / metaLimiteKwh) * 100 : 0.0;

        return DashboardDTO.builder()
                .consumoTotalDiarioKwh(Math.round(consumoTotalHojeKwh * 10.0) / 10.0)
                .variacaoDiariaPorcentagem(8.0)
                .custoDiarioReais(Math.round(custoDiarioDiretoReais * 100.0) / 100.0)
                .statusCustoDiario("Custo direto proporcional em R$")
                .metaGastoKwh(metaLimiteKwh)
                .percentualMetaAtingida(Math.round(percentualMeta * 10.0) / 10.0)
                .valorContaEstimadoMesReais(Math.round(valorContaEstimadoMes * 100.0) / 100.0)
                .statusEstimativaConta("Estimativa oficial CPFL (com impostos e CIP)")
                .estaDentroDaMeta(consumoTotalHojeKwh <= metaLimiteKwh)
                .mensagemStatusMeta(consumoTotalHojeKwh <= metaLimiteKwh ? "Você está dentro da sua meta estipulada!" : "Atenção: Meta estipulada ultrapassada!")
                .consumoTempoReal(pontosMinuto)          // Gráfico 1: Minutos
                .consumoDiarioHoras(pontosHora)           // Gráfico 2: Horas
                .resumoComparativoKwh(resumoComparativoKwh)
                .valorContaEstimadoReais(historicoContasReais) // Gráfico 3: Meses R$
                .metaConsumoMensalAtualKwh(Math.round(consumoTotalHojeKwh * 10.0) / 10.0)
                .metaConsumoMensalLimiteKwh(metaLimiteKwh)
                .build();
    }
}