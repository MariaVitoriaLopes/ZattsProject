package br.edu.fatec.zattsproject.backend.model;

public enum BandeiraTarifaria {
    VERDE(0.00),
    AMARELA(0.01885),
    VERMELHA_PATAMAR_1(0.04463),
    VERMELHA_PATAMAR_2(0.07877);

    private final double adicionalKwh;

    BandeiraTarifaria(double adicionalKwh) {
        this.adicionalKwh = adicionalKwh;
    }

    public double getAdicionalKwh() {
        return adicionalKwh;
    }
}