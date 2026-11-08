package br.edu.fatec.zattsproject.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "sessoes")
public class Sessao {

    @Id
    private String id;

    private String usuarioId;

    @Indexed(unique = true)
    private String sessionId;

    private String nome;

    private Tipo tipo;

    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime ultimoAcesso = LocalDateTime.now();

    @Builder.Default
    private boolean ativa = true;

    public enum Tipo {
        WEB,
        ANDROID
    }
}