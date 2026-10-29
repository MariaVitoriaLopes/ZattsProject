package br.edu.fatec.zattsproject.backend.repository;

import br.edu.fatec.zattsproject.backend.model.ContaEnergia;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ContaEnergiaRepository extends MongoRepository<ContaEnergia, String> {
    List<ContaEnergia> findByUsuarioId(String usuarioId);
}
