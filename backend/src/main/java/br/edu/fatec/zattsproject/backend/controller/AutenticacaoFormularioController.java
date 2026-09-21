package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AutenticacaoFormularioController {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/cadastro")
    public String cadastrar(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String senha,
            @RequestParam(required = false) String confirmarSenha,
            RedirectAttributes atributos) {

        if (email == null || email.isBlank()
                || senha == null || senha.isBlank()
                || confirmarSenha == null || confirmarSenha.isBlank()) {

            atributos.addFlashAttribute(
                    "erro",
                    "E-mail, senha e confirmação de senha são obrigatórios."
            );

            return "redirect:/cadastro";
        }

        email = email.trim().toLowerCase();

        if (!email.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

            atributos.addFlashAttribute("erro", "E-mail inválido.");
            return "redirect:/cadastro";
        }

        if (!senha.equals(confirmarSenha)) {
            atributos.addFlashAttribute(
                    "erro",
                    "As senhas não coincidem."
            );

            return "redirect:/cadastro";
        }

        if (!senha.matches(
                "^(?=\\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")) {

            atributos.addFlashAttribute(
                    "erro",
                    "A senha deve ter pelo menos 8 caracteres, uma letra maiúscula, uma letra minúscula, um número e um caractere especial."
            );

            return "redirect:/cadastro";
        }

        if (repository.findByEmail(email).isPresent()) {
            atributos.addFlashAttribute(
                    "erro",
                    "E-mail já cadastrado."
            );

            return "redirect:/cadastro";
        }

        Usuario usuario = Usuario.builder()
                .email(email)
                .senha(passwordEncoder.encode(senha))
                .build();

        repository.save(usuario);

        atributos.addFlashAttribute(
                "sucesso",
                "Cadastro realizado com sucesso! Faça login."
        );

        return "redirect:/login";
    }

    @PostMapping("/login")
    public String autenticar(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String senha,
            RedirectAttributes atributos) {

        if (email == null || email.isBlank()
                || senha == null || senha.isBlank()) {

            atributos.addFlashAttribute(
                    "erro",
                    "E-mail e senha são obrigatórios."
            );

            return "redirect:/login";
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
            atributos.addFlashAttribute(
                    "erro",
                    "E-mail ou senha inválidos."
            );

            return "redirect:/login";
        }

        atributos.addFlashAttribute(
                "sucesso",
                "Login realizado com sucesso!"
        );

        return "redirect:/home";
    }
}