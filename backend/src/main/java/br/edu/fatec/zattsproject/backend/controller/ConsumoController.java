package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.dto.ConsumoDiarioDTO;
import br.edu.fatec.zattsproject.backend.model.Consumo;
import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.ConsumoService;
import br.edu.fatec.zattsproject.backend.service.SseService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consumos")
@RequiredArgsConstructor
public class ConsumoController {

    private final ConsumoService service;
    private final SseService sseService;

    private Usuario obterUsuarioLogado(HttpSession session) {
        return (Usuario) session.getAttribute("usuarioLogado");
    }

    @PostMapping
    public Consumo salvar(@RequestBody Consumo consumo) {
        return service.salvar(consumo);
    }

    /*
     * Rota antiga mantida temporariamente por compatibilidade.
     * Agora ela só permite consultar o próprio usuário.
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> listarPorUsuario(
            @PathVariable String usuarioId,
            HttpSession session) {

        Usuario usuario = obterUsuarioLogado(session);

        if (usuario == null) {
            return ResponseEntity.status(401)
                    .body(Map.of(
                            "erro",
                            "Usuário não autenticado."
                    ));
        }

        if (!usuario.getId().equals(usuarioId)) {
            return ResponseEntity.status(403)
                    .body(Map.of(
                            "erro",
                            "Você não possui permissão para acessar estes dados."
                    ));
        }

        return ResponseEntity.ok(
                service.listarPorUsuario(usuario.getId())
        );
    }

    @GetMapping("/diario")
    public ResponseEntity<?> listarDiario(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate inicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fim,

            HttpSession session) {

        Usuario usuario = obterUsuarioLogado(session);

        if (usuario == null) {
            return ResponseEntity.status(401)
                    .body(Map.of(
                            "erro",
                            "Usuário não autenticado."
                    ));
        }

        try {

            List<ConsumoDiarioDTO> resultado =
                    service.listarTotaisDiarios(
                            usuario.getId(),
                            inicio,
                            fim
                    );

            return ResponseEntity.ok(resultado);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "erro",
                            e.getMessage()
                    ));
        }
    }

    @GetMapping(value = "/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCorrente() {
        return sseService.addEmitter();
    }

    /*
     * Endpoint legado. Agora também usa exclusivamente
     * o usuário autenticado.
     */
    @GetMapping("/historico")
    public ResponseEntity<?> getHistorico(
            HttpSession session) {

        Usuario usuario = obterUsuarioLogado(session);

        if (usuario == null) {
            return ResponseEntity.status(401)
                    .body(Map.of(
                            "erro",
                            "Usuário não autenticado."
                    ));
        }

        return ResponseEntity.ok(
                service.listarPorUsuario(usuario.getId())
        );
    }
}