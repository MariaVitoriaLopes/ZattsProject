package br.edu.fatec.zattsproject.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "consumo")
public class Consumo {

    @Id
    private String id;

    private String dispositivoId;
    private String usuarioId;

    private Double correnteAmperes;
    private Double voltagem;
    private Double potenciaWatts;
    private Double consumoKwh;
    private Double valorEstimadoReais;
    private String macAddress;

    @Builder.Default
    private LocalDateTime dataHora = LocalDateTime.now();
}