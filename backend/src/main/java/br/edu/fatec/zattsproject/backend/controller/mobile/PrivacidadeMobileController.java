package br.edu.fatec.zattsproject.backend.controller.mobile;

import br.edu.fatec.zattsproject.backend.model.Configuracao;
import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.AlertaService;
import br.edu.fatec.zattsproject.backend.service.ConfiguracaoService;
import br.edu.fatec.zattsproject.backend.service.ConsumoService;

import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile/configuracoes/privacidade")
@RequiredArgsConstructor
public class PrivacidadeMobileController {

    private final ConfiguracaoService configuracaoService;
    private final ConsumoService consumoService;
    private final AlertaService alertaService;

    public record PrivacidadeRequest(
            boolean compartilharDadosUso,
            boolean recomendacoesPersonalizadas,
            boolean comparacaoResidencias
    ) {}

    public record PrivacidadeResponse(
            boolean compartilharDadosUso,
            boolean recomendacoesPersonalizadas,
            boolean comparacaoResidencias
    ) {}

    @GetMapping
    public ResponseEntity<?> buscar(
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
                configuracaoService.buscarPrivacidade(
                        usuario.getId()
                );

        return ResponseEntity.ok(
                new PrivacidadeResponse(
                        configuracao.getCompartilharDadosUso(),
                        configuracao.getRecomendacoesPersonalizadas(),
                        configuracao.getComparacaoResidencias()
                )
        );
    }

    @PutMapping
    public ResponseEntity<?> salvar(
            @RequestBody PrivacidadeRequest dados,
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
                configuracaoService.salvarPrivacidade(
                        usuario.getId(),
                        dados.compartilharDadosUso(),
                        dados.recomendacoesPersonalizadas(),
                        dados.comparacaoResidencias()
                );

        return ResponseEntity.ok(
                new PrivacidadeResponse(
                        configuracao.getCompartilharDadosUso(),
                        configuracao.getRecomendacoesPersonalizadas(),
                        configuracao.getComparacaoResidencias()
                )
        );
    }

    @GetMapping("/exportar")
    public ResponseEntity<?> exportar(
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

        Map<String, Object> conta =
                new LinkedHashMap<>();

        conta.put("id", usuario.getId());
        conta.put("nome", usuario.getNome());
        conta.put("email", usuario.getEmail());
        conta.put("telefone", usuario.getTelefone());
        conta.put("criadoEm", usuario.getCriadoEm());
        conta.put(
                "questionarioRespondido",
                usuario.isQuestionarioRespondido()
        );

        Map<String, Object> dados =
                new LinkedHashMap<>();

        dados.put("conta", conta);

        dados.put(
                "configuracoes",
                configuracaoService.buscarPorUsuario(
                        usuario.getId()
                )
        );

        dados.put(
                "consumos",
                consumoService.listarPorUsuario(
                        usuario.getId()
                )
        );

        dados.put(
                "alertas",
                alertaService.listar(
                        usuario.getId()
                )
        );

        return ResponseEntity.ok()
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .header(
                        "Content-Disposition",
                        "attachment; filename=\"zatts-dados.json\""
                )
                .body(dados);
    }

    @PostMapping("/apagar-historico")
    public ResponseEntity<?> apagarHistorico(
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

        long quantidadeExcluida =
                consumoService
                        .apagarHistoricoPorUsuario(
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