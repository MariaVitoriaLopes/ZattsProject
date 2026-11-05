
package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.Configuracao;
import br.edu.fatec.zattsproject.backend.repository.ConfiguracaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ConfiguracaoService {

    private final ConfiguracaoRepository repository;

    private static final Set<String> TEMAS_VALIDOS = Set.of(
            "automatico",
            "claro",
            "escuro"
    );

    private static final Set<String> CORES_VALIDAS = Set.of(
            "padrao",
            "laranja",
            "vermelho",
            "azul",
            "roxo",
            "outra"
    );

    private static final Set<String> IDIOMAS_VALIDOS = Set.of(
            "pt-BR",
            "en",
            "es"
    );

    public Configuracao salvar(Configuracao configuracao) {
        return repository.save(configuracao);
    }

    public Configuracao buscarPorUsuario(String usuarioId) {
        return repository.findByUsuarioId(usuarioId).orElse(null);
    }

    public Configuracao buscarAparencia(String usuarioId) {

        Configuracao configuracao = buscarPorUsuario(usuarioId);

        if (configuracao == null) {
            configuracao = Configuracao.builder()
                    .usuarioId(usuarioId)
                    .build();

            return repository.save(configuracao);
        }

        boolean alterado = false;

        if (configuracao.getTema() == null
                || !TEMAS_VALIDOS.contains(configuracao.getTema())) {

            configuracao.setTema("automatico");
            alterado = true;
        }

        if (configuracao.getCorDestaque() == null
                || !CORES_VALIDAS.contains(configuracao.getCorDestaque())) {

            configuracao.setCorDestaque("padrao");
            alterado = true;
        }

        if (configuracao.getCorPersonalizada() == null
                || !configuracao.getCorPersonalizada()
                .matches("^#[0-9A-Fa-f]{6}$")) {

            configuracao.setCorPersonalizada("#E6FB1A");
            alterado = true;
        }

        if (configuracao.getIdioma() == null
                || !IDIOMAS_VALIDOS.contains(configuracao.getIdioma())) {

            configuracao.setIdioma("pt-BR");
            alterado = true;
        }

        if (alterado) {
            return repository.save(configuracao);
        }

        return configuracao;
    }

    public Configuracao salvarAparencia(
            String usuarioId,
            String tema,
            String corDestaque,
            String corPersonalizada,
            String idioma) {

        if (tema == null || !TEMAS_VALIDOS.contains(tema)) {
            throw new IllegalArgumentException(
                    "Tema inválido."
            );
        }

        if (corDestaque == null || !CORES_VALIDAS.contains(corDestaque)) {
            throw new IllegalArgumentException(
                    "Cor de destaque inválida."
            );
        }

        if (corPersonalizada == null
                || !corPersonalizada.matches("^#[0-9A-Fa-f]{6}$")) {

            throw new IllegalArgumentException(
                    "Cor personalizada inválida."
            );
        }

        if (idioma == null || !IDIOMAS_VALIDOS.contains(idioma)) {
            throw new IllegalArgumentException(
                    "Idioma inválido."
            );
        }

        Configuracao configuracao = buscarPorUsuario(usuarioId);

        if (configuracao == null) {
            configuracao = Configuracao.builder()
                    .usuarioId(usuarioId)
                    .build();
        }

        configuracao.setTema(tema);
        configuracao.setCorDestaque(corDestaque);
        configuracao.setCorPersonalizada(corPersonalizada);
        configuracao.setIdioma(idioma);

        return repository.save(configuracao);
    }

    public Configuracao buscarPrivacidade(String usuarioId) {

        Configuracao configuracao = buscarPorUsuario(usuarioId);

        if (configuracao == null) {
            configuracao = Configuracao.builder()
                    .usuarioId(usuarioId)
                    .build();

            return repository.save(configuracao);
        }

        boolean alterado = false;

        if (configuracao.getCompartilharDadosUso() == null) {
            configuracao.setCompartilharDadosUso(true);
            alterado = true;
        }

        if (configuracao.getRecomendacoesPersonalizadas() == null) {
            configuracao.setRecomendacoesPersonalizadas(true);
            alterado = true;
        }

        if (configuracao.getComparacaoResidencias() == null) {
            configuracao.setComparacaoResidencias(false);
            alterado = true;
        }

        if (alterado) {
            return repository.save(configuracao);
        }

        return configuracao;
    }

    public Map<String, Boolean> buscarPreferenciasNotificacoes(
            String usuarioId) {

        Configuracao configuracao = buscarPorUsuario(usuarioId);

        if (configuracao == null) {
            configuracao = Configuracao.builder()
                    .usuarioId(usuarioId)
                    .build();

            configuracao = repository.save(configuracao);
        }

        if (configuracao.getPreferenciasNotificacoes() == null) {
            configuracao.setPreferenciasNotificacoes(
                    new HashMap<>()
            );

            configuracao = repository.save(configuracao);
        }

        return configuracao.getPreferenciasNotificacoes();
    }

    public Configuracao salvarPreferenciasNotificacoes(
            String usuarioId,
            Map<String, Boolean> preferencias) {

        if (preferencias == null) {
            throw new IllegalArgumentException(
                    "As preferências de notificações são obrigatórias."
            );
        }

        for (Map.Entry<String, Boolean> item : preferencias.entrySet()) {

            String chave = item.getKey();
            Boolean valor = item.getValue();

            if (chave == null
                    || !chave.matches("^[A-Za-z0-9-]+$")
                    || valor == null) {

                throw new IllegalArgumentException(
                        "Preferência de notificação inválida."
                );
            }
        }

        Configuracao configuracao = buscarPorUsuario(usuarioId);

        if (configuracao == null) {
            configuracao = Configuracao.builder()
                    .usuarioId(usuarioId)
                    .build();
        }

        configuracao.setPreferenciasNotificacoes(
                new HashMap<>(preferencias)
        );

        return repository.save(configuracao);
    }

    public Configuracao salvarPrivacidade(
            String usuarioId,
            boolean compartilharDadosUso,
            boolean recomendacoesPersonalizadas,
            boolean comparacaoResidencias) {

        Configuracao configuracao = buscarPorUsuario(usuarioId);

        if (configuracao == null) {
            configuracao = Configuracao.builder()
                    .usuarioId(usuarioId)
                    .build();
        }

        configuracao.setCompartilharDadosUso(compartilharDadosUso);
        configuracao.setRecomendacoesPersonalizadas(recomendacoesPersonalizadas);
        configuracao.setComparacaoResidencias(comparacaoResidencias);

        return repository.save(configuracao);
    }

    public Configuracao salvarDadosEndereco(
            String usuarioId,
            Configuracao dados) {

        Configuracao configuracao = buscarPorUsuario(usuarioId);

        if (configuracao == null) {
            configuracao = Configuracao.builder()
                    .usuarioId(usuarioId)
                    .build();
        }

        configuracao.setCep(dados.getCep());
        configuracao.setLogradouro(dados.getLogradouro());
        configuracao.setNumero(dados.getNumero());
        configuracao.setComplemento(dados.getComplemento());
        configuracao.setBairro(dados.getBairro());
        configuracao.setCidade(dados.getCidade());
        configuracao.setUf(dados.getUf());
        configuracao.setFusoHorario(dados.getFusoHorario());

        return repository.save(configuracao);
    }

    // Utilizado na conclusão do questionário
    public Configuracao salvarMetaQuestionario(
            String usuarioId,
            String tipoMeta,
            BigDecimal valorMeta) {

        return definirMeta(usuarioId, tipoMeta, valorMeta, false);
    }

    // Cria uma nova meta, desde que não exista outra do mesmo tipo
    public Configuracao criarMeta(
            String usuarioId,
            String tipoMeta,
            BigDecimal valorMeta) {

        return definirMeta(usuarioId, tipoMeta, valorMeta, false);
    }

    // Atualiza uma meta existente
    public Configuracao editarMeta(
            String usuarioId,
            String tipoMeta,
            BigDecimal valorMeta) {

        return definirMeta(usuarioId, tipoMeta, valorMeta, true);
    }

    private Configuracao definirMeta(
            String usuarioId,
            String tipoMeta,
            BigDecimal valorMeta,
            boolean edicao) {

        if (valorMeta == null || valorMeta.signum() <= 0) {
            throw new IllegalArgumentException(
                    "A meta deve ser maior que zero."
            );
        }

        if (!"CONSUMO".equals(tipoMeta) && !"VALOR".equals(tipoMeta)) {
            throw new IllegalArgumentException(
                    "Tipo de meta inválido."
            );
        }

        Configuracao configuracao = buscarPorUsuario(usuarioId);

        if (configuracao == null) {
            if (edicao) {
                throw new IllegalStateException(
                        "A meta não existe para ser editada."
                );
            }

            configuracao = Configuracao.builder()
                    .usuarioId(usuarioId)
                    .build();
        }

        if ("CONSUMO".equals(tipoMeta)) {

            if (!edicao && configuracao.getMetaConsumoKwh() != null) {
                throw new IllegalStateException(
                        "Você já possui uma meta de consumo."
                );
            }

            if (edicao && configuracao.getMetaConsumoKwh() == null) {
                throw new IllegalStateException(
                        "Não existe uma meta de consumo para editar."
                );
            }

            configuracao.setMetaConsumoKwh(valorMeta.doubleValue());

        } else {

            if (!edicao && configuracao.getMetaGastoReais() != null) {
                throw new IllegalStateException(
                        "Você já possui uma meta de valor."
                );
            }

            if (edicao && configuracao.getMetaGastoReais() == null) {
                throw new IllegalStateException(
                        "Não existe uma meta de valor para editar."
                );
            }

            configuracao.setMetaGastoReais(valorMeta.doubleValue());
        }

        return repository.save(configuracao);
    }

    // Exclui somente a meta selecionada
    public Configuracao excluirMeta(
            String usuarioId,
            String tipoMeta) {

        Configuracao configuracao = buscarPorUsuario(usuarioId);

        if (configuracao == null) {
            throw new IllegalStateException(
                    "Nenhuma configuração encontrada."
            );
        }

        if ("CONSUMO".equals(tipoMeta)) {

            if (configuracao.getMetaConsumoKwh() == null) {
                throw new IllegalStateException(
                        "Não existe uma meta de consumo para excluir."
                );
            }

            configuracao.setMetaConsumoKwh(null);

        } else if ("VALOR".equals(tipoMeta)) {

            if (configuracao.getMetaGastoReais() == null) {
                throw new IllegalStateException(
                        "Não existe uma meta de valor para excluir."
                );
            }

            configuracao.setMetaGastoReais(null);

        } else {
            throw new IllegalArgumentException(
                    "Tipo de meta inválido."
            );
        }

        return repository.save(configuracao);
    }
}
