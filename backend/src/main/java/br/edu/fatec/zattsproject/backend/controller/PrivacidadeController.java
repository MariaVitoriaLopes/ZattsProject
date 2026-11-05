package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.model.Configuracao;
import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.ConfiguracaoService;
import br.edu.fatec.zattsproject.backend.service.ConsumoService;
import br.edu.fatec.zattsproject.backend.service.AlertaService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import java.util.LinkedHashMap;

import java.util.Map;

@RestController
@RequestMapping("/configuracoes/privacidade")
@RequiredArgsConstructor
public class PrivacidadeController {

    private final ConfiguracaoService configuracaoService;
    private final ConsumoService consumoService;
    private final AlertaService alertaService;

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

        Configuracao configuracao =
                configuracaoService.buscarPrivacidade(
                        usuario.getId()
                );

        return ResponseEntity.ok(
                Map.of(
                        "compartilharDadosUso",
                        configuracao.getCompartilharDadosUso(),
                        "recomendacoesPersonalizadas",
                        configuracao.getRecomendacoesPersonalizadas(),
                        "comparacaoResidencias",
                        configuracao.getComparacaoResidencias()
                )
        );
    }

    @PostMapping("/preferencias")
    public ResponseEntity<?> salvarPreferencias(
            @RequestParam boolean compartilharDadosUso,
            @RequestParam boolean recomendacoesPersonalizadas,
            @RequestParam boolean comparacaoResidencias,
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

        configuracaoService.salvarPrivacidade(
                usuario.getId(),
                compartilharDadosUso,
                recomendacoesPersonalizadas,
                comparacaoResidencias
        );

        return ResponseEntity.ok(
                Map.of(
                        "mensagem",
                        "Preferências de privacidade salvas com sucesso."
                )
        );
    }

    @GetMapping("/exportar")
    public ResponseEntity<?> exportarDados(
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

        Map<String, Object> conta = new LinkedHashMap<>();

        conta.put("id", usuario.getId());
        conta.put("nome", usuario.getNome());
        conta.put("email", usuario.getEmail());
        conta.put("telefone", usuario.getTelefone());
        conta.put("criadoEm", usuario.getCriadoEm());
        conta.put(
                "questionarioRespondido",
                usuario.isQuestionarioRespondido()
        );

        Map<String, Object> dadosExportacao =
                new LinkedHashMap<>();

        dadosExportacao.put("conta", conta);

        dadosExportacao.put(
                "configuracoes",
                configuracaoService.buscarPorUsuario(
                        usuario.getId()
                )
        );

        dadosExportacao.put(
                "consumos",
                consumoService.listarPorUsuario(
                        usuario.getId()
                )
        );

        dadosExportacao.put(
                "alertas",
                alertaService.listar(
                        usuario.getId()
                )
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(
                        "Content-Disposition",
                        "attachment; filename=\"zatts-dados.json\""
                )
                .body(dadosExportacao);
    }

    @PostMapping("/historico/excluir")
    public ResponseEntity<?> apagarHistorico(
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

        long quantidadeExcluida =
                consumoService.apagarHistoricoPorUsuario(
                        usuario.getId()
                );

        return ResponseEntity.ok(
                Map.of(
                        "mensagem",
                        "Histórico de consumo apagado com sucesso.",
                        "registrosExcluidos",
                        quantidadeExcluida
                )
        );
    }
}
