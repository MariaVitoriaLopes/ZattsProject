package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.Consumo;
//import com.fasterxml.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
public class MqttSubscriberService {

    @Autowired
    private SseService sseService;

    @Autowired
    private ConsumoService consumoService;

    private ConsumoAcumuladorService acumuladorService;

    private final ObjectMapper objectMapper;

    public MqttSubscriberService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return message -> {
            String payload = message.getPayload().toString();
            System.out.println("Mensagem MQTT recebida: " + payload);

            try {
//                Consumo novoConsumo = objectMapper.readValue(payload, Consumo.class);
//
//                if (novoConsumo.getDataHora() == null) {
//                    novoConsumo.setDataHora(LocalDateTime.now());
//                }
//
//                // 1. Salva a leitura no MongoDB
//                consumoService.salvar(novoConsumo);
//
//                // 2. Dispara a leitura em tempo real via SSE
//                sseService.sendConsumoToAll(novoConsumo);

                Consumo consumo = objectMapper.readValue(
                        payload,
                        Consumo.class
                );

                if (consumo.getDataHora() == null) {
                    consumo.setDataHora(
                            java.time.LocalDateTime.now()
                    );
                }

                // Não salvar a leitura individual no MongoDB.
                acumuladorService.processar(consumo);

            } catch (Exception e) {
                System.err.println("Erro ao processar mensagem MQTT: " + e.getMessage());
            }
        };
    }
}