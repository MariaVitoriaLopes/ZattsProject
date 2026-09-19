package br.edu.fatec.zattsproject.backend.repository;

import br.edu.fatec.zattsproject.backend.model.Consumo;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ConsumoRepository extends MongoRepository<Consumo, String> {
    List<Consumo> findByUsuarioIdOrderByDataHoraDesc(String usuarioId);
}