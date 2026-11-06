package br.edu.fatec.zattsproject.backend.controller.mobile;

import br.edu.fatec.zattsproject.backend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mobile/dashboard")
public class DashboardMobileApiController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/resumo")
    public ResponseEntity<?> getDadosMobile(@RequestHeader("usuarioId") String usuarioId) {
        try {
            var dados = dashboardService.obterDadosDashboard(usuarioId);
            return ResponseEntity.ok(dados);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao carregar dados Mobile: " + e.getMessage());
        }
    }
}