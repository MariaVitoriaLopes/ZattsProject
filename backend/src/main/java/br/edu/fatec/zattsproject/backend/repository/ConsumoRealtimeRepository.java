package br.edu.fatec.zattsproject.backend.repository;

import br.edu.fatec.zattsproject.backend.model.ConsumoRealtime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsumoRealtimeRepository extends MongoRepository<ConsumoRealtime, String> {

    List<ConsumoRealtime> findByUsuarioIdAndDataHoraBetween(String usuarioId, LocalDateTime inicio, LocalDateTime fim);

}