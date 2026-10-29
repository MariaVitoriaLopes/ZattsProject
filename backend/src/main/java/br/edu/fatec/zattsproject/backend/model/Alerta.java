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
@Document(collection = "alertas")
public class Alerta {

    @Id
    private String id;
    private String usuarioId;
    private String nome;
    private String tipo;
    private Double percentualLimite;
    private Double valorLimite;
    private Boolean ativo;

    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    private LocalDateTime atualizadoEm;
}
