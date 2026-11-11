package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.model.Configuracao;
import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.ConfiguracaoService;
import br.edu.fatec.zattsproject.backend.service.UsuarioService;
import br.edu.fatec.zattsproject.backend.service.SessaoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/configuracoes")
public class ConfiguracaoController {

    private final UsuarioService usuarioService;
    private final ConfiguracaoService configuracaoService;
    private final SessaoService sessaoService;

    // ==========================================
    // EDITAR DADOS PESSOAIS
    // ==========================================

    @PostMapping("/editar-dados")
    public String editarDados(
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam(required = false) String telefone,
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        UsuarioService.ResultadoUsuario resultado =
                usuarioService.atualizarDados(
                        usuario.getId(), nome, email, telefone
                );

        if (resultado == UsuarioService.ResultadoUsuario.SUCESSO) {

            usuario = usuarioService.buscarPorId(usuario.getId());
            session.setAttribute("usuarioLogado", usuario);

            redirect.addFlashAttribute(
                    "mensagem",
                    "Dados pessoais atualizados com sucesso!"
            );

        } else {
            redirect.addFlashAttribute(
                    "erro",
                    "Não foi possível atualizar os dados: " + resultado
            );
        }

        return "redirect:/configuracoes";
    }

    // ==========================================
    // EDITAR ENDEREÇO
    // ==========================================

    @PostMapping("/editar-endereco")
    public String editarEndereco(
            @ModelAttribute Configuracao dados,
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        configuracaoService.salvarDadosEndereco(
                usuario.getId(), dados
        );

        redirect.addFlashAttribute(
                "mensagem",
                "Endereço atualizado com sucesso!"
        );

        return "redirect:/configuracoes";
    }

    // ==========================================
    // EDITAR SENHA
    // ==========================================

    @PostMapping("/editar-senha")
    public String editarSenha(
            @RequestParam String senha,
            @RequestParam String confirmarSenha,
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        UsuarioService.ResultadoUsuario resultado =
                usuarioService.atualizarSenha(
                        usuario.getId(), senha, confirmarSenha
                );

        if (resultado == UsuarioService.ResultadoUsuario.SUCESSO) {

            redirect.addFlashAttribute(
                    "mensagem",
                    "Senha alterada com sucesso!"
            );

        } else {
            redirect.addFlashAttribute(
                    "erro",
                    "Não foi possível alterar a senha: " + resultado
            );
        }

        return "redirect:/configuracoes";
    }

    // ==========================================
    // GERENCIAMENTO DE METAS (WEB FORM REDIRECT)
    // ==========================================

    @PostMapping("/meta")
    public String salvarMetaForm(
            @RequestParam String tipoMeta, // "CONSUMO" ou "VALOR"
            @RequestParam BigDecimal valorMeta,
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            configuracaoService.editarMeta(usuario.getId(), tipoMeta, valorMeta);
            redirect.addFlashAttribute("mensagem", "Meta atualizada com sucesso!");
        } catch (IllegalStateException e) {
            try {
                configuracaoService.criarMeta(usuario.getId(), tipoMeta, valorMeta);
                redirect.addFlashAttribute("mensagem", "Meta criada com sucesso!");
            } catch (Exception ex) {
                redirect.addFlashAttribute("erro", ex.getMessage());
            }
        } catch (Exception e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/configuracoes";
    }

    // ==========================================
    // GERENCIAMENTO DE METAS (AJAX / REST)
    // ==========================================

    @PostMapping("/api/meta")
    @ResponseBody
    public ResponseEntity<?> salvarMetaAjax(
            @RequestParam String tipoMeta,
            @RequestParam BigDecimal valorMeta,
            @RequestHeader(value = "usuarioId", required = false) String usuarioIdHeader,
            HttpSession session) {

        String usuarioId = usuarioIdHeader;

        if (usuarioId == null || usuarioId.isBlank()) {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            if (usuario != null) {
                usuarioId = usuario.getId();
            }
        }

        if (usuarioId == null || usuarioId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Sessão expirada ou usuário não identificado."));
        }

        try {
            Configuracao config = configuracaoService.editarMeta(usuarioId, tipoMeta, valorMeta);
            return ResponseEntity.ok(Map.of("mensagem", "Meta atualizada com sucesso!", "configuracao", config));
        } catch (IllegalStateException e) {
            try {
                Configuracao config = configuracaoService.criarMeta(usuarioId, tipoMeta, valorMeta);
                return ResponseEntity.ok(Map.of("mensagem", "Meta criada com sucesso!", "configuracao", config));
            } catch (Exception ex) {
                return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // ==========================================
    // SALVAR FORMULÁRIO COMPLETO
    // ==========================================

    @PostMapping("/editar-completo")
    public String editarCompleto(
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam(required = false) String telefone,
            @RequestParam(required = false) String senha,
            @RequestParam(required = false) String confirmarSenha,
            @ModelAttribute Configuracao dados,
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        // Atualiza os dados pessoais
        UsuarioService.ResultadoUsuario resultado =
                usuarioService.atualizarDados(
                        usuario.getId(), nome, email, telefone
                );

        if (resultado != UsuarioService.ResultadoUsuario.SUCESSO) {

            redirect.addFlashAttribute(
                    "erro",
                    "Não foi possível atualizar os dados: " + resultado
            );

            return "redirect:/configuracoes";
        }

        // Atualiza a senha somente se foi preenchida
        if (senha != null && !senha.isBlank()) {

            resultado = usuarioService.atualizarSenha(
                    usuario.getId(), senha, confirmarSenha
            );

            if (resultado != UsuarioService.ResultadoUsuario.SUCESSO) {

                redirect.addFlashAttribute(
                        "erro",
                        "Não foi possível atualizar a senha: " + resultado
                );

                return "redirect:/configuracoes";
            }
        }

        // Salva o endereço e as demais configurações
        configuracaoService.salvarDadosEndereco(
                usuario.getId(), dados
        );

        // Atualiza os dados da sessão
        usuario = usuarioService.buscarPorId(usuario.getId());

        session.setAttribute("usuarioLogado", usuario);

        redirect.addFlashAttribute(
                "mensagem",
                "Configurações atualizadas com sucesso!"
        );

        return "redirect:/configuracoes";
    }

    @PostMapping("/notificacoes")
    @ResponseBody
    public ResponseEntity<?> salvarNotificacoes(
            @RequestParam boolean notifGerais,
            @RequestParam boolean notifConta,
            @RequestParam boolean notifAlerta,
            @RequestParam boolean notifConsumo,
            @RequestParam boolean notifMeta,
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "erro",
                            "Sua sessão expirou. Faça login novamente."
                    ));
        }

        configuracaoService.salvarNotificacoes(
                usuario.getId(),
                notifGerais,
                notifConta,
                notifAlerta,
                notifConsumo,
                notifMeta
        );

        return ResponseEntity.ok(
                Map.of(
                        "mensagem",
                        "Preferências de notificações salvas com sucesso."
                )
        );
    }

    // ==========================================
    // SAIR DA SESSÃO
    // ==========================================

    @PostMapping("/sair")
    public String sair(
            HttpSession session) {
        sessaoService.encerrarPorSessionId(
                session.getId()
        );
        session.invalidate();
        return "redirect:/login";
    }

    // ==========================================
    // EXCLUIR CONTA
    // ==========================================

    @PostMapping("/excluir")
    public String excluir(
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        UsuarioService.ResultadoUsuario resultado =
                usuarioService.excluir(usuario.getId());

        if (resultado == UsuarioService.ResultadoUsuario.SUCESSO) {

            session.invalidate();

            return "redirect:/login?contaExcluida";
        }

        redirect.addFlashAttribute(
                "erro",
                "Não foi possível excluir sua conta."
        );

        return "redirect:/configuracoes";
    }
}