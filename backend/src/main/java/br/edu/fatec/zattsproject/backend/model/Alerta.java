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
    private String icone;
    private String categoria;
    @Builder.Default
    private Boolean canalPush = true;
    @Builder.Default
    private Boolean canalEmail = true;
    @Builder.Default
    private Boolean canalApp = true;
    private String lembrete;
    private String repetir;
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();
    private LocalDateTime atualizadoEm;
}
