package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.ConfiguracaoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/configuracoes/notificacoes")
@RequiredArgsConstructor
public class NotificacoesController {

    private final ConfiguracaoService configuracaoService;

    @GetMapping("/preferencias")
    public ResponseEntity<?> buscarPreferencias(
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "erro",
                            "Sua sessão expirou. Faça login novamente."
                    ));
        }

        return ResponseEntity.ok(
                configuracaoService.buscarPreferenciasNotificacoes(
                        usuario.getId()
                )
        );
    }

    @PostMapping("/preferencias")
    public ResponseEntity<?> salvarPreferencias(
            @RequestBody Map<String, Boolean> preferencias,
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "erro",
                            "Sua sessão expirou. Faça login novamente."
                    ));
        }

        try {

            configuracaoService.salvarPreferenciasNotificacoes(
                    usuario.getId(),
                    preferencias
            );

            return ResponseEntity.ok(
                    Map.of(
                            "mensagem",
                            "Preferências de notificações salvas com sucesso."
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "erro",
                                    e.getMessage()
                            )
                    );
        }
    }
}
