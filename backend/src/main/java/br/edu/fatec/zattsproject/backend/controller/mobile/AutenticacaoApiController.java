package br.edu.fatec.zattsproject.backend.controller.mobile;

import br.edu.fatec.zattsproject.backend.service.AutenticacaoService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mobile/auth")
@RequiredArgsConstructor
public class AutenticacaoApiController {

    private final AutenticacaoService autenticacaoService;

    public record CadastroRequest(
            String email,
            String senha,
            String confirmarSenha
    ) {}

    public record LoginRequest(
            String email,
            String senha
    ) {}

    // CADASTRO
    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(
            @RequestBody CadastroRequest dados
    ) {

        AutenticacaoService.ResultadoCadastro resultado =
                autenticacaoService.cadastrar(
                        dados.email(),
                        dados.senha(),
                        dados.confirmarSenha()
                );

        return switch (resultado) {

            case CAMPOS_OBRIGATORIOS ->
                    ResponseEntity.badRequest().body(
                            Map.of("erro", "Todos os campos são obrigatórios.")
                    );

            case EMAIL_INVALIDO ->
                    ResponseEntity.badRequest().body(
                            Map.of("erro", "E-mail inválido.")
                    );

            case SENHAS_DIFERENTES ->
                    ResponseEntity.badRequest().body(
                            Map.of("erro", "As senhas não coincidem.")
                    );

            case SENHA_INVALIDA ->
                    ResponseEntity.badRequest().body(
                            Map.of("erro", "A senha deve ter pelo menos 8 caracteres, uma letra maiúscula, uma minúscula, um número e um caractere especial.")
                    );

            case EMAIL_JA_CADASTRADO ->
                    ResponseEntity.badRequest().body(
                            Map.of("erro", "E-mail já cadastrado.")
                    );

            case SUCESSO ->
                    ResponseEntity.ok(
                            Map.of("mensagem", "Cadastro realizado com sucesso!")
                    );
        };
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest dados
    ) {

        AutenticacaoService.ResultadoLogin resultado = autenticacaoService.autenticar(
                dados.email(),
                dados.senha()
        );

        return switch (resultado) {

            case CAMPOS_OBRIGATORIOS ->
                    ResponseEntity.badRequest().body(
                            Map.of("erro", "E-mail e senha são obrigatórios.")
                    );

            case CREDENCIAIS_INVALIDAS ->
                    ResponseEntity.status(401).body(
                            Map.of("erro", "E-mail ou senha inválidos.")
                    );

            case SUCESSO ->
                    ResponseEntity.ok(
                            Map.of("mensagem", "Login realizado com sucesso!")
                    );
        };
    }
}