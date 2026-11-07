package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AjudaService {

    private final EmailService emailService;

    private static final Map<String, String> ASSUNTOS = Map.of(
            "conta", "Conta e acesso",
            "dispositivo", "Dispositivos",
            "consumo", "Consumo e leituras",
            "meta", "Metas e alertas",
            "sugestao", "Sugestão",
            "outro", "Outro"
    );

    public ResultadoAjuda enviar(
            Usuario usuario,
            String assunto,
            String mensagem) {

        if (usuario == null) {
            return ResultadoAjuda.USUARIO_NAO_AUTENTICADO;
        }

        if (assunto == null || !ASSUNTOS.containsKey(assunto)) {
            return ResultadoAjuda.ASSUNTO_INVALIDO;
        }

        if (mensagem == null || mensagem.isBlank()) {
            return ResultadoAjuda.MENSAGEM_OBRIGATORIA;
        }

        String mensagemTratada = mensagem.trim();

        if (mensagemTratada.length() > 1000) {
            return ResultadoAjuda.MENSAGEM_MUITO_LONGA;
        }

        String nomeUsuario =
                usuario.getNome() == null || usuario.getNome().isBlank()
                        ? "Não informado"
                        : usuario.getNome().trim();

        try {
            emailService.enviarSuporte(
                    nomeUsuario,
                    usuario.getEmail(),
                    ASSUNTOS.get(assunto),
                    mensagemTratada
            );

            return ResultadoAjuda.SUCESSO;

        } catch (IllegalStateException e) {
            return ResultadoAjuda.ERRO_ENVIO;
        }
    }

    public enum ResultadoAjuda {
        SUCESSO,
        USUARIO_NAO_AUTENTICADO,
        ASSUNTO_INVALIDO,
        MENSAGEM_OBRIGATORIA,
        MENSAGEM_MUITO_LONGA,
        ERRO_ENVIO
    }
}