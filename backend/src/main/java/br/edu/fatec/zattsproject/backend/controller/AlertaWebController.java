
package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.model.Alerta;
import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.AlertaService;
import br.edu.fatec.zattsproject.backend.service.AlertaService.ResultadoAlerta;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notificacoes/alertas")
public class AlertaWebController {

    private final AlertaService alertaService;

    @PostMapping("/criar")
    public String criar(
            @ModelAttribute Alerta alerta,
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        alerta.setUsuarioId(usuario.getId());

        ResultadoAlerta resultado = alertaService.cadastrar(alerta);

        switch (resultado) {
            case SUCESSO:
                redirect.addFlashAttribute("mensagem", "Alerta criado com sucesso!");
                break;

            case CAMPOS_OBRIGATORIOS:
                redirect.addFlashAttribute("erro", "Preencha todos os campos obrigatórios.");
                break;

            case LIMITE_INVALIDO:
                redirect.addFlashAttribute("erro", "O limite informado é inválido.");
                break;

            default:
                redirect.addFlashAttribute("erro", "Erro ao cadastrar alerta: " + resultado);
                break;
        }

        return "redirect:/alertas";
    }

    @PostMapping("/{id}/editar")
    public String editar(
            @PathVariable String id,
            @ModelAttribute Alerta dados,
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        ResultadoAlerta resultado = alertaService.atualizar(
                id,
                usuario.getId(),
                dados
        );

        if (resultado == ResultadoAlerta.SUCESSO) {
            redirect.addFlashAttribute("mensagem", "Alerta atualizado com sucesso!");
        } else {
            redirect.addFlashAttribute("erro", "Não foi possível atualizar o alerta.");
        }

        return "redirect:/alertas";
    }

    @PostMapping("/{id}/ativo")
    @ResponseBody
    public ResponseEntity<?> alterarAtivo(
            @PathVariable String id,
            @RequestParam boolean ativo,
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return ResponseEntity.status(401)
                    .body("Usuário não autenticado.");
        }

        ResultadoAlerta resultado =
                alertaService.alterarAtivo(
                        id,
                        usuario.getId(),
                        ativo
                );

        if (resultado == ResultadoAlerta.NAO_ENCONTRADO) {
            return ResponseEntity.status(404)
                    .body("Alerta não encontrado.");
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/excluir")
    public String excluir(
            @PathVariable String id,
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        ResultadoAlerta resultado = alertaService.excluir(
                id,
                usuario.getId()
        );

        if (resultado == ResultadoAlerta.SUCESSO) {
            redirect.addFlashAttribute("mensagem", "Alerta excluído com sucesso!");
        } else {
            redirect.addFlashAttribute("erro", "Alerta não encontrado.");
        }

        return "redirect:/alertas";
    }
}
