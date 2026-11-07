package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.Consumo;
import br.edu.fatec.zattsproject.backend.repository.ConsumoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttLeituraService {

    private final ConsumoRepository consumoRepository;
    private final ObjectMapper objectMapper;
    private final SseService sseService;

    // Buffer em memória para guardar na base de dados apenas 1 vez por hora
    private final Map<String, AcumuladorHora> bufferAcumulado = new ConcurrentHashMap<>();

    private static class AcumuladorHora {
        LocalDateTime horaChave;
        double somaKwh;
        double somaPotenciaWatts;
        double somaCorrenteAmperes;
        int quantidadeLeituras;
        String dispositivoId;

        AcumuladorHora(LocalDateTime horaChave, String dispositivoId) {
            this.horaChave = horaChave;
            this.dispositivoId = dispositivoId;
            this.somaKwh = 0.0;
            this.somaPotenciaWatts = 0.0;
            this.somaCorrenteAmperes = 0.0;
            this.quantidadeLeituras = 0;
        }
    }

    public void processarEMensurarLeitura(String payloadJson) {
        try {
            Consumo consumoInstantaneo = objectMapper.readValue(payloadJson, Consumo.class);

            if (consumoInstantaneo.getDataHora() == null) {
                consumoInstantaneo.setDataHora(LocalDateTime.now());
            }

            String usuarioId = consumoInstantaneo.getUsuarioId();
            if (usuarioId == null) return;

            // 1. NOTIFICA O SSE APENAS QUE UMA LEITURA CHEGOU (Sem sobrescrever os cards com fracionados)
            sseService.sendConsumoToAll(consumoInstantaneo);

            // 2. ACUMULA EM MEMÓRIA PARA GRAVAR NO MONGO ATLAS DE 1 EM 1 HORA
            double kwhPulso = consumoInstantaneo.getConsumoKwh() != null ? consumoInstantaneo.getConsumoKwh() : 0.0;
            LocalDateTime horaAtualTruncada = consumoInstantaneo.getDataHora().truncatedTo(ChronoUnit.HOURS);

            bufferAcumulado.compute(usuarioId, (id, acumulador) -> {
                if (acumulador != null && !acumulador.horaChave.equals(horaAtualTruncada)) {
                    salvarConsumoFechadoNoAtlas(id, acumulador);
                    acumulador = null;
                }

                if (acumulador == null) {
                    acumulador = new AcumuladorHora(horaAtualTruncada, consumoInstantaneo.getDispositivoId());
                }

                acumulador.somaKwh += kwhPulso;
                acumulador.somaPotenciaWatts += consumoInstantaneo.getPotenciaWatts() != null ? consumoInstantaneo.getPotenciaWatts() : 0.0;
                acumulador.somaCorrenteAmperes += consumoInstantaneo.getCorrenteAmperes() != null ? consumoInstantaneo.getCorrenteAmperes() : 0.0;
                acumulador.quantidadeLeituras++;

                return acumulador;
            });

        } catch (Exception e) {
            log.error("Erro ao processar leitura do MQTT: ", e);
        }
    }

    private void salvarConsumoFechadoNoAtlas(String usuarioId, AcumuladorHora acumulador) {
        double potenciaMedia = acumulador.quantidadeLeituras > 0 ? (acumulador.somaPotenciaWatts / acumulador.quantidadeLeituras) : 0.0;
        double correnteMedia = acumulador.quantidadeLeituras > 0 ? (acumulador.somaCorrenteAmperes / acumulador.quantidadeLeituras) : 0.0;

        Consumo consumoFechado = Consumo.builder()
                .dispositivoId(acumulador.dispositivoId)
                .usuarioId(usuarioId)
                .consumoKwh(Math.round(acumulador.somaKwh * 100000.0) / 100000.0)
                .potenciaWatts(Math.round(potenciaMedia * 100.0) / 100.0)
                .correnteAmperes(Math.round(correnteMedia * 100.0) / 100.0)
                .voltagem(220.0)
                .dataHora(acumulador.horaChave)
                .build();

        Consumo salvo = consumoRepository.save(consumoFechado);
        log.info(">>> CONSUMO CONSOLIDADO DA HORA SALVO NO ATLAS: ID={} | Hora={} | Total={} kWh", salvo.getId(), acumulador.horaChave, salvo.getConsumoKwh());
    }
}