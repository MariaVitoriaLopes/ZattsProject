package br.edu.fatec.zattsproject.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttSubscriberService {

    private final MqttLeituraService mqttLeituraService;

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<String> message) {
        String payload = message.getPayload();
        log.info(">>> MENSAGEM MQTT RECEBIDA: {}", payload);

        try {
            mqttLeituraService.processarEMensurarLeitura(payload);
        } catch (Exception e) {
            log.error("❌ Erro ao repassar mensagem MQTT para o MqttLeituraService: ", e);
        }
    }
}