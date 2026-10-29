package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.Consumo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MqttSubscriberService {

    @Autowired
    private SseService sseService;

    @Autowired
    private ConsumoService consumoService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<String> message) {
        String payload = message.getPayload();
        System.out.println(">>> MENSAGEM MQTT RECEBIDA: " + payload);

        try {
            Consumo novoConsumo = objectMapper.readValue(payload, Consumo.class);

            if (novoConsumo.getDataHora() == null) {
                novoConsumo.setDataHora(LocalDateTime.now());
            }

            // Caso o JSON não traga usuarioId, podes definir um ID padrão ou buscar por dispositivoId
            if (novoConsumo.getUsuarioId() == null) {
                // Exemplo: novoConsumo.setUsuarioId("ID_PADRAO_OU_BUSCADO");
            }

            // 1. Salva a leitura no MongoDB Atlas na coleção 'consumo'
            consumoService.salvar(novoConsumo);

            // 2. Dispara a leitura em tempo real via SSE
            sseService.sendConsumoToAll(novoConsumo);

            System.out.println(">>> CONSUMO SALVO COM SUCESSO NO ATLAS PARA O USUARIO: " + novoConsumo.getUsuarioId());

        } catch (Exception e) {
            System.err.println("Erro ao processar mensagem MQTT: " + e.getMessage());
        }
    }}