package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.Alerta;
import br.edu.fatec.zattsproject.backend.model.Configuracao;
import br.edu.fatec.zattsproject.backend.model.Consumo;
import br.edu.fatec.zattsproject.backend.model.Notificacao;
import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.repository.AlertaRepository;
import br.edu.fatec.zattsproject.backend.repository.ConsumoRepository;
import br.edu.fatec.zattsproject.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertaDisparoService {

    private final AlertaRepository alertaRepository;
    private final ConsumoRepository consumoRepository;
    private final ConfiguracaoService configuracaoService;
    private final NotificacaoService notificacaoService;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    private static final Locale LOCALE_BR =
            Locale.forLanguageTag("pt-BR");

    public void avaliar(String usuarioId) {

        avaliarInterno(
                usuarioId,
                0.0,
                0.0
        );
    }

    public void avaliarEmTempoReal(
            String usuarioId,
            double consumoHoraAtualKwh,
            double potenciaMediaWatts) {

        avaliarInterno(
                usuarioId,
                sanitizarNaoNegativo(
                        consumoHoraAtualKwh
                ),
                sanitizarNaoNegativo(
                        potenciaMediaWatts
                )
        );
    }

    private void avaliarInterno(
            String usuarioId,
            double consumoNaoPersistidoKwh,
            double potenciaMediaWatts) {

        if (usuarioId == null
                || usuarioId.isBlank()) {

            return;
        }

        List<Alerta> alertas =
                alertaRepository
                        .findByUsuarioIdAndAtivoTrue(
                                usuarioId
                        );

        if (alertas.isEmpty()) {
            return;
        }

        Configuracao configuracao =
                configuracaoService
                        .buscarPorUsuario(
                                usuarioId
                        );

        if (configuracao != null
                && Boolean.FALSE.equals(
                configuracao.getNotifAlerta()
        )) {

            return;
        }

        LocalDateTime agora =
                LocalDateTime.now();

        LocalDateTime inicioMes =
                agora.toLocalDate()
                        .withDayOfMonth(1)
                        .atStartOfDay();

        LocalDateTime fimConsulta =
                agora.plusNanos(1);

        List<Consumo> consumosMes =
                consumoRepository
                        .findByUsuarioIdAndDataHoraBetween(
                                usuarioId,
                                inicioMes,
                                fimConsulta
                        );

        double consumoMesKwh =
                calcularConsumoTotal(
                        consumosMes
                )
                        + consumoNaoPersistidoKwh;

        double tarifaKwh =
                configuracaoService
                        .obterTarifaKwh(
                                usuarioId
                        );

        double gastoMesReais =
                consumoMesKwh
                        * tarifaKwh;

        for (Alerta alerta : alertas) {

            try {

                avaliarAlerta(
                        alerta,
                        configuracao,
                        consumoMesKwh,
                        gastoMesReais,
                        potenciaMediaWatts,
                        tarifaKwh,
                        agora
                );

            } catch (Exception e) {

                log.error(
                        "Erro ao avaliar alerta id={} usuarioId={}",
                        alerta.getId(),
                        usuarioId,
                        e
                );
            }
        }
    }

    private void avaliarAlerta(
            Alerta alerta,
            Configuracao configuracao,
            double consumoMesKwh,
            double gastoMesReais,
            double potenciaMediaWatts,
            double tarifaKwh,
            LocalDateTime agora) {

        if (alerta == null
                || !Boolean.TRUE.equals(
                alerta.getAtivo()
        )) {

            return;
        }

        String tipo =
                normalizar(
                        alerta.getTipo()
                );

        double valorAtual;

        Double meta;

        double crescimentoPorHora;

        if ("CONSUMO".equals(tipo)) {

            valorAtual =
                    consumoMesKwh;

            meta =
                    configuracao != null
                            ? configuracao
                            .getMetaConsumoKwh()
                            : null;

            crescimentoPorHora =
                    potenciaMediaWatts
                            / 1000.0;

        } else if ("GASTO".equals(tipo)) {

            valorAtual =
                    gastoMesReais;

            meta =
                    configuracao != null
                            ? configuracao
                            .getMetaGastoReais()
                            : null;

            crescimentoPorHora =
                    (
                            potenciaMediaWatts
                                    / 1000.0
                    )
                            * tarifaKwh;

        } else {

            log.warn(
                    "Alerta {} ignorado por possuir tipo desconhecido: {}",
                    alerta.getId(),
                    alerta.getTipo()
            );

            return;
        }

        boolean percentualMetaAtingido =
                verificarPercentualMeta(
                        alerta,
                        valorAtual,
                        meta
                );

        ResultadoLembrete resultadoLembrete =
                avaliarLembrete(
                        alerta,
                        valorAtual,
                        meta,
                        crescimentoPorHora
                );

        if (!resultadoLembrete
                .deveDisparar()) {

            return;
        }

        if (!repeticaoPermiteDisparo(
                alerta,
                agora
        )) {

            return;
        }

        Notificacao.Categoria
                categoriaNotificacao =
                determinarCategoriaNotificacao(
                        alerta,
                        tipo,
                        percentualMetaAtingido
                                || resultadoLembrete
                                .alvoPercentual()
                );

        if (!categoriaPermitida(
                configuracao,
                categoriaNotificacao
        )) {

            return;
        }

        boolean canalApp =
                canalAtivo(
                        alerta.getCanalApp()
                );

        boolean canalEmail =
                canalAtivo(
                        alerta.getCanalEmail()
                );

        boolean canalPush =
                canalAtivo(
                        alerta.getCanalPush()
                );

        if (!canalApp
                && !canalEmail
                && !canalPush) {

            return;
        }

        String titulo =
                construirTitulo(
                        alerta,
                        resultadoLembrete
                );

        String mensagem =
                construirMensagem(
                        alerta,
                        tipo,
                        valorAtual,
                        meta,
                        resultadoLembrete
                );

        boolean algumCanalEnviado =
                false;

        if (canalApp) {

            Notificacao notificacao =
                    notificacaoService
                            .criar(
                                    alerta.getUsuarioId(),
                                    categoriaNotificacao,
                                    titulo,
                                    mensagem,
                                    alerta.getId()
                            );

            if (notificacao != null) {

                algumCanalEnviado =
                        true;
            }
        }

        if (canalEmail
                && enviarEmail(
                alerta.getUsuarioId(),
                titulo,
                mensagem
        )) {

            algumCanalEnviado =
                    true;
        }

        if (canalPush) {

            log.info(
                    "Alerta {} solicitou canal PUSH, "
                            + "mas não existe provedor push "
                            + "configurado no backend.",
                    alerta.getId()
            );
        }

        if (algumCanalEnviado) {

            alerta.setUltimoDisparoEm(
                    agora
            );

            alertaRepository.save(
                    alerta
            );

            log.info(
                    "Alerta disparado: "
                            + "id={} "
                            + "usuarioId={} "
                            + "tipo={} "
                            + "categoria={} "
                            + "lembrete={} "
                            + "antecipado={} "
                            + "minutosEstimados={}",
                    alerta.getId(),
                    alerta.getUsuarioId(),
                    tipo,
                    categoriaNotificacao,
                    normalizarLembrete(
                            alerta.getLembrete()
                    ),
                    resultadoLembrete
                            .antecipado(),
                    resultadoLembrete
                            .minutosEstimados()
            );
        }
    }

    private ResultadoLembrete avaliarLembrete(
            Alerta alerta,
            double valorAtual,
            Double meta,
            double crescimentoPorHora) {

        AlvoAlerta alvo =
                determinarAlvoMaisProximo(
                        alerta,
                        meta
                );

        if (alvo == null) {

            return ResultadoLembrete
                    .naoDisparar();
        }

        if (valorAtual
                >= alvo.valor()) {

            return ResultadoLembrete
                    .dispararNaHora(
                            alvo.percentual()
                    );
        }

        int antecedenciaMinutos =
                obterAntecedenciaMinutos(
                        alerta.getLembrete()
                );

        if (antecedenciaMinutos <= 0) {

            return ResultadoLembrete
                    .naoDisparar();
        }

        if (!Double.isFinite(
                crescimentoPorHora
        )
                || crescimentoPorHora <= 0) {

            return ResultadoLembrete
                    .naoDisparar();
        }

        double restante =
                alvo.valor()
                        - valorAtual;

        double minutosAteLimite =
                (
                        restante
                                / crescimentoPorHora
                )
                        * 60.0;

        if (!Double.isFinite(
                minutosAteLimite
        )
                || minutosAteLimite < 0
                || minutosAteLimite
                > antecedenciaMinutos) {

            return ResultadoLembrete
                    .naoDisparar();
        }

        long minutosParaMensagem =
                Math.max(
                        1L,
                        (long) Math.ceil(
                                minutosAteLimite
                        )
                );

        return ResultadoLembrete
                .dispararAntecipado(
                        alvo.percentual(),
                        minutosParaMensagem
                );
    }

    private AlvoAlerta determinarAlvoMaisProximo(
            Alerta alerta,
            Double meta) {

        AlvoAlerta melhorAlvo =
                null;

        Double valorLimite =
                alerta.getValorLimite();

        if (valorPositivoValido(
                valorLimite
        )) {

            melhorAlvo =
                    new AlvoAlerta(
                            valorLimite,
                            false
                    );
        }

        Double percentualLimite =
                alerta.getPercentualLimite();

        if (valorPositivoValido(
                percentualLimite
        )
                && valorPositivoValido(
                meta
        )) {

            double alvoPercentual =
                    meta
                            * (
                            percentualLimite
                                    / 100.0
                    );

            if (Double.isFinite(
                    alvoPercentual
            )
                    && alvoPercentual > 0
                    && (
                    melhorAlvo == null
                            || alvoPercentual
                            < melhorAlvo.valor()
            )) {

                melhorAlvo =
                        new AlvoAlerta(
                                alvoPercentual,
                                true
                        );
            }
        }

        return melhorAlvo;
    }

    private boolean verificarPercentualMeta(
            Alerta alerta,
            double valorAtual,
            Double meta) {

        Double percentualLimite =
                alerta.getPercentualLimite();

        if (!valorPositivoValido(
                percentualLimite
        )
                || !valorPositivoValido(
                meta
        )) {

            return false;
        }

        double percentualAtual =
                (
                        valorAtual
                                / meta
                )
                        * 100.0;

        return percentualAtual
                >= percentualLimite;
    }

    private int obterAntecedenciaMinutos(
            String lembrete) {

        return switch (
                normalizarLembrete(
                        lembrete
                )
                ) {

            case "5_MIN" ->
                    5;

            case "15_MIN" ->
                    15;

            case "1_HORA" ->
                    60;

            default ->
                    0;
        };
    }

    private String normalizarLembrete(
            String lembrete) {

        String valor =
                normalizar(
                        lembrete
                );

        return valor.isBlank()
                ? "NA_HORA"
                : valor;
    }

    private Notificacao.Categoria
    determinarCategoriaNotificacao(
            Alerta alerta,
            String tipo,
            boolean percentualMetaRelacionado) {

        String categoriaAlerta =
                normalizar(
                        alerta.getCategoria()
                );

        if ("META".equals(
                categoriaAlerta
        )
                || percentualMetaRelacionado) {

            return Notificacao
                    .Categoria
                    .META;
        }

        if ("GASTO".equals(
                tipo
        )) {

            return Notificacao
                    .Categoria
                    .CONTA;
        }

        if ("CONSUMO".equals(
                tipo
        )) {

            return Notificacao
                    .Categoria
                    .CONSUMO;
        }

        return Notificacao
                .Categoria
                .ALERTA;
    }

    private boolean categoriaPermitida(
            Configuracao configuracao,
            Notificacao.Categoria categoria) {

        if (configuracao == null) {

            return true;
        }

        return switch (categoria) {

            case GERAIS ->
                    !Boolean.FALSE.equals(
                            configuracao
                                    .getNotifGerais()
                    );

            case CONTA ->
                    !Boolean.FALSE.equals(
                            configuracao
                                    .getNotifConta()
                    );

            case ALERTA ->
                    !Boolean.FALSE.equals(
                            configuracao
                                    .getNotifAlerta()
                    );

            case CONSUMO ->
                    !Boolean.FALSE.equals(
                            configuracao
                                    .getNotifConsumo()
                    );

            case META ->
                    !Boolean.FALSE.equals(
                            configuracao
                                    .getNotifMeta()
                    );
        };
    }

    private boolean repeticaoPermiteDisparo(
            Alerta alerta,
            LocalDateTime agora) {

        LocalDateTime ultimo =
                alerta.getUltimoDisparoEm();

        if (ultimo == null) {

            return true;
        }

        String repetir =
                normalizar(
                        alerta.getRepetir()
                );

        if (repetir.isBlank()) {

            repetir =
                    "NUNCA";
        }

        return switch (repetir) {

            case "NUNCA" ->
                    false;

            case "1_HORA" ->
                    !agora.isBefore(
                            ultimo.plusHours(1)
                    );

            case "6_HORAS" ->
                    !agora.isBefore(
                            ultimo.plusHours(6)
                    );

            case "DIARIO" ->
                    !agora.isBefore(
                            ultimo.plusDays(1)
                    );

            default ->
                    false;
        };
    }

    private boolean enviarEmail(
            String usuarioId,
            String titulo,
            String mensagem) {

        try {

            Optional<Usuario> usuarioOpt =
                    usuarioRepository
                            .findById(
                                    usuarioId
                            );

            if (usuarioOpt.isEmpty()) {

                log.warn(
                        "Não foi possível enviar e-mail "
                                + "do alerta: usuário {} "
                                + "não encontrado.",
                        usuarioId
                );

                return false;
            }

            String email =
                    usuarioOpt
                            .get()
                            .getEmail();

            if (email == null
                    || email.isBlank()) {

                log.warn(
                        "Usuário {} não possui "
                                + "e-mail válido para "
                                + "receber o alerta.",
                        usuarioId
                );

                return false;
            }

            emailService
                    .enviarAlerta(
                            email,
                            titulo,
                            mensagem
                    );

            return true;

        } catch (Exception e) {

            log.error(
                    "Erro ao enviar alerta por "
                            + "e-mail para usuarioId={}",
                    usuarioId,
                    e
            );

            return false;
        }
    }

    private String construirTitulo(
            Alerta alerta,
            ResultadoLembrete resultadoLembrete) {

        String nome =
                alerta.getNome() != null
                        && !alerta
                        .getNome()
                        .isBlank()
                        ? alerta
                        .getNome()
                        .trim()
                        : "Alerta";

        if (resultadoLembrete
                .antecipado()) {

            return "Lembrete de alerta: "
                    + nome;
        }

        return "Alerta disparado: "
                + nome;
    }

    private String construirMensagem(
            Alerta alerta,
            String tipo,
            double valorAtual,
            Double meta,
            ResultadoLembrete resultadoLembrete) {

        StringBuilder mensagem =
                new StringBuilder();

        if ("GASTO".equals(
                tipo
        )) {

            if (resultadoLembrete
                    .antecipado()) {

                mensagem.append(
                        "Seu gasto acumulado "
                                + "no mês está em R$ "
                );

            } else {

                mensagem.append(
                        "Seu gasto acumulado "
                                + "no mês chegou a R$ "
                );
            }

            mensagem
                    .append(
                            formatar(
                                    valorAtual,
                                    2
                            )
                    )
                    .append(".");

            if (alerta
                    .getValorLimite()
                    != null) {

                mensagem
                        .append(
                                " Limite configurado: R$ "
                        )
                        .append(
                                formatar(
                                        alerta
                                                .getValorLimite(),
                                        2
                                )
                        )
                        .append(".");
            }

        } else {

            if (resultadoLembrete
                    .antecipado()) {

                mensagem.append(
                        "Seu consumo acumulado "
                                + "no mês está em "
                );

            } else {

                mensagem.append(
                        "Seu consumo acumulado "
                                + "no mês chegou a "
                );
            }

            mensagem
                    .append(
                            formatar(
                                    valorAtual,
                                    2
                            )
                    )
                    .append(
                            " kWh."
                    );

            if (alerta
                    .getValorLimite()
                    != null) {

                mensagem
                        .append(
                                " Limite configurado: "
                        )
                        .append(
                                formatar(
                                        alerta
                                                .getValorLimite(),
                                        2
                                )
                        )
                        .append(
                                " kWh."
                        );
            }
        }

        if (valorPositivoValido(
                meta
        )
                && alerta
                .getPercentualLimite()
                != null) {

            double percentualAtual =
                    (
                            valorAtual
                                    / meta
                    )
                            * 100.0;

            mensagem
                    .append(
                            " Percentual atual da meta: "
                    )
                    .append(
                            formatar(
                                    percentualAtual,
                                    1
                            )
                    )
                    .append("%.");
        }

        if (resultadoLembrete
                .antecipado()) {

            mensagem.append(
                    " Mantido o ritmo atual, "
                            + "o limite deverá ser atingido "
                            + "em aproximadamente "
            );

            mensagem.append(
                    resultadoLembrete
                            .minutosEstimados()
            );

            if (resultadoLembrete
                    .minutosEstimados()
                    == 1) {

                mensagem.append(
                        " minuto."
                );

            } else {

                mensagem.append(
                        " minutos."
                );
            }
        }

        return mensagem.toString();
    }

    private double calcularConsumoTotal(
            List<Consumo> consumos) {

        double total =
                0.0;

        for (Consumo consumo : consumos) {

            if (consumo == null) {
                continue;
            }

            Double kwh =
                    consumo.getConsumoKwh();

            if (kwh == null
                    || !Double.isFinite(
                    kwh
            )
                    || kwh <= 0) {

                continue;
            }

            total +=
                    kwh;
        }

        return total;
    }

    private boolean canalAtivo(
            Boolean valor) {

        return !Boolean.FALSE.equals(
                valor
        );
    }

    private boolean valorPositivoValido(
            Double valor) {

        return valor != null
                && Double.isFinite(
                valor
        )
                && valor > 0;
    }

    private double sanitizarNaoNegativo(
            double valor) {

        if (!Double.isFinite(
                valor
        )
                || valor < 0) {

            return 0.0;
        }

        return valor;
    }

    private String normalizar(
            String valor) {

        return valor == null
                ? ""
                : valor
                .trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }

    private String formatar(
            double valor,
            int casas) {

        return String.format(
                LOCALE_BR,
                "%." + casas + "f",
                valor
        );
    }

    private record AlvoAlerta(
            double valor,
            boolean percentual) {
    }

    private record ResultadoLembrete(
            boolean deveDisparar,
            boolean antecipado,
            boolean alvoPercentual,
            long minutosEstimados) {

        static ResultadoLembrete
        naoDisparar() {

            return new ResultadoLembrete(
                    false,
                    false,
                    false,
                    0L
            );
        }

        static ResultadoLembrete
        dispararNaHora(
                boolean alvoPercentual) {

            return new ResultadoLembrete(
                    true,
                    false,
                    alvoPercentual,
                    0L
            );
        }

        static ResultadoLembrete
        dispararAntecipado(
                boolean alvoPercentual,
                long minutosEstimados) {

            return new ResultadoLembrete(
                    true,
                    true,
                    alvoPercentual,
                    minutosEstimados
            );
        }
    }
}