package br.edu.fatec.zattsproject.backend.dto;

public record SessaoDTO(
        String id,
        String nome,
        String tipo,
        String conectadoEm,
        boolean atual
) {
}