package br.edu.fatec.zattsproject.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PontoGraficoDTO {
    private String hora;
    private double consumoKwh;
}