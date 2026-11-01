
package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.model.Configuracao;
import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.ConfiguracaoService;
import br.edu.fatec.zattsproject.backend.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/configuracoes")
public class ConfiguracaoController {

    private final UsuarioService usuarioService;
    private final ConfiguracaoService configuracaoService;

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

    // ==========================================
    // SAIR DA SESSÃO
    // ==========================================

    @PostMapping("/sair")
    public String sair(HttpSession session) {

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
