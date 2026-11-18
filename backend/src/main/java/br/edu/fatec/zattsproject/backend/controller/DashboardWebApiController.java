package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.dto.DashboardDTO;
import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.ConfiguracaoService;
import br.edu.fatec.zattsproject.backend.service.ConsumoService;
import br.edu.fatec.zattsproject.backend.service.MqttLeituraService;
import br.edu.fatec.zattsproject.backend.service.SseService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.YearMonth;
import java.time.ZoneId;
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

        // 3. Projeção oficial mensal idêntica à do /historico (TE + TUSD + ICMS + PIS/COFINS + Bandeira + CIP)
        YearMonth mesAtual = YearMonth.now(ZoneId.of("America/Sao_Paulo"));
        double valorContaAtualizado = consumoService.calcularEstimativaMes(usuarioId, mesAtual, totalMensal);

        dto.setValorContaEstimadoMesReais(valorContaAtualizado);
        if (dto.getValorContaEstimadoReais() != null && !dto.getValorContaEstimadoReais().isEmpty()) {
            dto.getValorContaEstimadoReais().get(dto.getValorContaEstimadoReais().size() - 1)
                    .setValor(valorContaAtualizado);
        }

        // 4. Atualiza os kWh no DTO a cada 5 segundos
        dto.setConsumoTotalDiarioKwh(Math.round(totalDiario * 100000.0) / 100000.0);
        dto.setMetaConsumoMensalAtualKwh(Math.round(totalMensal * 100000.0) / 100000.0);

        // 5. Recalcula o custo diário em R$
        double tarifaKwh = configuracaoService.obterTarifaKwh(usuarioId);
        if (tarifaKwh <= 0) tarifaKwh = 0.85;
        double custoAtualizado = totalDiario * tarifaKwh;
        dto.setMetaGastoKwh(Math.round(custoAtualizado * 100.0) / 100.0);

        // 6. Recalcula a porcentagem da meta dinamicamente
        double limiteMeta = dto.getMetaConsumoMensalLimiteKwh();
        if (limiteMeta > 0) {
            double percentual = (totalMensal / limiteMeta) * 100.0;
            dto.setPercentualMetaAtingida(Math.round(percentual * 10.0) / 10.0);
        }

        // 7. Gráfico Tempo Real (últimos 10 minutos)
        List<DashboardDTO.PontoGraficoMinuto> pontosMemoria = mqttLeituraService.obterPontosGraficoTempoReal(usuarioId);
        if (pontosMemoria != null && !pontosMemoria.isEmpty()) {
            dto.setConsumoTempoReal(pontosMemoria);
        }

        return ResponseEntity.ok(dto);
    }
}