package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.dto.SessaoDTO;
import br.edu.fatec.zattsproject.backend.model.Sessao;
import br.edu.fatec.zattsproject.backend.repository.SessaoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessaoService {

    private final SessaoRepository repository;
    private final MongoTemplate mongoTemplate;

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy 'às' HH:mm",
                    Locale.forLanguageTag("pt-BR")
            );

    @PostConstruct
    public void garantirIndiceSessionId() {

        mongoTemplate
                .indexOps(Sessao.class)
                .ensureIndex(
                        new Index()
                                .on(
                                        "sessionId",
                                        Sort.Direction.ASC
                                )
                                .unique()
                );
    }

    public Sessao registrarWeb(
            String usuarioId,
            String sessionId,
            String userAgent) {

        String nome = montarNomeWeb(userAgent);

        return registrar(
                usuarioId,
                sessionId,
                nome,
                Sessao.Tipo.WEB
        );
    }

    public Sessao registrarAndroid(
            String usuarioId,
            String sessionId,
            String nomeDispositivo) {

        String nome =
                nomeDispositivo == null
                        || nomeDispositivo.isBlank()
                        ? "Android"
                        : nomeDispositivo.trim();

        return registrar(
                usuarioId,
                sessionId,
                nome,
                Sessao.Tipo.ANDROID
        );
    }

    private Sessao registrar(
            String usuarioId,
            String sessionId,
            String nome,
            Sessao.Tipo tipo) {

        LocalDateTime agora =
                LocalDateTime.now();

        Sessao sessao =
                repository.findBySessionId(sessionId)
                        .orElseGet(Sessao::new);

        sessao.setUsuarioId(usuarioId);
        sessao.setSessionId(sessionId);
        sessao.setNome(nome);
        sessao.setTipo(tipo);

        if (sessao.getCriadoEm() == null) {
            sessao.setCriadoEm(agora);
        }

        sessao.setUltimoAcesso(agora);
        sessao.setAtiva(true);

        return repository.save(sessao);
    }

    public boolean validarEAtualizar(
            String sessionId,
            String usuarioId) {

        Optional<Sessao> sessaoOpt =
                repository.findBySessionId(sessionId);

        if (sessaoOpt.isEmpty()) {
            return false;
        }

        Sessao sessao =
                sessaoOpt.get();

        if (!sessao.isAtiva()) {
            return false;
        }

        if (!Objects.equals(
                sessao.getUsuarioId(),
                usuarioId)) {

            return false;
        }

        sessao.setUltimoAcesso(
                LocalDateTime.now()
        );

        repository.save(sessao);

        return true;
    }

    public Optional<Sessao> buscarPorId(
            String id) {

        return repository.findById(id);
    }

    public void encerrarPorSessionId(
            String sessionId) {

        if (sessionId == null
                || sessionId.isBlank()) {

            return;
        }

        repository.findBySessionId(sessionId)
                .ifPresent(sessao -> {

                    if (sessao.isAtiva()) {

                        sessao.setAtiva(false);
                        sessao.setUltimoAcesso(
                                LocalDateTime.now()
                        );

                        repository.save(sessao);
                    }
                });
    }

    public void desativar(
            Sessao sessao) {

        if (sessao == null) {
            return;
        }

        sessao.setAtiva(false);
        sessao.setUltimoAcesso(
                LocalDateTime.now()
        );

        repository.save(sessao);
    }

    public List<SessaoDTO> listarAtivasParaTela(
            String usuarioId,
            String sessionIdAtual) {

        return repository
                .findByUsuarioIdAndAtivaTrueOrderByCriadoEmDesc(
                        usuarioId
                )
                .stream()
                .map(sessao -> {

                    boolean atual =
                            Objects.equals(
                                    sessao.getSessionId(),
                                    sessionIdAtual
                            );

                    String conectadoEm =
                            sessao.getCriadoEm() == null
                                    ? "Horário de conexão não disponível"
                                    : "Conectado em "
                                    + sessao.getCriadoEm().format(FORMATO_DATA);

                    String tipo =
                            sessao.getTipo() == null
                                    ? Sessao.Tipo.WEB.name()
                                    : sessao.getTipo().name();

                    return new SessaoDTO(
                            sessao.getId(),
                            sessao.getNome(),
                            tipo,
                            conectadoEm,
                            atual
                    );
                })
                .toList();
    }

    public void excluirPorUsuario(
            String usuarioId) {

        repository.deleteByUsuarioId(
                usuarioId
        );
    }

    private String montarNomeWeb(
            String userAgent) {

        if (userAgent == null
                || userAgent.isBlank()) {

            return "Navegador";
        }

        String navegador;

        if (userAgent.contains("Edg/")) {

            navegador = "Edge";

        } else if (
                userAgent.contains("OPR/")
                        || userAgent.contains("Opera")) {

            navegador = "Opera";

        } else if (
                userAgent.contains("Firefox/")) {

            navegador = "Firefox";

        } else if (
                userAgent.contains("Chrome/")) {

            navegador = "Chrome";

        } else if (
                userAgent.contains("Safari/")) {

            navegador = "Safari";

        } else {

            navegador = "Navegador";
        }

        String sistema;

        if (userAgent.contains("Windows")) {

            sistema = "Windows";

        } else if (
                userAgent.contains("Android")) {

            sistema = "Android";

        } else if (
                userAgent.contains("iPhone")
                        || userAgent.contains("iPad")) {

            sistema = "iOS";

        } else if (
                userAgent.contains("Mac OS X")
                        || userAgent.contains("Macintosh")) {

            sistema = "macOS";

        } else if (
                userAgent.contains("Linux")) {

            sistema = "Linux";

        } else {

            sistema = "Sistema desconhecido";
        }

        return navegador
                + " ("
                + sistema
                + ")";
    }
}