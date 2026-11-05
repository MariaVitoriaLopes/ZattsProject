package br.edu.fatec.zattsproject.backend.repository;

import br.edu.fatec.zattsproject.backend.model.Dispositivo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DispositivoRepository extends MongoRepository<Dispositivo, String> {

    Optional<Dispositivo> findByMacAddress(String macAddress);
}