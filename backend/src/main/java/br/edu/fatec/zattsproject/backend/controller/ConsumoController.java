package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.model.Consumo;
import br.edu.fatec.zattsproject.backend.service.ConsumoService;
import br.edu.fatec.zattsproject.backend.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/consumos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ConsumoController {

    private final ConsumoService service;
    private final SseService sseService;

    @PostMapping
    public Consumo salvar(@RequestBody Consumo consumo) {
        return service.salvar(consumo);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Consumo> listarPorUsuario(@PathVariable String usuarioId) {
        return service.listarPorUsuario(usuarioId);
    }


    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCorrente() {
        return sseService.addEmitter();
    }

    @GetMapping("/historico")
    public ResponseEntity<List<Consumo>> getHistorico() {
        return ResponseEntity.ok(service.listarPorUsuario(null));
    }
}