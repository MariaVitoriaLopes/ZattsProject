package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutenticacaoService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public ResultadoCadastro cadastrar(
            String email,
            String senha,
            String confirmarSenha) {

        if (email == null || email.isBlank()
                || senha == null || senha.isBlank()
                || confirmarSenha == null || confirmarSenha.isBlank()) {

            return ResultadoCadastro.CAMPOS_OBRIGATORIOS;
        }

        email = email.trim().toLowerCase();

        if (!email.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

            return ResultadoCadastro.EMAIL_INVALIDO;
        }

        if (!senha.equals(confirmarSenha)) {
            return ResultadoCadastro.SENHAS_DIFERENTES;
        }

        if (!senha.matches(
                "^(?=\\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")) {

            return ResultadoCadastro.SENHA_INVALIDA;
        }

        if (repository.findByEmail(email).isPresent()) {
            return ResultadoCadastro.EMAIL_JA_CADASTRADO;
        }

        Usuario usuario = Usuario.builder()
                .email(email)
                .senha(passwordEncoder.encode(senha))
                .build();

        repository.save(usuario);

        return ResultadoCadastro.SUCESSO;
    }

    public ResultadoLogin autenticar(
            String email,
            String senha) {

        if (email == null || email.isBlank()
                || senha == null || senha.isBlank()) {

            return ResultadoLogin.CAMPOS_OBRIGATORIOS;
        }

        email = email.trim().toLowerCase();

        boolean loginValido = repository.findByEmail(email)
                .filter(usuario ->
                        passwordEncoder.matches(
                                senha,
                                usuario.getSenha()
                        ))
                .isPresent();

        if (!loginValido) {
            return ResultadoLogin.CREDENCIAIS_INVALIDAS;
        }

        return ResultadoLogin.SUCESSO;
    }

    public enum ResultadoCadastro {
        SUCESSO,
        CAMPOS_OBRIGATORIOS,
        EMAIL_INVALIDO,
        SENHAS_DIFERENTES,
        SENHA_INVALIDA,
        EMAIL_JA_CADASTRADO
    }

    public enum ResultadoLogin {
        SUCESSO,
        CAMPOS_OBRIGATORIOS,
        CREDENCIAIS_INVALIDAS
    }
}