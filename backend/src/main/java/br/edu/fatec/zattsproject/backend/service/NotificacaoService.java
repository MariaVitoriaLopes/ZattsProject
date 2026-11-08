package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.Configuracao;
import br.edu.fatec.zattsproject.backend.model.Notificacao;
import br.edu.fatec.zattsproject.backend.repository.NotificacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository repository;
    private final ConfiguracaoService configuracaoService;
    public Page<Notificacao> listar(
            String usuarioId,
            Pageable pageable) {
        return repository.findByUsuarioIdOrderByCriadaEmDesc(
                usuarioId,
                pageable
        );
    }
    public Optional<Notificacao> buscar(
            String id,
            String usuarioId) {

        return repository.findByIdAndUsuarioId(
                id,
                usuarioId
        );
    }
    public Notificacao criar(
            String usuarioId,
            Notificacao.Categoria categoria,
            String titulo,
            String mensagem,
            String alertaId) {

        if (!categoriaPermitida(usuarioId, categoria)) {
            return null;
        }
        Notificacao notificacao = Notificacao.builder()
                .usuarioId(usuarioId)
                .categoria(categoria)
                .titulo(titulo)
                .mensagem(mensagem)
                .criadaEm(LocalDateTime.now())
                .lida(false)
                .alertaId(alertaId)
                .build();
        return repository.save(notificacao);
    }
    public boolean marcarComoLida(
            String id,
            String usuarioId) {
        Optional<Notificacao> notificacaoOpt =
                repository.findByIdAndUsuarioId(
                        id,
                        usuarioId
                );
        if (notificacaoOpt.isEmpty()) {
            return false;
        }
        Notificacao notificacao = notificacaoOpt.get();
        notificacao.setLida(true);
        repository.save(notificacao);
        return true;
    }
    public void marcarTodasComoLidas(String usuarioId) {

        List<Notificacao> notificacoes =
                repository.findByUsuarioIdAndLidaFalse(
                        usuarioId
                );
        notificacoes.forEach(notificacao ->
                notificacao.setLida(true)
        );
        repository.saveAll(notificacoes);
    }
    public long contarNaoLidas(String usuarioId) {

        return repository.countByUsuarioIdAndLidaFalse(
                usuarioId
        );
    }
    private boolean categoriaPermitida(
            String usuarioId,
            Notificacao.Categoria categoria) {

        Configuracao configuracao =
                configuracaoService.buscarPorUsuario(usuarioId);

        if (configuracao == null) {
            return true;
        }
        return switch (categoria) {
            case GERAIS ->
                    !Boolean.FALSE.equals(
                            configuracao.getNotifGerais()
                    );
            case CONTA ->
                    !Boolean.FALSE.equals(
                            configuracao.getNotifConta()
                    );
            case ALERTA ->
                    !Boolean.FALSE.equals(
                            configuracao.getNotifAlerta()
                    );
            case CONSUMO ->
                    !Boolean.FALSE.equals(
                            configuracao.getNotifConsumo()
                    );
            case META ->
                    !Boolean.FALSE.equals(
                            configuracao.getNotifMeta()
                    );
        };
    }
}