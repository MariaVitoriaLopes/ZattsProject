package br.edu.fatec.zattsproject.backend.repository;

import br.edu.fatec.zattsproject.backend.model.Alerta;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

    public interface AlertaRepository extends MongoRepository<Alerta, String> {
        List<Alerta> findByUsuarioIdAndAtivoTrue(String usuarioId);
    }

