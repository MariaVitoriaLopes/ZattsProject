package br.edu.fatec.zattsproject.backend.repository;

import br.edu.fatec.zattsproject.backend.model.Sessao;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SessaoRepository
        extends MongoRepository<Sessao, String> {

    Optional<Sessao> findBySessionId(String sessionId);

    List<Sessao> findByUsuarioIdAndAtivaTrueOrderByCriadoEmDesc(
            String usuarioId
    );

    void deleteByUsuarioId(String usuarioId);
}