
package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.Consumo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ConsumoAcumuladorService {

    private final SseService sseService;
    private final ConsumoService consumoService;

    /*
     * Armazena temporariamente o consumo de cada dispositivo
     * durante a hora atual.
     *
     * A chave identifica o dispositivo e a hora da medição.
     */
    private final Map<String, Consumo> acumuladores = new ConcurrentHashMap<>();

    public void processar(Consumo leitura) {
        if (leitura == null || leitura.getDispositivoId() == null) {
            return;
        }

        if (leitura.getConsumoKwh() == null) {
            System.err.println("Leitura ignorada: consumoKwh não informado.");
            return;
        }

        LocalDateTime dataHora = leitura.getDataHora() != null ? leitura.getDataHora() : LocalDateTime.now();

        leitura.setDataHora(dataHora);

        LocalDateTime inicioHora = dataHora.truncatedTo(ChronoUnit.HOURS);

        String chave = leitura.getDispositivoId() + "_" + inicioHora;

        Consumo acumulado = acumuladores.compute(chave, (key, atual) -> {
            if (atual == null) {
                return criarAcumulador(leitura, inicioHora);
            }

            double consumoAnterior = atual.getConsumoKwh();

            atual.setConsumoKwh(consumoAnterior + leitura.getConsumoKwh());

            atual.setCorrenteAmperes(leitura.getCorrenteAmperes());
            atual.setVoltagem(leitura.getVoltagem());
            atual.setPotenciaWatts(leitura.getPotenciaWatts());
            atual.setValorEstimadoReais(leitura.getValorEstimadoReais());
            atual.setDataHora(dataHora);

            return atual;
        });

        // Envia o acumulado parcial ao gráfico via SSE.
        sseService.sendConsumoToAll(acumulado);
    }

    private Consumo criarAcumulador(Consumo leitura, LocalDateTime inicioHora) {
        return Consumo.builder()
                .dispositivoId(leitura.getDispositivoId())
                .usuarioId(leitura.getUsuarioId())
                .correnteAmperes(leitura.getCorrenteAmperes())
                .voltagem(leitura.getVoltagem())
                .potenciaWatts(leitura.getPotenciaWatts())
                .consumoKwh(leitura.getConsumoKwh())
                .valorEstimadoReais(leitura.getValorEstimadoReais())
                .dataHora(inicioHora)
                .build();
    }

    /*
     * Executa no início de cada hora.
     * Persiste os acumulados da hora que acabou de terminar.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void salvarAcumuladosDaHoraAnterior() {
        LocalDateTime horaAtual =
                LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);

        acumuladores.entrySet().removeIf(entry -> {
            Consumo consumo = entry.getValue();

            if (consumo.getDataHora().isBefore(horaAtual)) {
                try {
                    consumo.setDataHora(consumo.getDataHora());
                    consumoService.salvar(consumo);

                    System.out.println(
                            "Consumo horário salvo: dispositivo="
                                    + consumo.getDispositivoId()
                                    + ", hora=" + consumo.getDataHora()
                                    + ", kWh=" + consumo.getConsumoKwh()
                    );

                    return true;
                } catch (Exception e) {
                    System.err.println("Erro ao salvar consumo horário: " + e.getMessage());
                    return false;
                }
            }
            return false;
        });
    }
}
