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

        if (usuarioId == null || usuarioId.isBlank()) {
            return;
        }

        List<Alerta> alertas =
                alertaRepository.findByUsuarioIdAndAtivoTrue(
                        usuarioId
                );

        if (alertas.isEmpty()) {
            return;
        }

        Configuracao configuracao =
                configuracaoService.buscarPorUsuario(usuarioId);

        if (configuracao != null
                && Boolean.FALSE.equals(
                configuracao.getNotifAlerta()
        )) {

            return;
        }

        LocalDateTime agora = LocalDateTime.now();

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
                calcularConsumoTotal(consumosMes);

        double tarifaKwh =
                configuracaoService.obterTarifaKwh(
                        usuarioId
                );

        double gastoMesReais =
                consumoMesKwh * tarifaKwh;

        for (Alerta alerta : alertas) {

            try {
                avaliarAlerta(
                        alerta,
                        configuracao,
                        consumoMesKwh,
                        gastoMesReais,
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
            LocalDateTime agora) {

        if (alerta == null
                || !Boolean.TRUE.equals(alerta.getAtivo())) {
            return;
        }

        String tipo =
                normalizar(alerta.getTipo());

        double valorAtual;

        Double meta;

        if ("CONSUMO".equals(tipo)) {

            valorAtual = consumoMesKwh;

            meta = configuracao != null
                    ? configuracao.getMetaConsumoKwh()
                    : null;

        } else if ("GASTO".equals(tipo)) {

            valorAtual = gastoMesReais;

            meta = configuracao != null
                    ? configuracao.getMetaGastoReais()
                    : null;

        } else {

            log.warn(
                    "Alerta {} ignorado por possuir tipo desconhecido: {}",
                    alerta.getId(),
                    alerta.getTipo()
            );

            return;
        }

        boolean limiteAbsolutoAtingido =
                verificarLimiteAbsoluto(
                        alerta,
                        valorAtual
                );

        boolean percentualMetaAtingido =
                verificarPercentualMeta(
                        alerta,
                        valorAtual,
                        meta
                );

        if (!limiteAbsolutoAtingido
                && !percentualMetaAtingido) {

            return;
        }

        if (!repeticaoPermiteDisparo(
                alerta,
                agora)) {

            return;
        }

        boolean canalApp =
                canalAtivo(alerta.getCanalApp());

        boolean canalEmail =
                canalAtivo(alerta.getCanalEmail());

        boolean canalPush =
                canalAtivo(alerta.getCanalPush());

        if (!canalApp
                && !canalEmail
                && !canalPush) {

            return;
        }

        String titulo =
                construirTitulo(alerta);

        String mensagem =
                construirMensagem(
                        alerta,
                        tipo,
                        valorAtual,
                        meta
                );

        boolean algumCanalEnviado = false;

        if (canalApp) {

            Notificacao notificacao =
                    notificacaoService.criar(
                            alerta.getUsuarioId(),
                            Notificacao.Categoria.ALERTA,
                            titulo,
                            mensagem,
                            alerta.getId()
                    );

            if (notificacao != null) {
                algumCanalEnviado = true;
            }
        }

        if (canalEmail) {

            if (enviarEmail(
                    alerta.getUsuarioId(),
                    titulo,
                    mensagem)) {

                algumCanalEnviado = true;
            }
        }

        if (canalPush) {

            log.info(
                    "Alerta {} solicitou canal PUSH, "
                            + "mas não existe provedor push configurado no backend.",
                    alerta.getId()
            );
        }

        if (algumCanalEnviado) {

            alerta.setUltimoDisparoEm(agora);

            alertaRepository.save(alerta);

            log.info(
                    "Alerta disparado: id={} usuarioId={} tipo={}",
                    alerta.getId(),
                    alerta.getUsuarioId(),
                    tipo
            );
        }
    }

    private boolean verificarLimiteAbsoluto(
            Alerta alerta,
            double valorAtual) {

        Double limite =
                alerta.getValorLimite();

        if (limite == null
                || !Double.isFinite(limite)
                || limite <= 0) {

            return false;
        }

        return valorAtual >= limite;
    }

    private boolean verificarPercentualMeta(
            Alerta alerta,
            double valorAtual,
            Double meta) {

        Double percentualLimite =
                alerta.getPercentualLimite();

        if (percentualLimite == null
                || !Double.isFinite(percentualLimite)
                || percentualLimite <= 0) {

            return false;
        }

        if (meta == null
                || !Double.isFinite(meta)
                || meta <= 0) {

            return false;
        }

        double percentualAtual =
                (valorAtual / meta) * 100.0;

        return percentualAtual >= percentualLimite;
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
                normalizar(alerta.getRepetir());

        if (repetir.isBlank()) {
            repetir = "NUNCA";
        }

        return switch (repetir) {

            case "NUNCA" -> false;

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

            default -> false;
        };
    }

    private boolean enviarEmail(
            String usuarioId,
            String titulo,
            String mensagem) {

        try {

            Optional<Usuario> usuarioOpt =
                    usuarioRepository.findById(
                            usuarioId
                    );

            if (usuarioOpt.isEmpty()) {

                log.warn(
                        "Não foi possível enviar e-mail do alerta: "
                                + "usuário {} não encontrado.",
                        usuarioId
                );

                return false;
            }

            String email =
                    usuarioOpt.get().getEmail();

            if (email == null
                    || email.isBlank()) {

                log.warn(
                        "Usuário {} não possui e-mail válido "
                                + "para receber o alerta.",
                        usuarioId
                );

                return false;
            }

            emailService.enviarAlerta(
                    email,
                    titulo,
                    mensagem
            );

            return true;

        } catch (Exception e) {

            log.error(
                    "Erro ao enviar alerta por e-mail para usuarioId={}",
                    usuarioId,
                    e
            );

            return false;
        }
    }

    private String construirTitulo(
            Alerta alerta) {

        String nome =
                alerta.getNome() != null
                        && !alerta.getNome().isBlank()
                        ? alerta.getNome().trim()
                        : "Alerta";

        return "Alerta disparado: " + nome;
    }

    private String construirMensagem(
            Alerta alerta,
            String tipo,
            double valorAtual,
            Double meta) {

        StringBuilder mensagem =
                new StringBuilder();

        if ("GASTO".equals(tipo)) {

            mensagem.append(
                    "Seu gasto acumulado no mês chegou a R$ "
            );

            mensagem.append(
                    formatar(valorAtual, 2)
            );

            mensagem.append(".");

            if (alerta.getValorLimite() != null) {

                mensagem.append(
                        " Limite configurado: R$ "
                );

                mensagem.append(
                        formatar(
                                alerta.getValorLimite(),
                                2
                        )
                );

                mensagem.append(".");
            }

        } else {

            mensagem.append(
                    "Seu consumo acumulado no mês chegou a "
            );

            mensagem.append(
                    formatar(valorAtual, 2)
            );

            mensagem.append(" kWh.");

            if (alerta.getValorLimite() != null) {

                mensagem.append(
                        " Limite configurado: "
                );

                mensagem.append(
                        formatar(
                                alerta.getValorLimite(),
                                2
                        )
                );

                mensagem.append(" kWh.");
            }
        }

        if (meta != null
                && meta > 0
                && alerta.getPercentualLimite() != null) {

            double percentualAtual =
                    (valorAtual / meta) * 100.0;

            mensagem.append(
                    " Percentual atual da meta: "
            );

            mensagem.append(
                    formatar(
                            percentualAtual,
                            1
                    )
            );

            mensagem.append("%.");
        }

        return mensagem.toString();
    }

    private double calcularConsumoTotal(
            List<Consumo> consumos) {

        double total = 0.0;

        for (Consumo consumo : consumos) {

            if (consumo == null) {
                continue;
            }

            Double kwh =
                    consumo.getConsumoKwh();

            if (kwh == null
                    || !Double.isFinite(kwh)
                    || kwh <= 0) {

                continue;
            }

            total += kwh;
        }

        return total;
    }

    private boolean canalAtivo(Boolean valor) {

        return !Boolean.FALSE.equals(valor);
    }

    private String normalizar(String valor) {

        return valor == null
                ? ""
                : valor.trim().toUpperCase();
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
}