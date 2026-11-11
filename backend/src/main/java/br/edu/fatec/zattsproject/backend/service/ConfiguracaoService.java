
package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.Configuracao;
import br.edu.fatec.zattsproject.backend.repository.ConfiguracaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ConfiguracaoService {

    private final ConfiguracaoRepository repository;

    public Configuracao salvar(Configuracao configuracao) {
        return repository.save(configuracao);
    }

    public Configuracao buscarPorUsuario(String usuarioId) {
        return repository.findByUsuarioId(usuarioId).orElse(null);
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

        return definirMeta(usuarioId, tipoMeta, valorMeta, false, true);
    }

    // Cria uma nova meta, desde que não exista outra
    public Configuracao criarMeta(
            String usuarioId,
            String tipoMeta,
            BigDecimal valorMeta) {

        return definirMeta(usuarioId, tipoMeta, valorMeta, false, false);
    }

    // Atualiza uma meta existente
    public Configuracao editarMeta(
            String usuarioId,
            String tipoMeta,
            BigDecimal valorMeta) {

        return definirMeta(usuarioId, tipoMeta, valorMeta, true, false);
    }

    private Configuracao definirMeta(
            String usuarioId,
            String tipoMeta,
            BigDecimal valorMeta,
            boolean edicao,
            boolean questionario) {

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

        boolean possuiMetaConsumo =
                configuracao.getMetaConsumoKwh() != null
                        && configuracao.getMetaConsumoKwh() > 0;

        boolean possuiMetaValor =
                configuracao.getMetaGastoReais() != null
                        && configuracao.getMetaGastoReais() > 0;

        // Impede cadastrar uma segunda meta
        if (!edicao && !questionario &&
                (possuiMetaConsumo || possuiMetaValor)) {

            throw new IllegalStateException(
                    "Você já possui uma meta cadastrada. Edite a meta existente."
            );
        }

        // Verifica se a meta que será editada realmente existe
        if (edicao) {

            if ("CONSUMO".equals(tipoMeta) && !possuiMetaConsumo) {
                throw new IllegalStateException(
                        "Não existe uma meta de consumo para editar."
                );
            }

            if ("VALOR".equals(tipoMeta) && !possuiMetaValor) {
                throw new IllegalStateException(
                        "Não existe uma meta de valor para editar."
                );
            }
        }

        // Mantém somente uma meta ativa por usuário
        if ("CONSUMO".equals(tipoMeta)) {

            configuracao.setMetaConsumoKwh(valorMeta.doubleValue());
            configuracao.setMetaGastoReais(null);

        } else {

            configuracao.setMetaGastoReais(valorMeta.doubleValue());
            configuracao.setMetaConsumoKwh(null);
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

    public double obterTarifaKwh(String usuarioId) {
        Configuracao configuracao = buscarPorUsuario(usuarioId);

        if (configuracao == null
                || configuracao.getTarifaKwh() == null
                || configuracao.getTarifaKwh() <= 0) {
            return 0.88;
        }

        return configuracao.getTarifaKwh();
    }

    public double obterCipReais(String usuarioId) {
        Configuracao configuracao = buscarPorUsuario(usuarioId);

        if (configuracao == null
                || configuracao.getCipReais() == null
                || configuracao.getCipReais() < 0) {
            return 12.50;
        }

        return configuracao.getCipReais();
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

    public Configuracao salvarNotificacoes(
            String usuarioId,
            boolean notifGerais,
            boolean notifConta,
            boolean notifAlerta,
            boolean notifConsumo,
            boolean notifMeta) {

        Configuracao configuracao = buscarPorUsuario(usuarioId);

        if (configuracao == null) {
            configuracao = Configuracao.builder()
                    .usuarioId(usuarioId)
                    .build();
        }

        configuracao.setNotifGerais(notifGerais);
        configuracao.setNotifConta(notifConta);
        configuracao.setNotifAlerta(notifAlerta);
        configuracao.setNotifConsumo(notifConsumo);
        configuracao.setNotifMeta(notifMeta);

        return repository.save(configuracao);
    }
}
