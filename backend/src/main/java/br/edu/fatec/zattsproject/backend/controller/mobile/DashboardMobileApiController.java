package br.edu.fatec.zattsproject.backend.controller.mobile;

import br.edu.fatec.zattsproject.backend.dto.DashboardDTO;
import br.edu.fatec.zattsproject.backend.service.ConfiguracaoService;
import br.edu.fatec.zattsproject.backend.service.ConsumoService;
import br.edu.fatec.zattsproject.backend.service.DashboardService;
import br.edu.fatec.zattsproject.backend.service.MqttLeituraService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/mobile/dashboard")
@RequiredArgsConstructor
public class DashboardMobileApiController {

    private final ConsumoService consumoService;
    private final MqttLeituraService mqttLeituraService;
    private final ConfiguracaoService configuracaoService;
    private final DashboardService dashboardService;

    @GetMapping("/resumo")
    public ResponseEntity<?> getDadosMobile(@RequestHeader("usuarioId") String usuarioId) {
        try {
            DashboardDTO dto = consumoService.obterDadosDashboard(usuarioId);

            // Recupera a estimativa do mês atual para o card mobile.
            if (dto.getValorContaEstimadoReais() != null
                    && !dto.getValorContaEstimadoReais().isEmpty()) {

                var ultimoMes = dto.getValorContaEstimadoReais()
                        .get(dto.getValorContaEstimadoReais().size() - 1);

                dto.setValorContaEstimadoMesReais(
                        ultimoMes.getValor()
                );
            }

            double emMemoria = mqttLeituraService.obterConsumoAcumuladoHoraAtual(usuarioId);
            double totalDiario = dto.getConsumoTotalDiarioKwh() + emMemoria;
            double totalMensal = dto.getMetaConsumoMensalAtualKwh() + emMemoria;

            dto.setConsumoTotalDiarioKwh(Math.round(totalDiario * 100000.0) / 100000.0);
            dto.setMetaConsumoMensalAtualKwh(Math.round(totalMensal * 100000.0) / 100000.0);

            // o app lê o custo em custoDiarioReais (o web usa outro campo)
            double tarifa = configuracaoService.obterTarifaKwh(usuarioId);
            dto.setCustoDiarioReais(Math.round(totalDiario * tarifa * 100.0) / 100.0);

            double limite = dto.getMetaConsumoMensalLimiteKwh();
            if (limite > 0) {
                dto.setPercentualMetaAtingida(Math.round(totalMensal / limite * 1000.0) / 10.0);
            }

            var pontos = mqttLeituraService.obterPontosGraficoTempoReal(usuarioId);
            if (pontos != null && !pontos.isEmpty()) {
                dto.setConsumoTempoReal(pontos);
            }

            System.out.println("===== TESTE DASHBOARD MOBILE =====");
            System.out.println("Usuário solicitado: " + usuarioId);
            System.out.println("Pontos recebidos: " + pontos.size());

            pontos.forEach(p ->
                    System.out.println(
                            "Hora: " + p.getHora() +
                                    " | Consumo: " + p.getConsumoKwh()
                    )
            );

            System.out.println("==================================");

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao carregar dados Mobile: " + e.getMessage());
        }
    }
}