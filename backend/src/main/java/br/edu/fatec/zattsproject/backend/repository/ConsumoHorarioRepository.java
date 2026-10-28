package br.edu.fatec.zattsproject.backend.repository;

import br.edu.fatec.zattsproject.backend.model.ConsumoHorario;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConsumoHorarioRepository extends MongoRepository<ConsumoHorario, String> {
}