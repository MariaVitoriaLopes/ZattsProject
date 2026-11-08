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
@Document(collection = "notificacoes")
public class Notificacao {

    @Id
    private String id;
    private String usuarioId;
    private Categoria categoria;
    private String titulo;
    private String mensagem;
    private LocalDateTime criadaEm;
    @Builder.Default
    private Boolean lida = false;
    private String alertaId;
    public enum Categoria {
        GERAIS,
        CONTA,
        ALERTA,
        CONSUMO,
        META
    }
}