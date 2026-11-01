
package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.RecuperacaoSenha;
import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.repository.RecuperacaoSenhaRepository;
import br.edu.fatec.zattsproject.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RecuperacaoSenhaService {

    private final RecuperacaoSenhaRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final SecureRandom random = new SecureRandom();

    public void solicitar(String email) {

        if (email == null || email.isBlank()) {
            return;
        }

        usuarioRepository.findByEmail(
                email.trim().toLowerCase()
        ).ifPresent(usuario -> {

            byte[] bytes = new byte[32];
            random.nextBytes(bytes);

            String token = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(bytes);

            repository.deleteByUsuarioId(usuario.getId());

            RecuperacaoSenha recuperacao = RecuperacaoSenha.builder()
                    .usuarioId(usuario.getId())
                    .tokenHash(gerarHash(token))
                    .expiraEm(Instant.now().plus(15, ChronoUnit.MINUTES))
                    .build();

            RecuperacaoSenha salva = repository.save(recuperacao);

            try {
                emailService.enviarRecuperacao(usuario.getEmail(), token);
            } catch (RuntimeException e) {
                repository.deleteById(salva.getId());
                throw e;
            }
        });
    }

    public boolean tokenValido(String token) {
        return buscarTokenValido(token) != null;
    }

    public ResultadoRedefinicao redefinir(
            String token,
            String senha,
            String confirmarSenha) {

        RecuperacaoSenha recuperacao = buscarTokenValido(token);

        if (recuperacao == null) {
            return ResultadoRedefinicao.TOKEN_INVALIDO;
        }

        if (senha == null || confirmarSenha == null
                || !senha.equals(confirmarSenha)) {
            return ResultadoRedefinicao.SENHAS_DIFERENTES;
        }

        if (!senha.matches(
                "^(?=\\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$"
        )) {
            return ResultadoRedefinicao.SENHA_INVALIDA;
        }

        Usuario usuario = usuarioRepository
                .findById(recuperacao.getUsuarioId())
                .orElse(null);

        if (usuario == null) {
            return ResultadoRedefinicao.TOKEN_INVALIDO;
        }

        usuario.setSenha(passwordEncoder.encode(senha));
        usuarioRepository.save(usuario);

        repository.deleteByUsuarioId(usuario.getId());

        return ResultadoRedefinicao.SUCESSO;
    }

    private RecuperacaoSenha buscarTokenValido(String token) {

        if (token == null || token.isBlank()) {
            return null;
        }

        RecuperacaoSenha recuperacao = repository
                .findByTokenHash(gerarHash(token))
                .orElse(null);

        if (recuperacao == null ||
                !recuperacao.getExpiraEm().isAfter(Instant.now())) {
            return null;
        }

        return recuperacao;
    }

    private String gerarHash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Erro ao gerar hash do token", e
            );
        }
    }

    public enum ResultadoRedefinicao {
        SUCESSO,
        TOKEN_INVALIDO,
        SENHAS_DIFERENTES,
        SENHA_INVALIDA
    }
}
