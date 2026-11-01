
package br.edu.fatec.zattsproject.backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "recuperacoes_senha")
public class RecuperacaoSenha {

    @Id
    private String id;

    private String usuarioId;
    private String tokenHash;
    private Instant expiraEm;
}
