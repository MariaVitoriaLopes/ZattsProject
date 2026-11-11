package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.dto.DashboardDTO;
import br.edu.fatec.zattsproject.backend.model.Notificacao;
import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.AlertaService;
import br.edu.fatec.zattsproject.backend.service.ConfiguracaoService;
import br.edu.fatec.zattsproject.backend.service.ConsumoService;
import br.edu.fatec.zattsproject.backend.service.MqttLeituraService;
import br.edu.fatec.zattsproject.backend.service.NotificacaoService;
import br.edu.fatec.zattsproject.backend.service.SessaoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ConfiguracaoService configuracaoService;
    private final AlertaService alertaService;
    private final ConsumoService consumoService;
    private final MqttLeituraService mqttLeituraService;
    private final NotificacaoService notificacaoService;
    private final SessaoService sessaoService;

    private Usuario obterUsuarioLogado(HttpSession session) {
        return (Usuario) session.getAttribute("usuarioLogado");
    }

    @GetMapping("/home")
    public String home(Model model, HttpSession session) {
        Usuario usuario = obterUsuarioLogado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        String usuarioId = usuario.getId();
        model.addAttribute("paginaAtual", "home");
        model.addAttribute("usuario", usuario);
        model.addAttribute("configuracao", configuracaoService.buscarPorUsuario(usuarioId));

        // 1. Pega dados consolidados salvos no Banco
        DashboardDTO dashboardData = consumoService.obterDadosDashboard(usuarioId);

        System.out.println("===== DASHBOARD HOME =====");
        System.out.println("Usuário: " + usuarioId);
        System.out.println("Meta recebida: " + dashboardData.getMetaGastoKwh());
        System.out.println("Limite mensal: " + dashboardData.getMetaConsumoMensalLimiteKwh());
        System.out.println("==========================");

        // 2. Soma o acumulado em memória da hora atual aos totais
        double emMemoria = mqttLeituraService.obterConsumoAcumuladoHoraAtual(usuarioId);
        double totalDiario = dashboardData.getConsumoTotalDiarioKwh() + emMemoria;
        double totalMensal = dashboardData.getMetaConsumoMensalAtualKwh() + emMemoria;

        dashboardData.setConsumoTotalDiarioKwh(Math.round(totalDiario * 100000.0) / 100000.0);
        dashboardData.setMetaConsumoMensalAtualKwh(Math.round(totalMensal * 100000.0) / 100000.0);

        // 3. Preenche a lista do gráfico em tempo real usando PontoGraficoMinuto
        List<DashboardDTO.PontoGraficoMinuto> pontosMemoria = mqttLeituraService.obterPontosGraficoTempoReal(usuarioId);
        if (pontosMemoria != null && !pontosMemoria.isEmpty()) {
            dashboardData.setConsumoTempoReal(pontosMemoria);
        }

        model.addAttribute("dash", dashboardData);
        model.addAttribute("usuarioId", usuarioId);
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
    public String notificacoes(
            @RequestParam(defaultValue = "0") int pagina,
            Model model,
            HttpSession session) {
        Usuario usuario = obterUsuarioLogado(session);
        if (usuario == null) {
            return "redirect:/login";
        }
        int paginaSegura = Math.max(pagina, 0);
        Page<Notificacao> paginaNotificacoes = notificacaoService.listar(usuario.getId(), PageRequest.of(paginaSegura, 10));
        model.addAttribute("paginaAtual", "notificacoes");
        model.addAttribute("configuracao", configuracaoService.buscarPorUsuario(usuario.getId()));
        model.addAttribute("notificacoes", paginaNotificacoes.getContent());
        model.addAttribute("paginaNotificacoes", paginaNotificacoes);
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
        Usuario usuario = obterUsuarioLogado(session);
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("paginaAtual", "configuracoes");
        model.addAttribute("configuracao", configuracaoService.buscarPorUsuario(usuario.getId()));
        return "configuracoes-notificacoes";
    }

    @GetMapping("/configuracoes/dispositivos")
    public String configuracoesDispositivos(Model model, HttpSession session) {
        Usuario usuario = obterUsuarioLogado(session);
        if (usuario == null) {
            return "redirect:/login";
        }
        var sessoes = sessaoService.listarAtivasParaTela(usuario.getId(), session.getId());
        model.addAttribute("paginaAtual", "configuracoes");
        model.addAttribute("sessoes", sessoes);
        model.addAttribute("quantidadeSessoes", sessoes.size());
        return "configuracoes-dispositivos";
    }

    @GetMapping("/configuracoes/privacidade")
    public String configuracoesPrivacidade(Model model, HttpSession session) {
        Usuario usuario = obterUsuarioLogado(session);
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("paginaAtual", "configuracoes");
        model.addAttribute("configuracao", configuracaoService.buscarPrivacidade(usuario.getId()));
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