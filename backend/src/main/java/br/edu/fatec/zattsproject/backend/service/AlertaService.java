
package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.Alerta;
import br.edu.fatec.zattsproject.backend.repository.AlertaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlertaService {

    private final AlertaRepository repository;

    public ResultadoAlerta cadastrar(Alerta alerta) {

        if (alerta == null
                || alerta.getUsuarioId() == null
                || alerta.getUsuarioId().isBlank()
                || alerta.getNome() == null
                || alerta.getNome().isBlank()
                || alerta.getTipo() == null
                || alerta.getTipo().isBlank()) {

            return ResultadoAlerta.CAMPOS_OBRIGATORIOS;
        }

        if (alerta.getPercentualLimite() == null
                && alerta.getValorLimite() == null) {

            return ResultadoAlerta.LIMITE_INVALIDO;
        }

        if ((alerta.getPercentualLimite() != null
                && (!Double.isFinite(alerta.getPercentualLimite())
                || alerta.getPercentualLimite() <= 0))
                || (alerta.getValorLimite() != null
                && (!Double.isFinite(alerta.getValorLimite())
                || alerta.getValorLimite() <= 0))) {

            return ResultadoAlerta.LIMITE_INVALIDO;
        }

        alerta.setId(null);
        alerta.setNome(alerta.getNome().trim());
        alerta.setTipo(alerta.getTipo().trim());
        alerta.setAtivo(true);
        alerta.setCriadoEm(LocalDateTime.now());
        alerta.setAtualizadoEm(LocalDateTime.now());

        repository.save(alerta);

        return ResultadoAlerta.SUCESSO;
    }

    public List<Alerta> listar(String usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    public List<Alerta> listarAtivos(String usuarioId) {
        return repository.findByUsuarioIdAndAtivoTrue(usuarioId);
    }

    public Optional<Alerta> buscar(String id, String usuarioId) {
        return repository.findByIdAndUsuarioId(id, usuarioId);
    }

    public ResultadoAlerta atualizar(
            String id,
            String usuarioId,
            Alerta dados) {

        Optional<Alerta> alertaOpt =
                repository.findByIdAndUsuarioId(id, usuarioId);

        if (alertaOpt.isEmpty()) {
            return ResultadoAlerta.NAO_ENCONTRADO;
        }

        if (dados == null) {
            return ResultadoAlerta.CAMPOS_OBRIGATORIOS;
        }

        Alerta alerta = alertaOpt.get();

        if (dados.getNome() != null) {
            if (dados.getNome().isBlank()) {
                return ResultadoAlerta.CAMPOS_OBRIGATORIOS;
            }
            alerta.setNome(dados.getNome().trim());
        }

        if (dados.getTipo() != null) {
            if (dados.getTipo().isBlank()) {
                return ResultadoAlerta.CAMPOS_OBRIGATORIOS;
            }
            alerta.setTipo(dados.getTipo().trim());
        }

        if (dados.getPercentualLimite() != null) {
            alerta.setPercentualLimite(dados.getPercentualLimite());
        }

        if (dados.getValorLimite() != null) {
            alerta.setValorLimite(dados.getValorLimite());
        }

        if (dados.getAtivo() != null) {
            alerta.setAtivo(dados.getAtivo());
        }

        if ((alerta.getPercentualLimite() == null
                && alerta.getValorLimite() == null)
                || (alerta.getPercentualLimite() != null
                && (!Double.isFinite(alerta.getPercentualLimite())
                || alerta.getPercentualLimite() <= 0))
                || (alerta.getValorLimite() != null
                && (!Double.isFinite(alerta.getValorLimite())
                || alerta.getValorLimite() <= 0))) {

            return ResultadoAlerta.LIMITE_INVALIDO;
        }

        alerta.setAtualizadoEm(LocalDateTime.now());

        repository.save(alerta);

        return ResultadoAlerta.SUCESSO;
    }

    public ResultadoAlerta excluir(String id, String usuarioId) {

        Optional<Alerta> alertaOpt =
                repository.findByIdAndUsuarioId(id, usuarioId);

        if (alertaOpt.isEmpty()) {
            return ResultadoAlerta.NAO_ENCONTRADO;
        }

        repository.delete(alertaOpt.get());

        return ResultadoAlerta.SUCESSO;
    }

    public enum ResultadoAlerta {
        SUCESSO,
        CAMPOS_OBRIGATORIOS,
        LIMITE_INVALIDO,
        NAO_ENCONTRADO
    }
}
