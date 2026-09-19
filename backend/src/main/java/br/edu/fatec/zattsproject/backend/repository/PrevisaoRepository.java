package br.edu.fatec.zattsproject.backend.repository;

import br.edu.fatec.zattsproject.backend.model.Previsao;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PrevisaoRepository extends MongoRepository<Previsao, String> {
    List<Previsao> findByUsuarioId(String usuarioId);
}