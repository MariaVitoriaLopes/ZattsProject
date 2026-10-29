package br.edu.fatec.zattsproject.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "configuracoes")
public class Configuracao {

    @Id
    private String id;
    private String usuarioId;
    private Double metaConsumoKwh;
    private Double metaGastoReais;
    private Boolean notificacoesAtivas;
    private String distribuidora;
    private String cidade;
    private String uf;
}