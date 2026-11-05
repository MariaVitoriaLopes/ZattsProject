package br.edu.fatec.zattsproject.backend.repository;

import br.edu.fatec.zattsproject.backend.model.Consumo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsumoRepository extends MongoRepository<Consumo, String> {

    List<Consumo> findByUsuarioId(String usuarioId);

    List<Consumo> findByUsuarioIdOrderByDataHoraDesc(String usuarioId);

    List<Consumo> findByUsuarioIdAndDataHoraBetween(String usuarioId, LocalDateTime inicio, LocalDateTime fim);
}