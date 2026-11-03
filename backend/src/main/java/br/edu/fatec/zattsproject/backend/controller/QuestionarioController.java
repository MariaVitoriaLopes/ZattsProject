package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.QuestionarioService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/questionario")
public class QuestionarioController {

    private final QuestionarioService questionarioService;

    @GetMapping
    public String inicio(HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        if (questionarioService.concluido(usuario.getId())) {
            return "redirect:/home";
        }

        return "questionario-inicio";
    }

    @GetMapping("/perguntas")
    public String perguntas(HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        if (questionarioService.concluido(usuario.getId())) {
            return "redirect:/home";
        }

        return "questionario-questao";
    }

    @PostMapping("/perguntas")
    public String salvar(
            @RequestParam Integer qtdPessoas,
            @RequestParam String concessionaria,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate vencimentoUltimaConta,

            @RequestParam String valorMes1,
            @RequestParam(required = false) String valorMes2,
            @RequestParam(required = false) String valorMes3,

            @RequestParam String consumoMes1,
            @RequestParam(required = false) String consumoMes2,
            @RequestParam(required = false) String consumoMes3,

            @RequestParam String definirMeta,
            @RequestParam(required = false) String tipoMeta,
            @RequestParam(required = false) String valorMeta,

            HttpSession session,
            RedirectAttributes atributos
    ) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        if (questionarioService.concluido(usuario.getId())) {
            return "redirect:/home";
        }

        try {

            questionarioService.salvar(
                    usuario.getId(),
                    qtdPessoas,
                    concessionaria,
                    vencimentoUltimaConta,
                    valorMes1,
                    valorMes2,
                    valorMes3,
                    consumoMes1,
                    consumoMes2,
                    consumoMes3,
                    definirMeta,
                    tipoMeta,
                    valorMeta
            );

            // Atualiza a sessão após concluir o questionário
            usuario.setQuestionarioRespondido(true);
            session.setAttribute("usuarioLogado", usuario);

            atributos.addFlashAttribute(
                    "sucesso",
                    "Questionário concluído com sucesso!"
            );

            return "redirect:/home";

        } catch (IllegalArgumentException | IllegalStateException e) {

            atributos.addFlashAttribute("erro", e.getMessage());

            return "redirect:/questionario/perguntas";
        }
    }
}