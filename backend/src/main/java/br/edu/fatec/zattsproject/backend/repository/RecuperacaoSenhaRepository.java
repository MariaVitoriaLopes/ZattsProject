
package br.edu.fatec.zattsproject.backend.repository;

import br.edu.fatec.zattsproject.backend.model.RecuperacaoSenha;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RecuperacaoSenhaRepository
        extends MongoRepository<RecuperacaoSenha, String> {

    Optional<RecuperacaoSenha> findByTokenHash(String tokenHash);

    void deleteByUsuarioId(String usuarioId);
}
