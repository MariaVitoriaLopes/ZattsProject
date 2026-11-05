package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.Consumo;
import br.edu.fatec.zattsproject.backend.model.Dispositivo;
import br.edu.fatec.zattsproject.backend.repository.DispositivoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MqttSubscriberService {

    @Autowired
    private SseService sseService;

    @Autowired
    private ConsumoService consumoService;

    @Autowired
    private DispositivoRepository dispositivoRepository; // Repositório para buscar o dispositivo pelo MAC

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<String> message) {
        String payload = message.getPayload();
        System.out.println(">>> MENSAGEM MQTT RECEBIDA: " + payload);

        try {
            // Converte o JSON enviado pelo ESP32 para a entidade Consumo
            Consumo novoConsumo = objectMapper.readValue(payload, Consumo.class);

            if (novoConsumo.getDataHora() == null) {
                novoConsumo.setDataHora(LocalDateTime.now());
            }

            // Busca o dispositivo no MongoDB pelo macAddress recebido do ESP32
            if (novoConsumo.getMacAddress() != null) {
                Optional<Dispositivo> dispOpt = dispositivoRepository.findByMacAddress(novoConsumo.getMacAddress());
                if (dispOpt.isPresent()) {
                    // Vincula o ID do utilizador dono do dispositivo
                    novoConsumo.setUsuarioId(dispOpt.get().getUsuarioId());
                } else {
                    System.err.println("⚠️ Dispositivo com MAC " + novoConsumo.getMacAddress() + " não foi encontrado no MongoDB!");
                }
            }

            // 1. Salva a leitura no MongoDB Atlas
            consumoService.salvar(novoConsumo);

            // 2. Dispara a leitura em tempo real via SSE para a Dashboard
            sseService.sendConsumoToAll(novoConsumo);

            System.out.println(">>> CONSUMO SALVO COM SUCESSO NO ATLAS PARA O USUARIO: " + novoConsumo.getUsuarioId());

        } catch (Exception e) {
            System.err.println("❌ Erro ao processar mensagem MQTT: " + e.getMessage());
        }
    }
}