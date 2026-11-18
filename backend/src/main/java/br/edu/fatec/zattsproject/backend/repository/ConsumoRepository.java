package br.edu.fatec.zattsproject.backend.repository;

import br.edu.fatec.zattsproject.backend.model.Consumo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsumoRepository extends MongoRepository<Consumo, String> {

    List<Consumo> findByUsuarioId(String usuarioId);

    List<Consumo> findByUsuarioIdOrderByDataHoraDesc(String usuarioId);

    List<Consumo> findByUsuarioIdAndDataHoraBetween(String usuarioId, LocalDateTime inicio, LocalDateTime fim);

    List<Consumo> findByDispositivoIdAndDataHoraBetween(String dispositivoId, LocalDateTime inicio, LocalDateTime fim);

    Optional<Consumo> findTopByUsuarioIdOrderByDataHoraDesc(String usuarioId);

    Optional<Consumo> findTopByOrderByDataHoraDesc();

    @Query(
            value = "{ 'usuarioId': ?0, 'dataHora': { $gte: ?1, $lt: ?2 } }",
            sort = "{ 'dataHora': 1 }"
    )
    List<Consumo> buscarPorUsuarioEIntervaloOrdenado(
            String usuarioId,
            LocalDateTime inicio,
            LocalDateTime fim
    );

    long deleteByUsuarioId(String usuarioId);
}