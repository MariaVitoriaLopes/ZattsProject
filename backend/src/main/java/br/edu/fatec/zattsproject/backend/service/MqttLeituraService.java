package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.Consumo;
import br.edu.fatec.zattsproject.backend.model.Dispositivo;
import br.edu.fatec.zattsproject.backend.repository.DispositivoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttLeituraService {

    private final ConsumoService consumoService;
    private final DispositivoRepository dispositivoRepository;
    private final ObjectMapper objectMapper;
    private final SseService sseService;

    private final Map<String, AcumuladorHora>
            bufferAcumulado =
            new ConcurrentHashMap<>();

    private static class AcumuladorHora {

        LocalDateTime horaChave;

        double somaKwh;
        double somaPotenciaWatts;
        double somaCorrenteAmperes;

        int quantidadeLeituras;

        String dispositivoId;

        AcumuladorHora(
                LocalDateTime horaChave,
                String dispositivoId) {

            this.horaChave = horaChave;
            this.dispositivoId = dispositivoId;
        }
    }

    public void processarEMensurarLeitura(
            String payloadJson) {

        try {

            Consumo consumoInstantaneo =
                    objectMapper.readValue(
                            payloadJson,
                            Consumo.class
                    );

            if (consumoInstantaneo.getDataHora()
                    == null) {

                consumoInstantaneo.setDataHora(
                        LocalDateTime.now()
                );
            }

            String usuarioId =
                    resolverUsuario(
                            consumoInstantaneo
                    );

            if (usuarioId == null) {

                log.warn(
                        "Leitura MQTT ignorada: "
                                + "não foi possível identificar o usuário. "
                                + "macAddress={}",
                        consumoInstantaneo.getMacAddress()
                );

                return;
            }

            consumoInstantaneo.setUsuarioId(
                    usuarioId
            );

            sseService.sendConsumoToAll(
                    consumoInstantaneo
            );

            double kwhPulso =
                    consumoInstantaneo.getConsumoKwh()
                            != null
                            ? consumoInstantaneo
                            .getConsumoKwh()
                            : 0.0;

            LocalDateTime horaAtualTruncada =
                    consumoInstantaneo
                            .getDataHora()
                            .truncatedTo(
                                    ChronoUnit.HOURS
                            );

            bufferAcumulado.compute(
                    usuarioId,
                    (id, acumulador) -> {

                        if (acumulador != null
                                && !acumulador
                                .horaChave
                                .equals(
                                        horaAtualTruncada
                                )) {

                            salvarConsumoFechadoNoAtlas(
                                    id,
                                    acumulador
                            );

                            acumulador = null;
                        }

                        if (acumulador == null) {

                            acumulador =
                                    new AcumuladorHora(
                                            horaAtualTruncada,
                                            consumoInstantaneo
                                                    .getDispositivoId()
                                    );
                        }

                        acumulador.somaKwh +=
                                kwhPulso;

                        acumulador.somaPotenciaWatts +=
                                consumoInstantaneo
                                        .getPotenciaWatts()
                                        != null
                                        ? consumoInstantaneo
                                        .getPotenciaWatts()
                                        : 0.0;

                        acumulador.somaCorrenteAmperes +=
                                consumoInstantaneo
                                        .getCorrenteAmperes()
                                        != null
                                        ? consumoInstantaneo
                                        .getCorrenteAmperes()
                                        : 0.0;

                        acumulador.quantidadeLeituras++;

                        return acumulador;
                    }
            );

        } catch (Exception e) {

            log.error(
                    "Erro ao processar leitura do MQTT:",
                    e
            );
        }
    }

    private String resolverUsuario(
            Consumo consumo) {

        if (consumo.getUsuarioId() != null
                && !consumo.getUsuarioId()
                .isBlank()) {

            return consumo.getUsuarioId();
        }

        if (consumo.getMacAddress() == null
                || consumo.getMacAddress()
                .isBlank()) {

            return null;
        }

        Optional<Dispositivo> dispositivo =
                dispositivoRepository
                        .findByMacAddress(
                                consumo.getMacAddress()
                        );

        if (dispositivo.isEmpty()) {

            log.warn(
                    "Nenhum dispositivo encontrado "
                            + "para o MAC {}",
                    consumo.getMacAddress()
            );

            return null;
        }

        Dispositivo encontrado =
                dispositivo.get();

        if (!encontrado.isAtivo()) {

            log.warn(
                    "Leitura ignorada porque "
                            + "o dispositivo {} está inativo.",
                    consumo.getMacAddress()
            );

            return null;
        }

        consumo.setDispositivoId(
                encontrado.getId()
        );

        return encontrado.getUsuarioId();
    }

    private void salvarConsumoFechadoNoAtlas(
            String usuarioId,
            AcumuladorHora acumulador) {

        double potenciaMedia =
                acumulador.quantidadeLeituras > 0
                        ? acumulador.somaPotenciaWatts
                        / acumulador.quantidadeLeituras
                        : 0.0;

        double correnteMedia =
                acumulador.quantidadeLeituras > 0
                        ? acumulador.somaCorrenteAmperes
                        / acumulador.quantidadeLeituras
                        : 0.0;

        Consumo consumoFechado =
                Consumo.builder()
                        .dispositivoId(
                                acumulador.dispositivoId
                        )
                        .usuarioId(
                                usuarioId
                        )
                        .consumoKwh(
                                Math.round(
                                        acumulador.somaKwh
                                                * 100000.0
                                ) / 100000.0
                        )
                        .potenciaWatts(
                                Math.round(
                                        potenciaMedia
                                                * 100.0
                                ) / 100.0
                        )
                        .correnteAmperes(
                                Math.round(
                                        correnteMedia
                                                * 100.0
                                ) / 100.0
                        )
                        .voltagem(220.0)
                        .dataHora(
                                acumulador.horaChave
                        )
                        .build();

        Consumo salvo =
                consumoService.salvar(
                        consumoFechado
                );

        log.info(
                ">>> CONSUMO CONSOLIDADO DA HORA "
                        + "SALVO NO ATLAS: "
                        + "ID={} | Hora={} | Total={} kWh",
                salvo.getId(),
                acumulador.horaChave,
                salvo.getConsumoKwh()
        );
    }
}