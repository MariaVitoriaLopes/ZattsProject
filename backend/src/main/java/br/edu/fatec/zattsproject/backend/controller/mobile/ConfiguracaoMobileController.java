package br.edu.fatec.zattsproject.backend.controller.mobile;

import br.edu.fatec.zattsproject.backend.model.Configuracao;
import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.ConfiguracaoService;

import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/configuracoes")
@RequiredArgsConstructor
public class ConfiguracaoMobileController {

    private final ConfiguracaoService configuracaoService;

    public record NotificacoesResponse(
            boolean notifGerais,
            boolean notifConta,
            boolean notifConsumo,
            boolean notifMeta,
            boolean notifAlerta
    ) {}

    public record NotificacoesRequest(
            boolean notifGerais,
            boolean notifConta,
            boolean notifConsumo,
            boolean notifMeta,
            boolean notifAlerta
    ) {}

    @GetMapping("/notificacoes")
    public ResponseEntity<?> buscarNotificacoes(
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

        Configuracao configuracao =
                configuracaoService
                        .buscarPorUsuario(
                                usuario.getId()
                        );

        if (configuracao == null) {

            return ResponseEntity.ok(
                    new NotificacoesResponse(
                            true,
                            true,
                            true,
                            true,
                            true
                    )
            );
        }

        return ResponseEntity.ok(
                new NotificacoesResponse(
                        valorPadrao(
                                configuracao.getNotifGerais()
                        ),
                        valorPadrao(
                                configuracao.getNotifConta()
                        ),
                        valorPadrao(
                                configuracao.getNotifConsumo()
                        ),
                        valorPadrao(
                                configuracao.getNotifMeta()
                        ),
                        valorPadrao(
                                configuracao.getNotifAlerta()
                        )
                )
        );
    }

    @PutMapping("/notificacoes")
    public ResponseEntity<?> salvarNotificacoes(
            @RequestBody NotificacoesRequest dados,
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

        Configuracao configuracao =
                configuracaoService
                        .salvarNotificacoes(
                                usuario.getId(),
                                dados.notifGerais(),
                                dados.notifConta(),
                                dados.notifAlerta(),
                                dados.notifConsumo(),
                                dados.notifMeta()
                        );

        return ResponseEntity.ok(
                new NotificacoesResponse(
                        valorPadrao(
                                configuracao.getNotifGerais()
                        ),
                        valorPadrao(
                                configuracao.getNotifConta()
                        ),
                        valorPadrao(
                                configuracao.getNotifConsumo()
                        ),
                        valorPadrao(
                                configuracao.getNotifMeta()
                        ),
                        valorPadrao(
                                configuracao.getNotifAlerta()
                        )
                )
        );
    }

    private boolean valorPadrao(
            Boolean valor
    ) {

        return valor == null || valor;
    }
}