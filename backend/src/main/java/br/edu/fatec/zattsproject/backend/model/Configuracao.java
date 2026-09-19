package br.edu.fatec.zattsproject.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "configuracoes")
public class Configuracao {

    @Id
    private String id;
    private String usuarioId;
    private Double metaConsumoKwh;
    private Double tarifaKwhReais;
    private Boolean alertaPicoLigado;
    private Double limiteNotificacaoReais;
}