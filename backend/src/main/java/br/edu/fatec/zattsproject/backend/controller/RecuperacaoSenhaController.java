
package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.service.RecuperacaoSenhaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class RecuperacaoSenhaController {

    private final RecuperacaoSenhaService service;

    @PostMapping("/recuperarSenha")
    public String solicitar(
            @RequestParam String email,
            RedirectAttributes redirect) {

        try {
            service.solicitar(email);
        } catch (RuntimeException e) {
            System.err.println("ERRO AO ENVIAR EMAIL DE RECUPERACAO:");
            e.printStackTrace();
        }

        redirect.addFlashAttribute(
                "mensagem",
                "Se o e-mail estiver cadastrado, você receberá um link de recuperação."
        );

        return "redirect:/recuperarSenha";
    }

    @GetMapping("/redefinirSenha")
    public String exibirRedefinicao(
            @RequestParam String token,
            Model model) {

        if (!service.tokenValido(token)) {
            model.addAttribute("erro",
                    "Link inválido ou expirado.");
            return "recuperarSenha";
        }

        model.addAttribute("token", token);

        return "redefinirSenha";
    }

    @PostMapping("/redefinirSenha")
    public String redefinir(
            @RequestParam String token,
            @RequestParam String senha,
            @RequestParam String confirmarSenha,
            RedirectAttributes redirect) {

        RecuperacaoSenhaService.ResultadoRedefinicao resultado =
                service.redefinir(token, senha, confirmarSenha);

        if (resultado ==
                RecuperacaoSenhaService.ResultadoRedefinicao.SUCESSO) {

            redirect.addFlashAttribute(
                    "sucesso",
                    "Senha atualizada! Faça login com sua nova senha."
            );

            return "redirect:/login";
        }

        if (resultado ==
                RecuperacaoSenhaService.ResultadoRedefinicao.TOKEN_INVALIDO) {

            redirect.addFlashAttribute(
                    "erro",
                    "Link inválido ou expirado. Solicite outro."
            );

            return "redirect:/recuperarSenha";
        }

        redirect.addFlashAttribute(
                "erro",
                resultado ==
                        RecuperacaoSenhaService.ResultadoRedefinicao.SENHAS_DIFERENTES
                        ? "As senhas não coincidem."
                        : "A senha não atende aos requisitos de segurança."
        );

        return "redirect:/redefinirSenha?token=" + token;
    }
}
