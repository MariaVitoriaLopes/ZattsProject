package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.dto.DashboardDTO;
import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.DashboardService;
import br.edu.fatec.zattsproject.backend.service.SseService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/web/dashboard")
@RequiredArgsConstructor
public class DashboardWebApiController {

    private final SseService sseService;
    private final DashboardService dashboardService;

    private String obterUsuarioId(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        return (usuario != null && usuario.getId() != null)
                ? usuario.getId()
                : "6ae2afdbe3f4de30aa7d8f38"; // Fallback para testes
    }

    /**
     * Endpoint SSE que mantém a conexão aberta com o navegador.
     */
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter inscreverSse(HttpSession session) {
        return sseService.addEmitter();
    }

    /**
     * Endpoint REST consultado via fetch() no frontend após o evento SSE disparar.
     */
    @GetMapping("/dados")
    public ResponseEntity<DashboardDTO> obterDadosDashboard(HttpSession session) {
        String usuarioId = obterUsuarioId(session);
        DashboardDTO dto = dashboardService.obterDadosDashboard(usuarioId);
        return ResponseEntity.ok(dto);
    }
}