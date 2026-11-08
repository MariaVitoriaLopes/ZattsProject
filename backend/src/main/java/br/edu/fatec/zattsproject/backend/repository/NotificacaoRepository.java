package br.edu.fatec.zattsproject.backend.repository;

import br.edu.fatec.zattsproject.backend.model.Notificacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository
        extends MongoRepository<Notificacao, String> {

    Page<Notificacao> findByUsuarioIdOrderByCriadaEmDesc(
            String usuarioId,
            Pageable pageable
    );

    Optional<Notificacao> findByIdAndUsuarioId(
            String id,
            String usuarioId
    );

    List<Notificacao> findByUsuarioIdAndLidaFalse(
            String usuarioId
    );

    long countByUsuarioIdAndLidaFalse(
            String usuarioId
    );
}