package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.NotificacaoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @PostMapping("/{id}/lida")
    public ResponseEntity<?> marcarComoLida(
            @PathVariable String id,
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

        boolean marcada = notificacaoService.marcarComoLida(
                id,
                usuario.getId()
        );

        if (!marcada) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                Map.of("mensagem", "Notificação marcada como lida.")
        );
    }

    @PostMapping("/lidas")
    public ResponseEntity<?> marcarTodasComoLidas(
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

        notificacaoService.marcarTodasComoLidas(
                usuario.getId()
        );

        return ResponseEntity.ok(
                Map.of("mensagem", "Notificações marcadas como lidas.")
        );
    }

    @GetMapping("/nao-lidas/contagem")
    public ResponseEntity<?> contarNaoLidas(
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

        long quantidade =
                notificacaoService.contarNaoLidas(
                        usuario.getId()
                );

        return ResponseEntity.ok(
                Map.of("quantidade", quantidade)
        );
    }
}