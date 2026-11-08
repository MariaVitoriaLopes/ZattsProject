package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.dto.ConsumoDiarioDTO;
import br.edu.fatec.zattsproject.backend.model.Consumo;
import br.edu.fatec.zattsproject.backend.repository.ConsumoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumoService {

    private final ConsumoRepository repository;
    private final ConfiguracaoService configuracaoService;
    private final AlertaDisparoService alertaDisparoService;

    public Consumo salvar(Consumo consumo) {

        Consumo salvo =
                repository.save(consumo);

        if (salvo.getUsuarioId() != null
                && !salvo.getUsuarioId().isBlank()) {

            try {

                alertaDisparoService.avaliar(
                        salvo.getUsuarioId()
                );

            } catch (Exception e) {

                log.error(
                        "Consumo salvo, mas houve erro "
                                + "ao avaliar alertas do usuarioId={}",
                        salvo.getUsuarioId(),
                        e
                );
            }
        }

        return salvo;
    }

    public List<Consumo> listarPorUsuario(
            String usuarioId) {

        return repository
                .findByUsuarioIdOrderByDataHoraDesc(
                        usuarioId
                );
    }

    public List<Consumo> buscarPorUsuarioEIntervalo(
            String usuarioId,
            LocalDateTime inicio,
            LocalDateTime fim) {

        return repository
                .findByUsuarioIdAndDataHoraBetween(
                        usuarioId,
                        inicio,
                        fim
                );
    }

    public List<ConsumoDiarioDTO> listarTotaisDiarios(
            String usuarioId,
            LocalDate inicio,
            LocalDate fim) {

        if (usuarioId == null
                || usuarioId.isBlank()) {

            throw new IllegalArgumentException(
                    "Usuário não informado."
            );
        }

        if (inicio == null || fim == null) {

            throw new IllegalArgumentException(
                    "As datas de início e fim são obrigatórias."
            );
        }

        if (fim.isBefore(inicio)) {

            throw new IllegalArgumentException(
                    "A data final não pode ser anterior à data inicial."
            );
        }

        LocalDateTime inicioConsulta =
                inicio.atStartOfDay();

        LocalDateTime fimConsulta =
                fim.plusDays(1)
                        .atStartOfDay();

        List<Consumo> leituras =
                repository
                        .buscarPorUsuarioEIntervaloOrdenado(
                                usuarioId,
                                inicioConsulta,
                                fimConsulta
                        );

        Map<LocalDate, Double> consumoPorDia =
                new TreeMap<>();

        for (Consumo consumo : leituras) {

            if (consumo.getDataHora() == null) {
                continue;
            }

            double kwh =
                    consumo.getConsumoKwh() != null
                            ? consumo.getConsumoKwh()
                            : 0.0;

            LocalDate dia =
                    consumo.getDataHora()
                            .toLocalDate();

            consumoPorDia.merge(
                    dia,
                    kwh,
                    Double::sum
            );
        }

        double tarifaKwh =
                configuracaoService
                        .obterTarifaKwh(
                                usuarioId
                        );

        return consumoPorDia
                .entrySet()
                .stream()
                .map(entry -> {

                    double kwh =
                            arredondar(
                                    entry.getValue(),
                                    5
                            );

                    double reais =
                            arredondar(
                                    kwh * tarifaKwh,
                                    2
                            );

                    return new ConsumoDiarioDTO(
                            entry.getKey(),
                            kwh,
                            reais
                    );
                })
                .toList();
    }

    private double arredondar(
            double valor,
            int casas) {

        double fator =
                Math.pow(
                        10,
                        casas
                );

        return Math.round(
                valor * fator
        ) / fator;
    }

    public long apagarHistoricoPorUsuario(
            String usuarioId) {

        if (usuarioId == null
                || usuarioId.isBlank()) {

            throw new IllegalArgumentException(
                    "Usuário inválido."
            );
        }

        return repository
                .deleteByUsuarioId(
                        usuarioId
                );
    }
}
