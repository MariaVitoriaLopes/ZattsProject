package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.Consumo;
import br.edu.fatec.zattsproject.backend.repository.ConsumoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsumoService {

    private final ConsumoRepository repository;

    public Consumo salvar(Consumo consumo) {
        return repository.save(consumo);
    }

    public List<Consumo> listarPorUsuario(String usuarioId) {
        return repository.findByUsuarioIdOrderByDataHoraDesc(usuarioId);
    }

    public List<Consumo> listarHistorico(
            String usuarioId,
            String periodo) {

        if (usuarioId == null || usuarioId.isBlank()) {
            throw new IllegalArgumentException(
                    "Usuário inválido."
            );
        }

        String filtro =
                periodo == null
                        ? "todos"
                        : periodo.trim().toLowerCase();

        LocalDateTime agora = LocalDateTime.now();

        return switch (filtro) {

            case "dia", "diario" -> {

                LocalDateTime inicio =
                        agora.toLocalDate()
                                .atStartOfDay();

                yield repository
                        .findByUsuarioIdAndDataHoraBetweenOrderByDataHoraDesc(
                                usuarioId,
                                inicio,
                                agora
                        );
            }

            case "semana", "semanal" -> {

                LocalDateTime inicio =
                        agora.toLocalDate()
                                .minusDays(6)
                                .atStartOfDay();

                yield repository
                        .findByUsuarioIdAndDataHoraBetweenOrderByDataHoraDesc(
                                usuarioId,
                                inicio,
                                agora
                        );
            }

            case "mes", "mensal" -> {

                LocalDateTime inicio =
                        agora.toLocalDate()
                                .withDayOfMonth(1)
                                .atStartOfDay();

                yield repository
                        .findByUsuarioIdAndDataHoraBetweenOrderByDataHoraDesc(
                                usuarioId,
                                inicio,
                                agora
                        );
            }

            case "todos" ->
                    repository
                            .findByUsuarioIdOrderByDataHoraDesc(
                                    usuarioId
                            );

            default -> throw new IllegalArgumentException(
                    "Período inválido."
            );
        };
    }

    public long apagarHistoricoPorUsuario(String usuarioId) {
        return repository.deleteByUsuarioId(usuarioId);
    }
}