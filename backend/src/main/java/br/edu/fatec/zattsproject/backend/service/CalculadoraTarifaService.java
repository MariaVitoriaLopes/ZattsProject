package br.edu.fatec.zattsproject.backend.service;

import org.springframework.stereotype.Service;

@Service
public class CalculadoraTarifaService {

    private static final double TARIFA_BASE_DIRETA_KWH = 0.65;
    private static final double TARIFA_EFETIVA_COM_IMPOSTOS_KWH = 0.88;
    private static final double FATOR_IMPOSTOS = 1.3538;

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

    public double calcularCipPraiaGrande(double consumoKwh) {
        if (consumoKwh <= 0) return 0.0;
        if (consumoKwh <= 50) return 6.80;
        if (consumoKwh <= 100) return 12.50;
        if (consumoKwh <= 200) return 19.90;
        return 28.40;
    }

    public double calcularEstimativaOficialContaMensal(double consumoProjetadoMesKwh, BandeiraTarifaria bandeira) {
        return calcularEstimativaOficialContaMensal(consumoProjetadoMesKwh, null, bandeira);
    }

    public double calcularEstimativaOficialContaMensal(
            double consumoProjetadoMesKwh,
            Double tarifaPersonalizadaKwh,
            BandeiraTarifaria bandeira) {

        if (consumoProjetadoMesKwh <= 0) {
            return 0.0;
        }

        double tarifaBaseComImpostos = (tarifaPersonalizadaKwh != null && tarifaPersonalizadaKwh > 0)
                ? tarifaPersonalizadaKwh
                : TARIFA_EFETIVA_COM_IMPOSTOS_KWH;

        double valorBandeiraComImpostos = (bandeira != null)
                ? bandeira.getAdicionalKwh() * FATOR_IMPOSTOS
                : 0.0;

        double tarifaFinalComImpostos = tarifaBaseComImpostos + valorBandeiraComImpostos;
        double cip = calcularCipPraiaGrande(consumoProjetadoMesKwh);

        return (consumoProjetadoMesKwh * tarifaFinalComImpostos) + cip;
    }
}