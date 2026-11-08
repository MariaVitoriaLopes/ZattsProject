package br.edu.fatec.zattsproject.backend.model;

import br.edu.fatec.zattsproject.backend.service.CalculadoraTarifaService.BandeiraTarifaria;
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
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;
    private BandeiraTarifaria bandeiraAtual;
    private String fusoHorario;
    @Builder.Default
    private Double tarifaKwh = 0.88;
    @Builder.Default
    private Double cipReais = 12.50;
    @Builder.Default
    private Boolean compartilharDadosUso = true;
    @Builder.Default
    private Boolean recomendacoesPersonalizadas = true;
    @Builder.Default
    private Boolean comparacaoResidencias = false;
    @Builder.Default
    private Boolean notifGerais = true;
    @Builder.Default
    private Boolean notifConta = true;
    @Builder.Default
    private Boolean notifAlerta = true;
    @Builder.Default
    private Boolean notifConsumo = true;
    @Builder.Default
    private Boolean notifMeta = true;
}
