package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.dto.DashboardDTO;
import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.AlertaService;
import br.edu.fatec.zattsproject.backend.service.ConfiguracaoService;
import br.edu.fatec.zattsproject.backend.service.DashboardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ConfiguracaoService configuracaoService;
    private final AlertaService alertaService;
    private final DashboardService dashboardService;

    private Usuario obterUsuarioLogado(HttpSession session) {
        return (Usuario) session.getAttribute("usuarioLogado");
    }

    @GetMapping("/home")
    public String home(Model model, HttpSession session) {
        Usuario usuario = obterUsuarioLogado(session);

        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("paginaAtual", "home");
        model.addAttribute("usuario", usuario);
        model.addAttribute("configuracao", configuracaoService.buscarPorUsuario(usuario.getId()));

        DashboardDTO dashboardData = dashboardService.obterDadosDashboard(usuario.getId());
        model.addAttribute("dash", dashboardData);
        model.addAttribute("usuarioId", usuario.getId());

        return "home";
    }

    @GetMapping("/historico")
    public String historico(Model model, HttpSession session) {
        Usuario usuario = obterUsuarioLogado(session);
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("paginaAtual", "historico");
        model.addAttribute("usuarioId", usuario.getId());
        return "historico";
    }

    @GetMapping("/alertas")
    public String alertas(Model model, HttpSession session) {
        Usuario usuario = obterUsuarioLogado(session);
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("paginaAtual", "alertas");
        model.addAttribute("alertas", alertaService.listar(usuario.getId()));
        return "alertas";
    }

    @GetMapping("/notificacoes")
    public String notificacoes(Model model, HttpSession session) {
        Usuario usuario = obterUsuarioLogado(session);
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("paginaAtual", "notificacoes");
        model.addAttribute("configuracao", configuracaoService.buscarPorUsuario(usuario.getId()));
        return "notificacoes";
    }

    @GetMapping("/configuracoes")
    public String configuracoes(Model model, HttpSession session) {
        Usuario usuario = obterUsuarioLogado(session);
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("paginaAtual", "configuracoes");
        model.addAttribute("usuario", usuario);
        model.addAttribute("configuracao", configuracaoService.buscarPorUsuario(usuario.getId()));
        return "configuracoes";
    }

    @GetMapping("/configuracoes/preferencias")
    public String configuracoesPreferencias(Model model, HttpSession session) {
        if (obterUsuarioLogado(session) == null) {
            return "redirect:/login";
        }
        model.addAttribute("paginaAtual", "configuracoes");
        return "configuracoes-preferencias";
    }

    @GetMapping("/configuracoes/notificacoes")
    public String configuracoesNotificacoes(Model model, HttpSession session) {
        if (obterUsuarioLogado(session) == null) {
            return "redirect:/login";
        }
        model.addAttribute("paginaAtual", "configuracoes");
        return "configuracoes-notificacoes";
    }

    @GetMapping("/configuracoes/dispositivos")
    public String configuracoesDispositivos(Model model, HttpSession session) {
        if (obterUsuarioLogado(session) == null) {
            return "redirect:/login";
        }
        model.addAttribute("paginaAtual", "configuracoes");
        return "configuracoes-dispositivos";
    }

    @GetMapping("/configuracoes/privacidade")
    public String configuracoesPrivacidade(Model model, HttpSession session) {
        if (obterUsuarioLogado(session) == null) {
            return "redirect:/login";
        }
        model.addAttribute("paginaAtual", "configuracoes");
        return "configuracoes-privacidade";
    }

    @GetMapping("/configuracoes/ajuda")
    public String configuracoesAjuda(Model model, HttpSession session) {
        if (obterUsuarioLogado(session) == null) {
            return "redirect:/login";
        }
        model.addAttribute("paginaAtual", "configuracoes");
        return "configuracoes-ajuda";
    }
}