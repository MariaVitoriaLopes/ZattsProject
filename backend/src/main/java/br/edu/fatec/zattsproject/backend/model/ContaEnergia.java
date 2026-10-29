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
@Document(collection = "contas_energia")
public class ContaEnergia {

    @Id
    private String id;
    private String usuarioId;
    private String mesReferencia; // "2026-10" ou "10/2026"
    private Double consumoKwh;
    private Double valorTotalReais;

    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    private LocalDateTime atualizadoEm;
}