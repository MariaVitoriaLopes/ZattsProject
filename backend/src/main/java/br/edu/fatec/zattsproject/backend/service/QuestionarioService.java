
package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.Questionario;
import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.repository.QuestionarioRepository;
import br.edu.fatec.zattsproject.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class QuestionarioService {

    private final QuestionarioRepository questionarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConfiguracaoService configuracaoService;

    private static final Set<String> CONCESSIONARIAS = Set.of(
            "edp", "enel", "cpfl", "elektro",
            "cemig", "light", "copel", "outra"
    );

    public boolean concluido(String usuarioId) {
        return questionarioRepository.existsByUsuarioIdAndStatus(
                usuarioId, "CONCLUIDO"
        );
    }

    public void salvar(
            String usuarioId,
            Integer qtdPessoas,
            String concessionaria,
            LocalDate vencimentoUltimaConta,
            String valorMes1,
            String valorMes2,
            String valorMes3,
            String definirMeta,
            String tipoMeta,
            String valorMeta
    ) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Usuário não encontrado.")
                );

        if (usuario.isQuestionarioRespondido()) {
            throw new IllegalStateException(
                    "O questionário já foi respondido."
            );
        }

        if (qtdPessoas == null || qtdPessoas < 1 || qtdPessoas > 5) {
            throw new IllegalArgumentException(
                    "Quantidade de pessoas inválida."
            );
        }

        if (concessionaria == null ||
                !CONCESSIONARIAS.contains(concessionaria)) {
            throw new IllegalArgumentException(
                    "Concessionária inválida."
            );
        }

        if (vencimentoUltimaConta == null) {
            throw new IllegalArgumentException(
                    "Informe a data de vencimento."
            );
        }

        // Valida a escolha da meta
        if (!"sim".equals(definirMeta) &&
                !"nao".equals(definirMeta)) {
            throw new IllegalArgumentException(
                    "Informe se deseja definir uma meta."
            );
        }

        boolean desejaMeta = "sim".equals(definirMeta);

        // Primeiro mês obrigatório
        BigDecimal mes1 = converterValor(valorMes1);

        // Segundo e terceiro meses opcionais
        BigDecimal mes2 = valorMes2 == null || valorMes2.isBlank()
                ? null
                : converterValor(valorMes2);

        BigDecimal mes3 = valorMes3 == null || valorMes3.isBlank()
                ? null
                : converterValor(valorMes3);

        // Valida o tipo de meta
        if (desejaMeta &&
                !"CONSUMO".equals(tipoMeta) &&
                !"VALOR".equals(tipoMeta)) {
            throw new IllegalArgumentException(
                    "Selecione se sua meta é de consumo ou valor."
            );
        }

        String tipoMetaFinal = desejaMeta ? tipoMeta : null;

        // Converte o valor da meta somente se necessário
        BigDecimal meta = desejaMeta
                ? converterValor(valorMeta)
                : null;

        if (meta != null && meta.signum() <= 0) {
            throw new IllegalArgumentException(
                    "A meta deve ser maior que zero."
            );
        }

        if (concluido(usuarioId)) {
            throw new IllegalStateException(
                    "O questionário já foi concluído."
            );
        }

        LocalDateTime agora = LocalDateTime.now();

        Questionario questionario = Questionario.builder()
                .usuarioId(usuarioId)
                .status("CONCLUIDO")
                .etapaAtual(7)
                .qtdPessoas(qtdPessoas)
                .concessionaria(concessionaria)
                .vencimentoUltimaConta(vencimentoUltimaConta)
                .valorMes1(mes1)
                .valorMes2(mes2)
                .valorMes3(mes3)
                .definirMeta(desejaMeta)
                .tipoMeta(tipoMetaFinal)
                .valorMeta(meta)
                .criadoEm(agora)
                .atualizadoEm(agora)
                .concluidoEm(agora)
                .build();

        // Salva as respostas no MongoDB
        questionarioRepository.save(questionario);

        // Salva a meta nas configurações do usuário
        if (desejaMeta) {
            configuracaoService.salvarMetaQuestionario(
                    usuarioId,
                    tipoMetaFinal,
                    meta
            );
        }

        // Marca o questionário como respondido
        usuario.setQuestionarioRespondido(true);
        usuarioRepository.save(usuario);
    }

    private BigDecimal converterValor(String valor) {

        if (valor == null ||
                !valor.trim().matches("\\d+([.,]\\d{1,2})?")) {
            throw new IllegalArgumentException(
                    "Informe um valor numérico válido."
            );
        }

        BigDecimal convertido = new BigDecimal(
                valor.trim().replace(",", ".")
        );

        if (convertido.signum() < 0) {
            throw new IllegalArgumentException(
                    "O valor não pode ser negativo."
            );
        }

        return convertido;
    }
}
