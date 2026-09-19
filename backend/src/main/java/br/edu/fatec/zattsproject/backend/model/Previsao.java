package br.edu.fatec.zattsproject.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "previsoes")
public class Previsao {

    @Id
    private String id;
    private String usuarioId;
    private String mesReferencia; // Tipo assim:"2026-09"
    private Double estimativaKwhMes;
    private Double estimativaValorMes;

    @Builder.Default
    private LocalDateTime calculadoEm = LocalDateTime.now();
}