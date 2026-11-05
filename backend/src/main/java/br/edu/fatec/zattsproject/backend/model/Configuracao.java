
package br.edu.fatec.zattsproject.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.HashMap;
import java.util.Map;

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

    @Builder.Default
    private String tema = "automatico";

    @Builder.Default
    private String corDestaque = "padrao";

    @Builder.Default
    private String corPersonalizada = "#E6FB1A";

    @Builder.Default
    private String idioma = "pt-BR";

    @Builder.Default
    private Map<String, Boolean> preferenciasNotificacoes = new HashMap<>();

    private String distribuidora;

    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;

    private String fusoHorario;

    @Builder.Default
    private Boolean compartilharDadosUso = true;

    @Builder.Default
    private Boolean recomendacoesPersonalizadas = true;

    @Builder.Default
    private Boolean comparacaoResidencias = false;
}
