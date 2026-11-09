package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.dto.ConsumoDiarioDTO;
import br.edu.fatec.zattsproject.backend.dto.DashboardDTO;
import br.edu.fatec.zattsproject.backend.model.Consumo;
import br.edu.fatec.zattsproject.backend.repository.ConsumoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumoService {

    private final ConsumoRepository repository;
    private final ConfiguracaoService configuracaoService;
    private final AlertaDisparoService alertaDisparoService;

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

    public DashboardDTO obterDadosDashboard(String usuarioId) {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicioDia = hoje.atStartOfDay();
        LocalDateTime fimDia = hoje.plusDays(1).atStartOfDay();

        // 1. TOTAIS DIÁRIO (MEIA-NOITE ATÉ AGORA) E MENSAL
        List<Consumo> leiturasHoje = repository.findByUsuarioIdAndDataHoraBetween(usuarioId, inicioDia, fimDia);
        double consumoTotalDiarioKwh = leiturasHoje.stream()
                .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0)
                .sum();

        LocalDateTime inicioMes = hoje.withDayOfMonth(1).atStartOfDay();
        List<Consumo> leiturasMes = repository.findByUsuarioIdAndDataHoraBetween(usuarioId, inicioMes, fimDia);
        double acumuladoMesKwh = leiturasMes.stream()
                .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0)
                .sum();

        double limiteMeta = 250.0;

        DashboardDTO dto = new DashboardDTO();
        dto.setConsumoTotalDiarioKwh(arredondar(consumoTotalDiarioKwh, 4));
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

        // 2. BUSCA HISTÓRICA COMPLETA NO MONGODB ATLAS
        List<Consumo> todasLeituras = repository.findByUsuarioIdOrderByDataHoraDesc(usuarioId);

        // Envia a última leitura e o histórico recente para o frontend alimentar a janela dos 15 minutos
        List<DashboardDTO.PontoGraficoMinuto> pontosGrafico1 = new ArrayList<>();
        if (todasLeituras != null && !todasLeituras.isEmpty()) {
            List<Consumo> recentes = todasLeituras.stream()
                    .limit(15)
                    .sorted(Comparator.comparing(Consumo::getDataHora))
                    .toList();

            for (Consumo c : recentes) {
                DashboardDTO.PontoGraficoMinuto p = new DashboardDTO.PontoGraficoMinuto();
                p.setHora(c.getDataHora() != null ? c.getDataHora().format(DateTimeFormatter.ofPattern("HH:mm")) : "");
                p.setConsumoKwh(c.getConsumoKwh() != null ? arredondar(c.getConsumoKwh(), 4) : 0.0);
                pontosGrafico1.add(p);
            }
        }
        dto.setConsumoTempoReal(pontosGrafico1);

        // --- GRÁFICO 2: ÚLTIMAS 10 HORAS REGISTRADAS ---
        if (todasLeituras != null && !todasLeituras.isEmpty()) {
            Map<String, Double> consumoPorHora = new LinkedHashMap<>();

            for (Consumo c : todasLeituras) {
                if (c.getDataHora() != null) {
                    String chaveHora = c.getDataHora().format(DateTimeFormatter.ofPattern("HH")) + "h";
                    double kwh = c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0;
                    consumoPorHora.merge(chaveHora, kwh, Double::sum);
                }
            }

            List<DashboardDTO.PontoGraficoHora> pontosGrafico2 = consumoPorHora.entrySet().stream()
                    .map(entry -> {
                        DashboardDTO.PontoGraficoHora p = new DashboardDTO.PontoGraficoHora();
                        p.setHora(entry.getKey());
                        p.setConsumoKwh(arredondar(entry.getValue(), 3));
                        return p;
                    })
                    .toList();

            List<DashboardDTO.PontoGraficoHora> listaOrdenada = new ArrayList<>(pontosGrafico2);
            Collections.reverse(listaOrdenada);

            if (listaOrdenada.size() > 10) {
                listaOrdenada = listaOrdenada.subList(listaOrdenada.size() - 10, listaOrdenada.size());
            }

            dto.setConsumoDiarioHoras(listaOrdenada);
        } else {
            dto.setConsumoDiarioHoras(new ArrayList<>());
        }

        // --- GRÁFICO 3: ESTIMATIVA DA CONTA (3 MESES HISTÓRICOS REAIS) ---
        double tarifaKwh = configuracaoService.obterTarifaKwh(usuarioId);
        List<DashboardDTO.PontoGraficoMes> pontosGrafico3 = new ArrayList<>();

        for (int i = 2; i >= 0; i--) {
            LocalDate mesRef = hoje.minusMonths(i);
            LocalDateTime inicioM = mesRef.withDayOfMonth(1).atStartOfDay();
            LocalDateTime fimM = mesRef.withDayOfMonth(mesRef.lengthOfMonth()).atTime(23, 59, 59);

            List<Consumo> leiturasDoMes = repository.findByUsuarioIdAndDataHoraBetween(usuarioId, inicioM, fimM);
            double totalKwhMes = leiturasDoMes.stream()
                    .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0)
                    .sum();

            double valorEstimado = totalKwhMes > 0
                    ? arredondar((totalKwhMes * tarifaKwh) + 12.50, 2)
                    : 0.0;

            String nomeMes = mesRef.format(DateTimeFormatter.ofPattern("MMM", new Locale("pt", "BR")));
            nomeMes = nomeMes.substring(0, 1).toUpperCase() + nomeMes.substring(1).replace(".", "");

            DashboardDTO.PontoGraficoMes pMes = new DashboardDTO.PontoGraficoMes();
            pMes.setMes(nomeMes);
            pMes.setValor(valorEstimado);
            pontosGrafico3.add(pMes);
        }
        dto.setValorContaEstimadoReais(pontosGrafico3);

        return dto;
    }

    public List<Consumo> listarPorUsuario(String usuarioId) {
        return repository.findByUsuarioIdOrderByDataHoraDesc(usuarioId);
    }

    public List<Consumo> buscarPorUsuarioEIntervalo(String usuarioId, LocalDateTime inicio, LocalDateTime fim) {
        return repository.findByUsuarioIdAndDataHoraBetween(usuarioId, inicio, fim);
    }

    public List<ConsumoDiarioDTO> listarTotaisDiarios(String usuarioId, LocalDate inicio, LocalDate fim) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new IllegalArgumentException("Usuário não informado.");
        }
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("As datas de início e fim são obrigatórias.");
        }
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("A data final não pode ser anterior à data inicial.");
        }

        LocalDateTime inicioConsulta = inicio.atStartOfDay();
        LocalDateTime fimConsulta = fim.plusDays(1).atStartOfDay();

        List<Consumo> leituras = repository.buscarPorUsuarioEIntervaloOrdenado(
                usuarioId,
                inicioConsulta,
                fimConsulta
        );

        Map<LocalDate, Double> consumoPorDia = new TreeMap<>();
        for (Consumo consumo : leituras) {
            if (consumo.getDataHora() == null) continue;
            double kwh = consumo.getConsumoKwh() != null ? consumo.getConsumoKwh() : 0.0;
            LocalDate dia = consumo.getDataHora().toLocalDate();
            consumoPorDia.merge(dia, kwh, Double::sum);
        }

        double tarifaKwh = configuracaoService.obterTarifaKwh(usuarioId);

        return consumoPorDia.entrySet().stream()
                .map(entry -> {
                    double kwh = arredondar(entry.getValue(), 5);
                    double reais = arredondar(kwh * tarifaKwh, 2);
                    return new ConsumoDiarioDTO(entry.getKey(), kwh, reais);
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