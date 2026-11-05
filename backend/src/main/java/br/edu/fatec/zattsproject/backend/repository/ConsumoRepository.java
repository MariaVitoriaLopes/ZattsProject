package br.edu.fatec.zattsproject.backend.repository;

import br.edu.fatec.zattsproject.backend.model.Consumo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsumoRepository extends MongoRepository<Consumo, String> {

    List<Consumo> findByUsuarioIdOrderByDataHoraDesc(String usuarioId);

    List<Consumo> findByUsuarioIdAndDataHoraBetweenOrderByDataHoraDesc(
            String usuarioId,
            LocalDateTime inicio,
            LocalDateTime fim
    );

    long deleteByUsuarioId(String usuarioId);
}