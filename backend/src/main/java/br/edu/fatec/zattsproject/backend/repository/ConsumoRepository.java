package br.edu.fatec.zattsproject.backend.repository;

import br.edu.fatec.zattsproject.backend.model.Consumo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsumoRepository extends MongoRepository<Consumo, String> {

    // Lista todos os consumos de um utilizador
    List<Consumo> findByUsuarioId(String usuarioId);

    // Lista consumos ordenados do mais recente para o mais antigo
    List<Consumo> findByUsuarioIdOrderByDataHoraDesc(String usuarioId);

    // Lista consumos dentro de um intervalo de datas (para relatórios/histórico)
    List<Consumo> findByUsuarioIdAndDataHoraBetween(String usuarioId, LocalDateTime inicio, LocalDateTime fim);

    // Retorna a ÚLTIMA leitura recebida de um utilizador específico (Usado no Dashboard)
    Optional<Consumo> findTopByUsuarioIdOrderByDataHoraDesc(String usuarioId);

    // Retorna a ÚLTIMA leitura geral do sistema (Caso o MQTT ainda não grave o usuarioId de imediato)
    Optional<Consumo> findTopByOrderByDataHoraDesc();

    @Query(
            value = "{ 'usuarioId': ?0, 'dataHora': { $gte: ?1, $lt: ?2 } }",
            sort = "{ 'dataHora': 1 }"
    )
    List<Consumo> buscarPorUsuarioEIntervaloOrdenado(
            String usuarioId,
            LocalDateTime inicio,
            LocalDateTime fim
    );
}