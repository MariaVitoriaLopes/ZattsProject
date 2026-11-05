package br.edu.fatec.zattsproject.backend.service;

import org.springframework.stereotype.Service;

@Service
public class CalculadoraTarifaService {

    private static final double TARIFA_BASE_DIRETA_KWH = 0.65;
    private static final double TARIFA_EFETIVA_COM_IMPOSTOS_KWH = 0.88;
    private static final double CIP_PRAIA_GRANDE = 12.50;

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

    public double calcularCustoDiretoAFerroEFogo(double consumoKwh) {
        if (consumoKwh <= 0) return 0.0;
        return consumoKwh * TARIFA_BASE_DIRETA_KWH;
    }

    public double calcularEstimativaOficialContaMensal(double consumoProjetadoMesKwh, BandeiraTarifaria bandeira) {
        if (consumoProjetadoMesKwh <= 0) return CIP_PRAIA_GRANDE;

        double valorBandeira = (bandeira != null) ? bandeira.getAdicionalKwh() : 0.0;
        double tarifaFinalComImpostos = TARIFA_EFETIVA_COM_IMPOSTOS_KWH + valorBandeira;

        return (consumoProjetadoMesKwh * tarifaFinalComImpostos) + CIP_PRAIA_GRANDE;
    }
}