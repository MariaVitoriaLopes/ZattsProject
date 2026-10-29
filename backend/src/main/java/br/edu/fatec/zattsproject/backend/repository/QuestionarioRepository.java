package br.edu.fatec.zattsproject.backend.repository;


import br.edu.fatec.zattsproject.backend.model.Questionario;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuestionarioRepository extends MongoRepository<Questionario, String> {
        List<Questionario> findByUsuarioId(String usuarioId);
    }



