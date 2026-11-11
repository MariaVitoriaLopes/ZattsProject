package br.edu.fatec.zattsproject.backend.controller.mobile;

import br.edu.fatec.zattsproject.backend.dto.SessaoDTO;
import br.edu.fatec.zattsproject.backend.model.Sessao;
import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.SessaoService;

import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/mobile/configuracoes/dispositivos")
@RequiredArgsConstructor
public class SessaoMobileController {

    private final SessaoService sessaoService;

    @GetMapping
    public ResponseEntity<?> listar(
            HttpSession session
    ) {

        Usuario usuario =
                (Usuario) session.getAttribute(
                        "usuarioLogado"
                );

        if (usuario == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        List<SessaoDTO> sessoes =
                sessaoService
                        .listarAtivasParaTela(
                                usuario.getId(),
                                session.getId()
                        );

        return ResponseEntity.ok(
                sessoes
        );
    }

    @PostMapping("/{id}/desconectar")
    public ResponseEntity<?> desconectar(
            @PathVariable String id,
            HttpSession session
    ) {

        Usuario usuario =
                (Usuario) session.getAttribute(
                        "usuarioLogado"
                );

        if (usuario == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
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
                usuario.getId()
        )) {

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