package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.model.Configuracao;
import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.ConfiguracaoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/configuracoes/aparencia")
public class AparenciaController {

    private final ConfiguracaoService configuracaoService;

    @GetMapping
    public ResponseEntity<?> buscar(HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return ResponseEntity.status(401)
                    .body(Map.of(
                            "erro",
                            "Usuário não autenticado."
                    ));
        }

        Configuracao configuracao =
                configuracaoService.buscarAparencia(
                        usuario.getId()
                );

        return ResponseEntity.ok(Map.of(
                "tema", configuracao.getTema(),
                "corDestaque", configuracao.getCorDestaque(),
                "corPersonalizada", configuracao.getCorPersonalizada(),
                "idioma", configuracao.getIdioma()
        ));
    }

    @PostMapping
    public ResponseEntity<?> salvar(
            @RequestBody Map<String, String> dados,
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return ResponseEntity.status(401)
                    .body(Map.of(
                            "erro",
                            "Usuário não autenticado."
                    ));
        }

        try {

            configuracaoService.salvarAparencia(
                    usuario.getId(),
                    dados.get("tema"),
                    dados.get("corDestaque"),
                    dados.get("corPersonalizada"),
                    dados.get("idioma")
            );

            return ResponseEntity.ok(Map.of(
                    "mensagem",
                    "Preferências salvas com sucesso."
            ));

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "erro",
                            e.getMessage()
                    ));
        }
    }
}