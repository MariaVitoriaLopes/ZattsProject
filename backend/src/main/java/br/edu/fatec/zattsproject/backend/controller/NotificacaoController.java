package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.NotificacaoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.edu.fatec.zattsproject.backend.model.Notificacao;
import org.springframework.data.domain.PageRequest;

import java.util.Map;

@RestController
@RequestMapping("/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @GetMapping("/api")
    public ResponseEntity<?> listarNotificacoes(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "50") int tamanho,
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
        int paginaSegura = Math.max(pagina, 0);
        int tamanhoSeguro =
                Math.min(Math.max(tamanho, 1), 100);
        return ResponseEntity.ok(
                notificacaoService.listar(
                        usuario.getId(),
                        PageRequest.of(
                                paginaSegura,
                                tamanhoSeguro
                        )
                ).getContent()
        );
    }

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
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(
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

        boolean excluida = notificacaoService.excluir(
                id,
                usuario.getId()
        );

        if (!excluida) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<?> excluirTodas(
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

        notificacaoService.excluirTodas(
                usuario.getId()
        );

        return ResponseEntity.noContent().build();
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