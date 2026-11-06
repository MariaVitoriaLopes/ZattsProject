package br.edu.fatec.zattsproject.backend.controller.mobile;

import br.edu.fatec.zattsproject.backend.model.Configuracao;
import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.repository.UsuarioRepository;
import br.edu.fatec.zattsproject.backend.service.ConfiguracaoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile/conta")
@RequiredArgsConstructor
public class ContaMobileController {

    private final UsuarioRepository usuarioRepository;
    private final ConfiguracaoService configuracaoService;
    private final PasswordEncoder passwordEncoder;

    public record ContaRequest(
            String nome,
            String email,
            String telefone,
            String fusoHorario,
            String cep,
            String logradouro,
            String numero,
            String complemento,
            String bairro,
            String cidade,
            String uf,
            String senhaAtual,
            String novaSenha,
            String confirmarSenha
    ) {}

    private Usuario buscarUsuario(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Usuario usuarioSessao = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioSessao == null) {
            return null;
        }

        return usuarioRepository.findById(usuarioSessao.getId()).orElse(null);
    }

    private Map<String, Object> montarResposta(Usuario usuario) {
        Configuracao configuracao = configuracaoService.buscarPorUsuario(usuario.getId());

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("nome", usuario.getNome());
        resposta.put("email", usuario.getEmail());
        resposta.put("telefone", usuario.getTelefone());

        resposta.put("fusoHorario", configuracao != null ? configuracao.getFusoHorario() : null);
        resposta.put("cep", configuracao != null ? configuracao.getCep() : null);
        resposta.put("logradouro", configuracao != null ? configuracao.getLogradouro() : null);
        resposta.put("numero", configuracao != null ? configuracao.getNumero() : null);
        resposta.put("complemento", configuracao != null ? configuracao.getComplemento() : null);
        resposta.put("bairro", configuracao != null ? configuracao.getBairro() : null);
        resposta.put("cidade", configuracao != null ? configuracao.getCidade() : null);
        resposta.put("uf", configuracao != null ? configuracao.getUf() : null);

        return resposta;
    }

    @GetMapping
    public ResponseEntity<?> buscar(HttpServletRequest request) {
        Usuario usuario = buscarUsuario(request);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Sessão expirada."));
        }

        return ResponseEntity.ok(montarResposta(usuario));
    }

    @PutMapping
    public ResponseEntity<?> atualizar(
            @RequestBody ContaRequest dados,
            HttpServletRequest request
    ) {
        Usuario usuario = buscarUsuario(request);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Sessão expirada."));
        }

        if (dados.nome() == null || dados.nome().isBlank()
                || dados.email() == null || dados.email().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Nome e e-mail são obrigatórios."));
        }

        String email = dados.email().trim().toLowerCase();

        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "E-mail inválido."));
        }

        if (!email.equals(usuario.getEmail())
                && usuarioRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "E-mail já cadastrado."));
        }

        String novaSenha = dados.novaSenha();
        String confirmarSenha = dados.confirmarSenha();
        String senhaAtual = dados.senhaAtual();

        boolean alterarSenha = (novaSenha != null && !novaSenha.isEmpty())
                || (confirmarSenha != null && !confirmarSenha.isEmpty())
                || (senhaAtual != null && !senhaAtual.isEmpty());

        if (alterarSenha) {
            if (senhaAtual == null || senhaAtual.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Informe sua senha atual."));
            }

            if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Senha atual incorreta."));
            }

            if (novaSenha == null || !novaSenha.equals(confirmarSenha)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "As novas senhas não coincidem."));
            }

            if (!novaSenha.matches("^(?=\\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "A senha deve ter pelo menos 8 caracteres, " +
                                "uma letra maiúscula, uma minúscula, um número e um caractere especial."));
            }
        }

        usuario.setNome(dados.nome().trim());
        usuario.setEmail(email);
        usuario.setTelefone(dados.telefone());

        if (alterarSenha) {
            usuario.setSenha(passwordEncoder.encode(novaSenha));
        }

        usuarioRepository.save(usuario);

        Configuracao endereco = configuracaoService.buscarPorUsuario(usuario.getId());
        if (endereco == null) {
            endereco = new Configuracao();
        }

        endereco.setFusoHorario(dados.fusoHorario());
        endereco.setCep(dados.cep());
        endereco.setLogradouro(dados.logradouro());
        endereco.setNumero(dados.numero());
        endereco.setComplemento(dados.complemento());
        endereco.setBairro(dados.bairro());
        endereco.setCidade(dados.cidade());
        endereco.setUf(dados.uf());

        configuracaoService.salvarDadosEndereco(usuario.getId(), endereco);

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute("usuarioLogado", usuario);
        }

        return ResponseEntity.ok(montarResposta(usuario));
    }
}