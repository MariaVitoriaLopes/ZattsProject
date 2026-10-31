
package br.edu.fatec.zattsproject.backend.repository;

import br.edu.fatec.zattsproject.backend.model.Alerta;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AlertaRepository extends MongoRepository<Alerta, String> {

    List<Alerta> findByUsuarioId(String usuarioId);

    List<Alerta> findByUsuarioIdAndAtivoTrue(String usuarioId);

    Optional<Alerta> findByIdAndUsuarioId(String id, String usuarioId);
}
