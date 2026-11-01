
package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.AlertaService;
import br.edu.fatec.zattsproject.backend.service.ConfiguracaoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class homeController {

    private final ConfiguracaoService configuracaoService;

    private final AlertaService alertaService;

    private boolean usuarioLogado(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        return usuario != null;
    }


    @GetMapping("/home")
    public String home(Model model, HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("paginaAtual", "home");

        model.addAttribute(
                "configuracao",
                configuracaoService.buscarPorUsuario(usuario.getId())
        );

        return "home";
    }


    @GetMapping("/historico")
    public String historico(Model model, HttpSession session) {

        if (!usuarioLogado(session)) {
            return "redirect:/login";
        }

        model.addAttribute("paginaAtual", "historico");

        return "historico";
    }

    @GetMapping("/alertas")
    public String alertas(Model model, HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("paginaAtual", "alertas");

        model.addAttribute(
                "alertas",
                alertaService.listar(usuario.getId())
        );

        return "alertas";
    }

    @GetMapping("/notificacoes")
    public String notificacoes(Model model, HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("paginaAtual", "notificacoes");

        model.addAttribute(
                "configuracao",
                configuracaoService.buscarPorUsuario(usuario.getId())
        );

        // Futuramente, carregar aqui as notificações
        // geradas pelos alertas personalizados.
        // Não utilizar alertaService.listar() para isso,
        // pois ele retorna as regras, não os disparos.

        return "notificacoes";
    }

    @GetMapping("/configuracoes")
    public String configuracoes(Model model, HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("paginaAtual", "configuracoes");
        model.addAttribute("usuario", usuario);

        model.addAttribute(
                "configuracao",
                configuracaoService.buscarPorUsuario(usuario.getId())
        );

        return "configuracoes";
    }

    @GetMapping("/configuracoes/preferencias")
    public String configuracoesPreferencias(Model model, HttpSession session) {

        if (!usuarioLogado(session)) {
            return "redirect:/login";
        }

        model.addAttribute("paginaAtual", "configuracoes");

        return "configuracoes-preferencias";
    }

    @GetMapping("/configuracoes/notificacoes")
    public String configuracoesNotificacoes(Model model, HttpSession session) {

        if (!usuarioLogado(session)) {
            return "redirect:/login";
        }

        model.addAttribute("paginaAtual", "configuracoes");

        return "configuracoes-notificacoes";
    }
}
