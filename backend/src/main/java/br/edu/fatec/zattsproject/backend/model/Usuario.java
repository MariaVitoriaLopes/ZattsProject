package br.edu.fatec.zattsproject.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;

    private String nome;

    private String email;

    private String telefone;

    private String fotoPerfil;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

}