package br.edu.fatec.zattsproject.backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "consumos")
public class Consumo {

    @Id
    private String id;

    private String dispositivoId;  // Identificador da placa ESP32 (ex: "dispositivo-01")
    private String usuarioId;

    private Double correnteAmperes; // Valor da corrente recebida do ESP32
    private Double voltagem;
    private Double potenciaWatts;
    private Double consumoKwh;
    private Double valorEstimadoReais;

    @Builder.Default
    private LocalDateTime dataHora = LocalDateTime.now();
}