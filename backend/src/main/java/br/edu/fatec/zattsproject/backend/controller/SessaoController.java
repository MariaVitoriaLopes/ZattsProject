package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.model.Sessao;
import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.SessaoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/configuracoes/dispositivos")
public class SessaoController {

    private final SessaoService sessaoService;

    @PostMapping("/{id}/desconectar")
    @ResponseBody
    public ResponseEntity<?> desconectar(
            @PathVariable String id,
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute(
                        "usuarioLogado"
                );

        if (usuario == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "erro",
                                    "Sua sessão expirou."
                            )
                    );
        }

        Optional<Sessao> sessaoOpt =
                sessaoService.buscarPorId(id);

        if (sessaoOpt.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "erro",
                                    "Sessão não encontrada."
                            )
                    );
        }

        Sessao sessaoAlvo =
                sessaoOpt.get();

        if (!Objects.equals(
                sessaoAlvo.getUsuarioId(),
                usuario.getId())) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "erro",
                                    "Você não pode desconectar esta sessão."
                            )
                    );
        }

        boolean atual =
                Objects.equals(
                        sessaoAlvo.getSessionId(),
                        session.getId()
                );

        sessaoService.desativar(
                sessaoAlvo
        );

        if (atual) {
            session.invalidate();
        }

        return ResponseEntity.ok(
                Map.of(
                        "atual",
                        atual
                )
        );
    }
}