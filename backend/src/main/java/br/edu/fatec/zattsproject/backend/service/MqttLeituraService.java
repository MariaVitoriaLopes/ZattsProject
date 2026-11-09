package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.dto.DashboardDTO;
import br.edu.fatec.zattsproject.backend.model.Consumo;
import br.edu.fatec.zattsproject.backend.model.ConsumoRealtime;
import br.edu.fatec.zattsproject.backend.model.Dispositivo;
import br.edu.fatec.zattsproject.backend.repository.ConsumoRealtimeRepository;
import br.edu.fatec.zattsproject.backend.repository.DispositivoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttLeituraService {

    private final ConsumoService consumoService;
    private final ConsumoRealtimeRepository realtimeRepository;
    private final DispositivoRepository dispositivoRepository;
    private final ObjectMapper objectMapper;
    private final SseService sseService;
    private final AlertaDisparoService alertaDisparoService;

    private final Map<String, AcumuladorHora> bufferAcumulado = new ConcurrentHashMap<>();
    private final Map<String, AcumuladorMinuto> bufferMinutoRealtime = new ConcurrentHashMap<>();
    private final Map<String, List<DashboardDTO.PontoGraficoMinuto>> bufferPontosGrafico = new ConcurrentHashMap<>();

    public static class AcumuladorHora {
        public LocalDateTime horaChave;
        public double somaKwh;
        public double somaPotenciaWatts;
        public double somaCorrenteAmperes;
        public int quantidadeLeituras;
        public String dispositivoId;

        AcumuladorHora(LocalDateTime horaChave, String dispositivoId) {
            this.horaChave = horaChave;
            this.dispositivoId = dispositivoId;
            this.somaKwh = 0.0;
            this.somaPotenciaWatts = 0.0;
            this.somaCorrenteAmperes = 0.0;
            this.quantidadeLeituras = 0;
        }
    }

    public static class AcumuladorMinuto {
        public LocalDateTime minutoChave;
        public double somaKwh;

        AcumuladorMinuto(LocalDateTime minutoChave) {
            this.minutoChave = minutoChave;
            this.somaKwh = 0.0;
        }
    }

    public void processarEMensurarLeitura(String payloadJson) {
        try {
            Consumo consumoInstantaneo = objectMapper.readValue(payloadJson, Consumo.class);

            if (consumoInstantaneo.getDataHora() == null) {
                consumoInstantaneo.setDataHora(LocalDateTime.now());
            }

            String usuarioId = resolverUsuario(consumoInstantaneo);

            if (usuarioId == null) {
                log.warn("Leitura MQTT ignorada: usuário não identificado.");
                return;
            }

            consumoInstantaneo.setUsuarioId(usuarioId);

            String horaMinutoAtualStr = consumoInstantaneo.getDataHora().format(DateTimeFormatter.ofPattern("HH:mm"));
            double valKwh = valorPositivoOuZero(consumoInstantaneo.getConsumoKwh());

            // 1. GRAVAÇÃO MINUTO A MINUTO (Apenas na coleção consumo_realtime)
            LocalDateTime minutoAtualTruncado = consumoInstantaneo.getDataHora().truncatedTo(ChronoUnit.MINUTES);

            bufferMinutoRealtime.compute(usuarioId, (id, acMinuto) -> {
                if (acMinuto != null && !acMinuto.minutoChave.equals(minutoAtualTruncado)) {
                    double consumoMinuto = Math.round(acMinuto.somaKwh * 100000.0) / 100000.0;
                    consumoService.salvarRealtimeMinuto(id, consumoMinuto, acMinuto.minutoChave);
                    acMinuto = null;
                }

                if (acMinuto == null) {
                    acMinuto = new AcumuladorMinuto(minutoAtualTruncado);
                }

                acMinuto.somaKwh += valKwh;
                return acMinuto;
            });

            // 2. ATUALIZA GRÁFICO EM MEMÓRIA
            List<DashboardDTO.PontoGraficoMinuto> listaPontos = bufferPontosGrafico.computeIfAbsent(usuarioId, k -> new CopyOnWriteArrayList<>());

            if (!listaPontos.isEmpty()) {
                DashboardDTO.PontoGraficoMinuto ultimoPonto = listaPontos.get(listaPontos.size() - 1);
                if (ultimoPonto.getHora().equals(horaMinutoAtualStr)) {
                    double novoValor = ultimoPonto.getConsumoKwh() + valKwh;
                    ultimoPonto.setConsumoKwh(Math.round(novoValor * 100000.0) / 100000.0);
                } else {
                    adicionarNovoPonto(listaPontos, horaMinutoAtualStr, valKwh);
                }
            } else {
                adicionarNovoPonto(listaPontos, horaMinutoAtualStr, valKwh);
            }

            // 3. ENVIO SSE PARA O FRONTEND
            sseService.sendConsumoToAll(consumoInstantaneo);

            double potenciaWatts = valorPositivoOuZero(consumoInstantaneo.getPotenciaWatts());
            double correnteAmperes = valorPositivoOuZero(consumoInstantaneo.getCorrenteAmperes());

            // 4. GRAVAÇÃO DE HORA EM HORA (Apenas na coleção consumo permanente)
            LocalDateTime horaAtualTruncada = consumoInstantaneo.getDataHora().truncatedTo(ChronoUnit.HOURS);

            AcumuladorHora acumuladorAtual = bufferAcumulado.compute(usuarioId, (id, acumulador) -> {
                if (acumulador != null && !acumulador.horaChave.equals(horaAtualTruncada)) {
                    salvarConsumoFechadoNoAtlas(id, acumulador);
                    acumulador = null;
                }

                if (acumulador == null) {
                    acumulador = new AcumuladorHora(horaAtualTruncada, consumoInstantaneo.getDispositivoId());
                }

                acumulador.somaKwh += valKwh;
                acumulador.somaPotenciaWatts += potenciaWatts;
                acumulador.somaCorrenteAmperes += correnteAmperes;
                acumulador.quantidadeLeituras++;

                return acumulador;
            });

            if (acumuladorAtual != null) {
                double potenciaMediaAtual = acumuladorAtual.quantidadeLeituras > 0
                        ? (acumuladorAtual.somaPotenciaWatts / acumuladorAtual.quantidadeLeituras)
                        : 0.0;

                try {
                    alertaDisparoService.avaliarEmTempoReal(
                            usuarioId,
                            acumuladorAtual.somaKwh,
                            potenciaMediaAtual
                    );
                } catch (Exception e) {
                    log.error("Erro nos alertas em tempo real:", e);
                }
            }

        } catch (Exception e) {
            log.error("Erro ao processar leitura do MQTT:", e);
        }
    }

    private void adicionarNovoPonto(List<DashboardDTO.PontoGraficoMinuto> listaPontos, String horaMinuto, double valKwh) {
        DashboardDTO.PontoGraficoMinuto novoPonto = new DashboardDTO.PontoGraficoMinuto();
        novoPonto.setHora(horaMinuto);
        novoPonto.setConsumoKwh(Math.round(valKwh * 100000.0) / 100000.0);
        listaPontos.add(novoPonto);

        if (listaPontos.size() > 10) {
            listaPontos.remove(0);
        }
    }

    private String resolverUsuario(Consumo consumo) {
        if (consumo.getUsuarioId() != null && !consumo.getUsuarioId().isBlank()) {
            return consumo.getUsuarioId();
        }

        if (consumo.getMacAddress() == null || consumo.getMacAddress().isBlank()) {
            return null;
        }

        Optional<Dispositivo> dispositivo = dispositivoRepository.findByMacAddress(consumo.getMacAddress());

        if (dispositivo.isEmpty() || !dispositivo.get().isAtivo()) {
            return null;
        }

        consumo.setDispositivoId(dispositivo.get().getId());
        return dispositivo.get().getUsuarioId();
    }

    private void salvarConsumoFechadoNoAtlas(String usuarioId, AcumuladorHora acumulador) {
        double potenciaMedia = acumulador.quantidadeLeituras > 0
                ? (acumulador.somaPotenciaWatts / acumulador.quantidadeLeituras)
                : 0.0;

        double correnteMedia = acumulador.quantidadeLeituras > 0
                ? (acumulador.somaCorrenteAmperes / acumulador.quantidadeLeituras)
                : 0.0;

        Consumo consumoFechado = Consumo.builder()
                .dispositivoId(acumulador.dispositivoId)
                .usuarioId(usuarioId)
                .consumoKwh(Math.round(acumulador.somaKwh * 100000.0) / 100000.0)
                .potenciaWatts(Math.round(potenciaMedia * 100.0) / 100.0)
                .correnteAmperes(Math.round(correnteMedia * 100.0) / 100.0)
                .voltagem(220.0)
                .dataHora(acumulador.horaChave)
                .build();

        Consumo salvo = consumoService.salvar(consumoFechado);

        log.info(">>> CONSUMO DA HORA CONSOLIDADO E SALVO NO ATLAS: ID={} | Total={} kWh",
                salvo.getId(), salvo.getConsumoKwh());
    }

    public double obterConsumoAcumuladoHoraAtual(String usuarioId) {
        if (usuarioId == null) return 0.0;
        AcumuladorHora acumulador = bufferAcumulado.get(usuarioId);
        return acumulador != null ? acumulador.somaKwh : 0.0;
    }

    public List<DashboardDTO.PontoGraficoMinuto> obterPontosGraficoTempoReal(String usuarioId) {
        if (usuarioId == null) return new ArrayList<>();

        LocalDateTime agora = LocalDateTime.now();
        List<DashboardDTO.PontoGraficoMinuto> pontos = new ArrayList<>();

        for (int i = 9; i >= 0; i--) {
            LocalDateTime minutoAlvo = agora.minusMinutes(i);
            LocalDateTime inicioMinuto = minutoAlvo.withSecond(0).withNano(0);
            LocalDateTime fimMinuto = minutoAlvo.withSecond(59).withNano(999999999);
            String horaMinutoStr = minutoAlvo.format(DateTimeFormatter.ofPattern("HH:mm"));

            List<ConsumoRealtime> leiturasBanco = realtimeRepository.findByUsuarioIdAndDataHoraBetween(usuarioId, inicioMinuto, fimMinuto);
            double consumoMinuto = leiturasBanco.stream()
                    .mapToDouble(c -> c.getConsumoKwh() != null ? c.getConsumoKwh() : 0.0)
                    .sum();

            AcumuladorMinuto acMinuto = bufferMinutoRealtime.get(usuarioId);
            if (acMinuto != null && acMinuto.minutoChave.equals(minutoAlvo.truncatedTo(ChronoUnit.MINUTES))) {
                consumoMinuto += acMinuto.somaKwh;
            }

            DashboardDTO.PontoGraficoMinuto p = new DashboardDTO.PontoGraficoMinuto();
            p.setHora(horaMinutoStr);
            p.setConsumoKwh(Math.round(consumoMinuto * 100000.0) / 100000.0);
            pontos.add(p);
        }

        return pontos;
    }

    private double valorPositivoOuZero(Double valor) {
        return (valor == null || !Double.isFinite(valor) || valor < 0) ? 0.0 : valor;
    }
}