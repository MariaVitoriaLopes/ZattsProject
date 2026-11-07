
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
}
