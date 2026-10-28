package br.edu.fatec.zattsproject.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "consumos_horarios")
public class ConsumoHorario {
    @Id
    private String id;
    private String caixaEnergiaId;
    private LocalDateTime inicioHora;
    private LocalDateTime fimHora;
    private double consumoKwh;
}