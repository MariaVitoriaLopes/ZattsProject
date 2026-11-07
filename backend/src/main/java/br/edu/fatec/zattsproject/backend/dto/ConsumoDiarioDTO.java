package br.edu.fatec.zattsproject.backend.dto;

import java.time.LocalDate;

public record ConsumoDiarioDTO(
        LocalDate dia,
        double kwh,
        double reais
) {
}