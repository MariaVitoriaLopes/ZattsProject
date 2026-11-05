
package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.Alerta;
import br.edu.fatec.zattsproject.backend.repository.AlertaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Set;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlertaService {

    private final AlertaRepository repository;

    private static final Set<String> ICONES_VALIDOS = Set.of(
            "bolt",
            "flag",
            "shield",
            "wifi_off",
            "power_settings_new",
            "alarm_on",
            "crisis_alert"
    );

    private static final Set<String> CATEGORIAS_VALIDAS = Set.of(
            "ALERTA",
            "META",
            "DISPOSITIVO"
    );

    private static final Set<String> TIPOS_VALIDOS = Set.of(
            "CONSUMO",
            "GASTO"
    );

    private static final Set<String> LEMBRETES_VALIDOS = Set.of(
            "NA_HORA",
            "5_MIN",
            "15_MIN",
            "1_HORA"
    );

    private static final Set<String> REPETICOES_VALIDAS = Set.of(
            "NUNCA",
            "1_HORA",
            "6_HORAS",
            "DIARIO"
    );

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

        if (alerta.getNome().trim().length() > 100) {
            return ResultadoAlerta.CAMPOS_OBRIGATORIOS;
        }

        if (alerta.getIcone() == null || alerta.getIcone().isBlank()) {
            alerta.setIcone("bolt");
        }

        if (alerta.getCategoria() == null || alerta.getCategoria().isBlank()) {
            alerta.setCategoria("ALERTA");
        }

        if (alerta.getLembrete() == null || alerta.getLembrete().isBlank()) {
            alerta.setLembrete("NA_HORA");
        }

        if (alerta.getRepetir() == null || alerta.getRepetir().isBlank()) {
            alerta.setRepetir("1_HORA");
        }

        if (alerta.getCanalPush() == null) {
            alerta.setCanalPush(true);
        }

        if (alerta.getCanalEmail() == null) {
            alerta.setCanalEmail(true);
        }

        if (alerta.getCanalApp() == null) {
            alerta.setCanalApp(true);
        }

        if (alerta.getAtivo() == null) {
            alerta.setAtivo(true);
        }

        String tipo = alerta.getTipo().trim();
        String icone = alerta.getIcone().trim();
        String categoria = alerta.getCategoria().trim();
        String lembrete = alerta.getLembrete().trim();
        String repetir = alerta.getRepetir().trim();

        if (!TIPOS_VALIDOS.contains(tipo)
                || !ICONES_VALIDOS.contains(icone)
                || !CATEGORIAS_VALIDAS.contains(categoria)
                || !LEMBRETES_VALIDOS.contains(lembrete)
                || !REPETICOES_VALIDAS.contains(repetir)) {

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
        alerta.setTipo(tipo);
        alerta.setIcone(icone);
        alerta.setCategoria(categoria);
        alerta.setLembrete(lembrete);
        alerta.setRepetir(repetir);
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

        if (dados == null
                || dados.getNome() == null
                || dados.getNome().isBlank()
                || dados.getTipo() == null
                || dados.getTipo().isBlank()) {

            return ResultadoAlerta.CAMPOS_OBRIGATORIOS;
        }

        if (dados.getNome().trim().length() > 100) {
            return ResultadoAlerta.CAMPOS_OBRIGATORIOS;
        }

        Alerta alerta = alertaOpt.get();

        String tipo = dados.getTipo().trim();

        String icone =
                dados.getIcone() == null || dados.getIcone().isBlank()
                        ? "bolt"
                        : dados.getIcone().trim();

        String categoria =
                dados.getCategoria() == null || dados.getCategoria().isBlank()
                        ? "ALERTA"
                        : dados.getCategoria().trim();

        String lembrete =
                dados.getLembrete() == null || dados.getLembrete().isBlank()
                        ? "NA_HORA"
                        : dados.getLembrete().trim();

        String repetir =
                dados.getRepetir() == null || dados.getRepetir().isBlank()
                        ? "1_HORA"
                        : dados.getRepetir().trim();

        if (!TIPOS_VALIDOS.contains(tipo)
                || !ICONES_VALIDOS.contains(icone)
                || !CATEGORIAS_VALIDAS.contains(categoria)
                || !LEMBRETES_VALIDOS.contains(lembrete)
                || !REPETICOES_VALIDAS.contains(repetir)) {

            return ResultadoAlerta.CAMPOS_OBRIGATORIOS;
        }

        Double percentualLimite = dados.getPercentualLimite();
        Double valorLimite = dados.getValorLimite();

        if (percentualLimite == null
                && valorLimite == null) {

            return ResultadoAlerta.LIMITE_INVALIDO;
        }

        if ((percentualLimite != null
                && (!Double.isFinite(percentualLimite)
                || percentualLimite <= 0))
                || (valorLimite != null
                && (!Double.isFinite(valorLimite)
                || valorLimite <= 0))) {

            return ResultadoAlerta.LIMITE_INVALIDO;
        }

        alerta.setNome(dados.getNome().trim());
        alerta.setTipo(tipo);
        alerta.setIcone(icone);
        alerta.setCategoria(categoria);

        alerta.setPercentualLimite(percentualLimite);
        alerta.setValorLimite(valorLimite);

        alerta.setLembrete(lembrete);
        alerta.setRepetir(repetir);

        if (dados.getCanalPush() != null) {
            alerta.setCanalPush(dados.getCanalPush());
        } else if (alerta.getCanalPush() == null) {
            alerta.setCanalPush(true);
        }

        if (dados.getCanalEmail() != null) {
            alerta.setCanalEmail(dados.getCanalEmail());
        } else if (alerta.getCanalEmail() == null) {
            alerta.setCanalEmail(true);
        }

        if (dados.getCanalApp() != null) {
            alerta.setCanalApp(dados.getCanalApp());
        } else if (alerta.getCanalApp() == null) {
            alerta.setCanalApp(true);
        }

        if (dados.getAtivo() != null) {
            alerta.setAtivo(dados.getAtivo());
        } else if (alerta.getAtivo() == null) {
            alerta.setAtivo(true);
        }

        alerta.setAtualizadoEm(LocalDateTime.now());

        repository.save(alerta);

        return ResultadoAlerta.SUCESSO;
    }

    public ResultadoAlerta alterarAtivo(
            String id,
            String usuarioId,
            boolean ativo) {

        Optional<Alerta> alertaOpt =
                repository.findByIdAndUsuarioId(id, usuarioId);

        if (alertaOpt.isEmpty()) {
            return ResultadoAlerta.NAO_ENCONTRADO;
        }

        Alerta alerta = alertaOpt.get();

        alerta.setAtivo(ativo);
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
