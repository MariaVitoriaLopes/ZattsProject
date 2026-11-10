package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.dto.ConsumoDiarioDTO;
import br.edu.fatec.zattsproject.backend.dto.DashboardDTO;
import br.edu.fatec.zattsproject.backend.model.Consumo;
import br.edu.fatec.zattsproject.backend.model.ConsumoRealtime;
import br.edu.fatec.zattsproject.backend.repository.ConsumoRepository;
import br.edu.fatec.zattsproject.backend.repository.ConsumoRealtimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumoService {

    private final ConsumoRepository repository;
    private final ConsumoRealtimeRepository realtimeRepository;
    private final ConfiguracaoService configuracaoService;
    private final AlertaDisparoService alertaDisparoService;
    private final CalculadoraTarifaService calculadoraTarifaService;

    // Salva EXCLUSIVAMENTE na coleção permanente "consumo" (Hora fechada)
    public Consumo salvar(Consumo consumo) {
        Consumo salvo = repository.save(consumo);

        if (salvo.getUsuarioId() != null && !salvo.getUsuarioId().isBlank()) {
            try {
                alertaDisparoService.avaliar(salvo.getUsuarioId());
            } catch (Exception e) {
                log.error("Erro ao avaliar alertas:", e);
            }
        }
        return salvo;
    }

    // Salva EXCLUSIVAMENTE na coleção temporária "consumo_realtime" (Minuto a minuto)
    public void salvarRealtimeMinuto(String usuarioId, double somaKwh, LocalDateTime minutoChave) {
        // Define o limite exato da meia-noite no fuso de São Paulo / Brasil
        ZoneId zoneIdBrasil = ZoneId.of("America/Sao_Paulo");
        ZonedDateTime meiaNoiteProximoDia = minutoChave.toLocalDate().plusDays(1).atStartOfDay(zoneIdBrasil);
        Date dataExpiracaoMeiaNoite = Date.from(meiaNoiteProximoDia.toInstant());

        ConsumoRealtime rt = new ConsumoRealtime();
        rt.setUsuarioId(usuarioId);
        rt.setConsumoKwh(somaKwh);
        rt.setDataHora(minutoChave);
        rt.setDataExpiracao(dataExpiracaoMeiaNoite);

        realtimeRepository.save(rt);
    }

    // TAREFA AGENDADA: Garante a limpeza total do consumo_realtime todos os dias à 00:00:05
    @Scheduled(cron = "5 0 0 * * *", zone = "America/Sao_Paulo")
    public void limparRealtimeMeiaNoite() {
        try {
            realtimeRepository.deleteAll();
            log.info(">>> COLEÇÃO CONSUMO_REALTIME LIMPA COM SUCESSO À MEIA-NOITE <<<");
        } catch (Exception e) {
            log.error("Erro ao limpar consumo_realtime à meia-noite:", e);
        }
    }

    public DashboardDTO obterDadosDashboard(String usuarioId) {
        LocalDate hoje = LocalDate.now();
        double tarifaKwh = configuracaoService.obterTarifaKwh(usuarioId);

        double limiteMeta = 250.0;

        // 1. ACUMULADO DIÁRIO (BANCO PERMANENTE + REALTIME DO DIA)
        LocalDateTime inicioHoje = hoje.atStartOfDay();
        LocalDateTime fimHoje = hoje.plusDays(1).atStartOfDay().minusNanos(1);
        List<Consumo> leiturasHoje = repository.findByUsuarioIdAndDataHoraBetween(usuarioId, inicioHoje, fimHoje);
        double consumoTotalDiarioKwh = leiturasHoje.stream()
                .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0).sum();

        List<ConsumoRealtime> realtimeHoje = realtimeRepository.findByUsuarioIdAndDataHoraBetween(usuarioId, inicioHoje, fimHoje);
        consumoTotalDiarioKwh += realtimeHoje.stream()
                .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0).sum();

        // 2. ACUMULADO MENSAL (BANCO PERMANENTE + REALTIME DO DIA)
        LocalDateTime inicioMes = hoje.withDayOfMonth(1).atStartOfDay();
        List<Consumo> leiturasMes = repository.findByUsuarioIdAndDataHoraBetween(usuarioId, inicioMes, fimHoje);
        double acumuladoMesKwh = leiturasMes.stream()
                .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0).sum();

        acumuladoMesKwh += realtimeHoje.stream()
                .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0).sum();

        DashboardDTO dto = new DashboardDTO();

        dto.setConsumoTotalDiarioKwh(arredondar(consumoTotalDiarioKwh, 5));
        dto.setMetaConsumoMensalAtualKwh(arredondar(acumuladoMesKwh, 2));
        dto.setMetaConsumoMensalLimiteKwh(limiteMeta);
        dto.setMetaGastoKwh(limiteMeta);

        double percentual = limiteMeta > 0 ? (acumuladoMesKwh / limiteMeta) * 100.0 : 0.0;
        dto.setPercentualMetaAtingida(arredondar(percentual, 1));

        if (percentual >= 100) {
            dto.setMensagemStatusMeta("Meta mensal atingida!");
        } else if (percentual >= 80) {
            dto.setMensagemStatusMeta("Atenção: 80% da meta atingida!");
        } else {
            dto.setMensagemStatusMeta("Você está dentro da sua meta estipulada!");
        }

        // 3. GRÁFICO 1: TEMPO REAL (ÚLTIMOS 10 MINUTOS)
        LocalDateTime agora = LocalDateTime.now();
        List<DashboardDTO.PontoGraficoMinuto> pontosGrafico1 = new ArrayList<>();
        for (int i = 9; i >= 0; i--) {
            LocalDateTime minutoAlvo = agora.minusMinutes(i);
            LocalDateTime inicioMinuto = minutoAlvo.withSecond(0).withNano(0);
            LocalDateTime fimMinuto = minutoAlvo.withSecond(59).withNano(999999999);

            List<ConsumoRealtime> leiturasMinuto = realtimeRepository.findByUsuarioIdAndDataHoraBetween(usuarioId, inicioMinuto, fimMinuto);
            double consumoMinuto = leiturasMinuto.stream().mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0).sum();

            DashboardDTO.PontoGraficoMinuto p = new DashboardDTO.PontoGraficoMinuto();
            p.setHora(minutoAlvo.format(DateTimeFormatter.ofPattern("HH:mm")));
            p.setConsumoKwh(arredondar(consumoMinuto, 4));
            pontosGrafico1.add(p);
        }
        dto.setConsumoTempoReal(pontosGrafico1);

        // 4. GRÁFICO 2: MEDIÇÕES POR HORA DO DIA
        List<Consumo> todasLeituras = repository.findByUsuarioIdOrderByDataHoraDesc(usuarioId);
        if (todasLeituras != null && !todasLeituras.isEmpty()) {
            Map<String, Double> consumoPorHora = new LinkedHashMap<>();
            for (Consumo c : todasLeituras) {
                if (c.getDataHora() != null) {
                    String chaveHora = c.getDataHora().format(DateTimeFormatter.ofPattern("HH")) + "h";
                    double kwh = c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0;
                    consumoPorHora.merge(chaveHora, kwh, Double::sum);
                }
            }
            List<DashboardDTO.PontoGraficoHora> pontosGrafico2 = consumoPorHora.entrySet().stream().map(entry -> {
                DashboardDTO.PontoGraficoHora p = new DashboardDTO.PontoGraficoHora();
                p.setHora(entry.getKey());
                p.setConsumoKwh(arredondar(entry.getValue(), 3));
                return p;
            }).toList();

            List<DashboardDTO.PontoGraficoHora> listaOrdenada = new ArrayList<>(pontosGrafico2);
            Collections.reverse(listaOrdenada);
            if (listaOrdenada.size() > 10) {
                listaOrdenada = listaOrdenada.subList(listaOrdenada.size() - 10, listaOrdenada.size());
            }
            dto.setConsumoDiarioHoras(listaOrdenada);
        } else {
            dto.setConsumoDiarioHoras(new ArrayList<>());
        }

        // 5. GRÁFICO 3: ESTIMATIVA DA CONTA
        List<DashboardDTO.PontoGraficoMes> pontosGrafico3 = new ArrayList<>();
        for (int i = 2; i >= 0; i--) {
            LocalDate mesRef = hoje.minusMonths(i);
            LocalDateTime inicioM = mesRef.withDayOfMonth(1).atStartOfDay();
            LocalDateTime fimM = mesRef.withDayOfMonth(mesRef.lengthOfMonth()).atTime(23, 59, 59);

            List<Consumo> leiturasDoMes = repository.findByUsuarioIdAndDataHoraBetween(usuarioId, inicioM, fimM);
            double totalKwhMes = leiturasDoMes.stream()
                    .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0).sum();

            if (i == 0) {
                totalKwhMes += realtimeHoje.stream()
                        .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0).sum();
            }

            double valorEstimado = calculadoraTarifaService.calcularEstimativaOficialContaMensal(
                    totalKwhMes, CalculadoraTarifaService.BandeiraTarifaria.VERDE
            );

            String nomeMes = mesRef.format(DateTimeFormatter.ofPattern("MMM", new Locale("pt", "BR")));
            nomeMes = nomeMes.substring(0, 1).toUpperCase() + nomeMes.substring(1).replace(".", "");

            DashboardDTO.PontoGraficoMes pMes = new DashboardDTO.PontoGraficoMes();
            pMes.setMes(nomeMes);
            pMes.setValor(arredondar(valorEstimado, 2));
            pontosGrafico3.add(pMes);
        }
        dto.setValorContaEstimadoReais(pontosGrafico3);

        return dto;
    }

    public List<Consumo> listarPorUsuario(String usuarioId) {
        return repository.findByUsuarioIdOrderByDataHoraDesc(usuarioId);
    }

    public List<ConsumoDiarioDTO> listarTotaisDiarios(
            String usuarioId,
            LocalDate inicio,
            LocalDate fim) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new IllegalArgumentException(
                    "Usuário não informado."
            );
        }
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException(
                    "As datas de início e fim são obrigatórias."
            );
        }
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException(
                    "A data final não pode ser anterior à data inicial."
            );
        }
        LocalDateTime inicioConsulta =
                inicio.atStartOfDay();
        LocalDateTime fimConsulta =
                fim.plusDays(1)
                        .atStartOfDay()
                        .minusNanos(1);
        List<Consumo> leituras =
                repository.findByUsuarioIdAndDataHoraBetween(
                        usuarioId,
                        inicioConsulta,
                        fimConsulta
                );
        Map<LocalDate, Double> consumoPorDia =
                new TreeMap<>();

        for (Consumo consumo : leituras) {
            if (consumo.getDataHora() == null) {
                continue;
            }
            double kwh =
                    consumo.getConsumoKwh() != null
                            ? consumo.getConsumoKwh()
                            : 0.0;
            if (!Double.isFinite(kwh) || kwh < 0) {
                kwh = 0.0;
            }
            LocalDate dia =
                    consumo.getDataHora()
                            .toLocalDate();
            consumoPorDia.merge(
                    dia,
                    kwh,
                    Double::sum
            );
        }
        LocalDate hoje =
                LocalDate.now();
        boolean consultaIncluiHoje =
                !hoje.isBefore(inicio)
                        && !hoje.isAfter(fim);
        if (consultaIncluiHoje) {
            LocalDateTime inicioHoje =
                    hoje.atStartOfDay();
            LocalDateTime fimHoje =
                    hoje.plusDays(1)
                            .atStartOfDay()
                            .minusNanos(1);
            LocalDateTime ultimaLeituraPermanenteHoje =
                    leituras.stream()
                            .filter(consumo ->
                                    consumo.getDataHora() != null
                                            && consumo.getDataHora()
                                            .toLocalDate()
                                            .equals(hoje)
                            )
                            .map(Consumo::getDataHora)
                            .max(LocalDateTime::compareTo)
                            .orElse(null);
            LocalDateTime inicioRealtime =
                    inicioHoje;
            if (ultimaLeituraPermanenteHoje != null) {
                inicioRealtime =
                        ultimaLeituraPermanenteHoje
                                .withMinute(0)
                                .withSecond(0)
                                .withNano(0)
                                .plusHours(1);
            }
            if (!inicioRealtime.isAfter(fimHoje)) {
                List<ConsumoRealtime> realtime =
                        realtimeRepository
                                .findByUsuarioIdAndDataHoraBetween(
                                        usuarioId,
                                        inicioRealtime,
                                        fimHoje
                                );
                double consumoRealtime =
                        realtime.stream()
                                .mapToDouble(item -> {
                                    Double valor =
                                            item.getConsumoKwh();
                                    if (valor == null
                                            || !Double.isFinite(valor)
                                            || valor < 0) {
                                        return 0.0;
                                    }
                                    return valor;
                                })
                                .sum();
                if (consumoRealtime > 0) {
                    consumoPorDia.merge(
                            hoje,
                            consumoRealtime,
                            Double::sum
                    );
                }
            }
        }
        double tarifaKwh =
                configuracaoService
                        .obterTarifaKwh(
                                usuarioId
                        );
        return consumoPorDia
                .entrySet()
                .stream()
                .map(entry -> {
                    double kwh =
                            arredondar(
                                    entry.getValue(),
                                    5
                            );
                    double reais =
                            arredondar(
                                    kwh * tarifaKwh,
                                    2
                            );
                    return new ConsumoDiarioDTO(
                            entry.getKey(),
                            kwh,
                            reais
                    );
                })
                .toList();
    }

    public long apagarHistoricoPorUsuario(String usuarioId) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new IllegalArgumentException("Usuário inválido.");
        }
        return repository.deleteByUsuarioId(usuarioId);
    }

    private double arredondar(double valor, int casas) {
        double fator = Math.pow(10, casas);
        return Math.round(valor * fator) / fator;
    }
}