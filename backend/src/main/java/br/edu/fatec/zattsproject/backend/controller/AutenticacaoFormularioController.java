package br.edu.fatec.zattsproject.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import br.edu.fatec.zattsproject.backend.service.AutenticacaoService;

@Controller
@RequiredArgsConstructor
public class AutenticacaoFormularioController {

    private final AutenticacaoService autenticacaoService;

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
                atributos.addFlashAttribute(
                        "sucesso",
                        "Login realizado com sucesso!"
                );
                return "redirect:/home";
        }

        return "redirect:/login";
    }
}