package br.edu.fatec.zattsproject.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Document(collection = "consumo_realtime")
public class ConsumoRealtime {

    @Id
    private String id;
    private String usuarioId;
    private Double consumoKwh;
    private LocalDateTime dataHora;

    // O MONGODB APAGA O REGISTRO ASSIM QUE CHEGA A DATA/HORA DESTA VARIÁVEL (MEIA-NOITE)
    @Indexed(expireAfterSeconds = 0)
    private Date dataExpiracao;
}