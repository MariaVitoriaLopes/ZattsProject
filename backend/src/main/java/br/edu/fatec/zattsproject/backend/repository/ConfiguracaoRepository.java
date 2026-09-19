package br.edu.fatec.zattsproject.backend.repository;

import br.edu.fatec.zattsproject.backend.model.Configuracao;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ConfiguracaoRepository extends MongoRepository<Configuracao, String> {
    Optional<Configuracao> findByUsuarioId(String usuarioId);
}