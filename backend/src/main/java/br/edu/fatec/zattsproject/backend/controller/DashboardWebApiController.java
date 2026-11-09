package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.dto.DashboardDTO;
import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.ConsumoService;
import br.edu.fatec.zattsproject.backend.service.MqttLeituraService;
import br.edu.fatec.zattsproject.backend.service.SseService;
import br.edu.fatec.zattsproject.backend.service.ConfiguracaoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/web/dashboard")
@RequiredArgsConstructor
public class DashboardWebApiController {

    private final SseService sseService;
    private final ConsumoService consumoService;
    private final MqttLeituraService mqttLeituraService;
    private final ConfiguracaoService configuracaoService;

    private String obterUsuarioId(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        return (usuario != null && usuario.getId() != null)
                ? usuario.getId()
                : "6ae2afdbe3f4de30aa7d8f38";
    }

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter inscreverSse(HttpSession session) {
        return sseService.addEmitter();
    }

    @GetMapping("/dados")
    public ResponseEntity<DashboardDTO> obterDadosDashboard(HttpSession session) {
        String usuarioId = obterUsuarioId(session);

        DashboardDTO dto = consumoService.obterDadosDashboard(usuarioId);

        // 1. Consumo em memória ainda não persistido (MQTT a cada 5s)
        double emMemoria = mqttLeituraService.obterConsumoAcumuladoHoraAtual(usuarioId);

        // 2. Soma histórico permanente (Banco) + memória recente (MQTT)
        double totalDiario = dto.getConsumoTotalDiarioKwh() + emMemoria;
        double totalMensal = dto.getMetaConsumoMensalAtualKwh() + emMemoria;

        // 3. Atualiza os kWh no DTO
        dto.setConsumoTotalDiarioKwh(Math.round(totalDiario * 100000.0) / 100000.0);
        dto.setMetaConsumoMensalAtualKwh(Math.round(totalMensal * 100000.0) / 100000.0);

        // 4. RECALCULA O VALOR EM REAIS DO CARD USANDO O MÉTODO EXISTENTE NO DTO
        double tarifaKwh = configuracaoService.obterTarifaKwh(usuarioId);
        double custoAtualizado = totalDiario * tarifaKwh;
        dto.setMetaGastoKwh(Math.round(custoAtualizado * 100.0) / 100.0);

        // 5. RECALCULA A PORCENTAGEM DA META DINAMICAMENTE
        double limiteMeta = dto.getMetaConsumoMensalLimiteKwh();
        if (limiteMeta > 0) {
            double percentual = (totalMensal / limiteMeta) * 100.0;
            dto.setPercentualMetaAtingida(Math.round(percentual * 10.0) / 10.0);
        }

        // 6. Gráfico Tempo Real
        List<DashboardDTO.PontoGraficoMinuto> pontosMemoria = mqttLeituraService.obterPontosGraficoTempoReal(usuarioId);
        if (pontosMemoria != null && !pontosMemoria.isEmpty()) {
            dto.setConsumoTempoReal(pontosMemoria);
        }

        return ResponseEntity.ok(dto);
    }
}