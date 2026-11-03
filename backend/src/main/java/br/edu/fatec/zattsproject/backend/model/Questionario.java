
package br.edu.fatec.zattsproject.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "questionarios")
public class Questionario {

    @Id
    private String id;

    private String usuarioId;
    private String status;
    private Integer etapaAtual;

    private Integer qtdPessoas;
    private String concessionaria;
    private LocalDate vencimentoUltimaConta;

    // Histórico das contas
    private BigDecimal valorMes1;
    private BigDecimal valorMes2;
    private BigDecimal valorMes3;

    // Consumo das contas em kWh
    private BigDecimal consumoMes1;
    private BigDecimal consumoMes2;
    private BigDecimal consumoMes3;

    // Meta
    private Boolean definirMeta;
    private String tipoMeta; // CONSUMO ou VALOR
    private BigDecimal valorMeta;

    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    private LocalDateTime atualizadoEm;
    private LocalDateTime concluidoEm;
}
