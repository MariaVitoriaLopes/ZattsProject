package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.AutenticacaoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AutenticacaoFormularioController {

    private final AutenticacaoService autenticacaoService;

    // --- MÉTODOS GET (Carregam as páginas no navegador) ---

    @GetMapping("/login")
    public String exibirLogin() {
        return "login"; // Abre templates/login.html
    }

    @GetMapping("/cadastro")
    public String exibirCadastro() {
        return "cadastro"; // Abre templates/cadastro.html
    }

    @GetMapping("/recuperarSenha")
    public String exibirRecuperarSenha() {
        return "recuperarSenha"; // Abre templates/recuperarSenha.html
    }

    // --- MÉTODOS POST (Processam os formulários enviados) ---

    @PostMapping("/cadastro")
    public String cadastrar(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String senha,
            @RequestParam(required = false) String confirmarSenha,
            RedirectAttributes atributos) {

        AutenticacaoService.ResultadoCadastro resultado =
                autenticacaoService.cadastrar(email, senha, confirmarSenha);

        switch (resultado) {
            case CAMPOS_OBRIGATORIOS:
                atributos.addFlashAttribute(
                        "erro",
                        "E-mail, senha e confirmação de senha são obrigatórios."
                );
                return "redirect:/cadastro";

            case EMAIL_INVALIDO:
                atributos.addFlashAttribute(
                        "erro",
                        "E-mail inválido."
                );
                return "redirect:/cadastro";

            case SENHAS_DIFERENTES:
                atributos.addFlashAttribute(
                        "erro",
                        "As senhas não coincidem."
                );
                return "redirect:/cadastro";

            case SENHA_INVALIDA:
                atributos.addFlashAttribute(
                        "erro",
                        "A senha deve ter pelo menos 8 caracteres, uma letra maiúscula, uma letra minúscula, um número e um caractere especial."
                );
                return "redirect:/cadastro";

            case EMAIL_JA_CADASTRADO:
                atributos.addFlashAttribute(
                        "erro",
                        "E-mail já cadastrado."
                );
                return "redirect:/cadastro";

            case SUCESSO:
                atributos.addFlashAttribute(
                        "sucesso",
                        "Cadastro realizado com sucesso! Faça login."
                );
                return "redirect:/login";
        }

        return "redirect:/cadastro";
    }

    @PostMapping("/login")
    public String autenticar(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String senha,
            HttpSession session,
            RedirectAttributes atributos) {

        AutenticacaoService.ResultadoLogin resultado =
                autenticacaoService.autenticar(email, senha);

        switch (resultado) {
            case CAMPOS_OBRIGATORIOS:
                atributos.addFlashAttribute(
                        "erro",
                        "E-mail e senha são obrigatórios."
                );
                return "redirect:/login";

            case CREDENCIAIS_INVALIDAS:
                atributos.addFlashAttribute(
                        "erro",
                        "E-mail ou senha inválidos."
                );
                return "redirect:/login";

            case SUCESSO:
                // Guarda o utilizador na sessão para o homeController permitir o acesso
                Usuario usuario = new Usuario();
                usuario.setEmail(email);
                session.setAttribute("usuarioLogado", usuario);

                atributos.addFlashAttribute(
                        "sucesso",
                        "Login realizado com sucesso!"
                );
                return "redirect:/home";
        }

        return "redirect:/login";
    }
}