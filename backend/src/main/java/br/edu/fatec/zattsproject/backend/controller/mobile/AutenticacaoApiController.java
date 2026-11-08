package br.edu.fatec.zattsproject.backend.controller.mobile;

import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.repository.UsuarioRepository;
import br.edu.fatec.zattsproject.backend.service.AutenticacaoService;
import br.edu.fatec.zattsproject.backend.service.SessaoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mobile/auth")
@RequiredArgsConstructor
public class AutenticacaoApiController {

    private final AutenticacaoService autenticacaoService;
    private final UsuarioRepository usuarioRepository;
    private final SessaoService sessaoService;

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
            @RequestBody LoginRequest dados,
            HttpServletRequest request
    ) {
        AutenticacaoService.ResultadoLogin resultado =
                autenticacaoService.autenticar(
                        dados.email(),
                        dados.senha()
                );

        return switch (resultado) {
            case CAMPOS_OBRIGATORIOS ->
                    ResponseEntity.badRequest().body(
                            Map.of("erro", "E-mail e senha são obrigatórios.")
                    );

            case CREDENCIAIS_INVALIDAS ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                            Map.of("erro", "E-mail ou senha inválidos.")
                    );

            case SUCESSO -> {
                Usuario usuario =
                        autenticacaoService
                                .buscarUsuarioPorEmail(
                                        dados.email()
                                )
                                .orElseThrow();
                HttpSession session =
                        request.getSession(true);
                sessaoService.encerrarPorSessionId(
                        session.getId()
                );
                String novoSessionId =
                        request.changeSessionId();
                usuario.setSenha(null);

                session.setAttribute(
                        "usuarioLogado",
                        usuario
                );
                sessaoService.registrarAndroid(
                        usuario.getId(),
                        novoSessionId,
                        request.getHeader("X-Dispositivo")
                );
                yield ResponseEntity.ok(
                        Map.of(
                                "mensagem",
                                "Login realizado com sucesso!",
                                "usuarioId",
                                usuario.getId(),
                                "email",
                                usuario.getEmail(),
                                "questionarioRespondido",
                                usuario.isQuestionarioRespondido()
                        )
                );
            }
        };
    }

    @GetMapping("/sessao")
    public ResponseEntity<?> verificarSessao(
            HttpServletRequest request
    ) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("erro", "Sessão não encontrada ou expirada.")
            );
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("erro", "Sessão não encontrada ou expirada.")
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "usuarioId", usuario.getId(),
                        "email", usuario.getEmail(),
                        "questionarioRespondido", usuario.isQuestionarioRespondido()
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request) {
        HttpSession session =
                request.getSession(false);
        if (session != null) {
            sessaoService.encerrarPorSessionId(
                    session.getId()
            );
            session.invalidate();
        }
        return ResponseEntity.ok(
                Map.of(
                        "mensagem",
                        "Logout realizado com sucesso!"
                )
        );
    }

    @DeleteMapping("/conta")
    public ResponseEntity<?> excluirConta(
            HttpServletRequest request
    ) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Sessão expirada."));
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado."));
        }

        sessaoService.excluirPorUsuario(
                usuario.getId()
        );

        usuarioRepository.deleteById(usuario.getId());

        session.invalidate();

        return ResponseEntity.ok(
                Map.of("mensagem", "Conta excluída com sucesso!")
        );
    }
}