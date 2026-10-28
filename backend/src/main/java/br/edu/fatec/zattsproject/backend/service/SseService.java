package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.Consumo;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter addEmitter() {
        SseEmitter emitter = new SseEmitter(0L); // 0L = Sem timeout
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        return emitter;
    }

    public void sendConsumoToAll(Consumo consumo) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("nova-leitura")
                        .data(consumo));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    public void sendConsumoHorario(String caixaEnergiaId, LocalDateTime inicioHora, LocalDateTime fimHora, double consumoKwh, boolean consolidado) {
        Map<String, Object> dados = Map.of(
                "caixaEnergiaId", caixaEnergiaId,
                "inicioHora", inicioHora.toString(),
                "fimHora", fimHora.toString(),
                "consumoKwh", consumoKwh,
                "consolidado", consolidado
        );

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name("consumo-horario")
                                .data(dados)
                );
            } catch (IOException | IllegalStateException e) {
                emitters.remove(emitter);
            }
        }
    }
}