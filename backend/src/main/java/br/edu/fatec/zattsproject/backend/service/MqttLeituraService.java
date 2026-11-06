package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.Consumo;
import br.edu.fatec.zattsproject.backend.repository.ConsumoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttLeituraService {

    private final ConsumoRepository consumoRepository;
    private final ObjectMapper objectMapper;
    private final SseService sseService;

    /**
     * Método chamado sempre que uma nova mensagem chega do ESP32 via MQTT
     */
    public void processarEMensurarLeitura(String payloadJson) {
        try {
            Consumo consumo = objectMapper.readValue(payloadJson, Consumo.class);

            if (consumo.getDataHora() == null) {
                consumo.setDataHora(LocalDateTime.now());
            }

            Consumo salvo = consumoRepository.save(consumo);

            log.info("Consumo salvo imediatamente no MongoDB Atlas: ID={}", salvo.getId());

            // Transmite a nova leitura via SSE para os clientes conectados
            sseService.sendConsumoToAll(salvo);

        } catch (Exception e) {
            log.error("Erro ao processar e salvar consumo do MQTT: ", e);
        }
    }
}