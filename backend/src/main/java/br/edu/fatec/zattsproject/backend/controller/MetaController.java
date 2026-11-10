
package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.ConfiguracaoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/metas")
public class MetaController {

    private final ConfiguracaoService configuracaoService;

    // =====================================================
    // CRIAR META
    // =====================================================

    @PostMapping("/criar")
    public String criar(
            @RequestParam("tipo") String tipoMeta,
            @RequestParam("valor") String valorMeta,
            HttpSession session,
            RedirectAttributes atributos) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        try {

            configuracaoService.criarMeta(
                    usuario.getId(),
                    tipoMeta,
                    converterValor(valorMeta)
            );

            atributos.addFlashAttribute(
                    "sucesso",
                    "Meta criada com sucesso!"
            );

        } catch (IllegalArgumentException | IllegalStateException e) {

            atributos.addFlashAttribute(
                    "erro",
                    e.getMessage()
            );
        }

        return "redirect:/home";
    }

    // =====================================================
    // EDITAR META
    // =====================================================

    @PostMapping("/editar")
    public String editar(
            @RequestParam("tipo") String tipoMeta,
            @RequestParam("valor") String valorMeta,
            HttpSession session,
            RedirectAttributes atributos) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        try {

            configuracaoService.editarMeta(
                    usuario.getId(),
                    tipoMeta,
                    converterValor(valorMeta)
            );

            atributos.addFlashAttribute(
                    "sucesso",
                    "Meta atualizada com sucesso!"
            );

        } catch (IllegalArgumentException | IllegalStateException e) {

            atributos.addFlashAttribute(
                    "erro",
                    e.getMessage()
            );
        }

        return "redirect:/home";
    }

    // =====================================================
    // EXCLUIR META
    // =====================================================

    @PostMapping("/excluir")
    public String excluir(
            @RequestParam String tipoMeta,
            HttpSession session,
            RedirectAttributes atributos) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        try {

            configuracaoService.excluirMeta(
                    usuario.getId(),
                    tipoMeta
            );

            atributos.addFlashAttribute(
                    "sucesso",
                    "Meta excluída com sucesso!"
            );

        } catch (IllegalArgumentException | IllegalStateException e) {

            atributos.addFlashAttribute(
                    "erro",
                    e.getMessage()
            );
        }

        return "redirect:/notificacoes?aba=metas";
    }

    // =====================================================
    // CONVERTER E VALIDAR VALOR
    // =====================================================

    private BigDecimal converterValor(String valor) {

        if (valor == null ||
                !valor.trim().matches("\\d+([.,]\\d{1,2})?")) {

            throw new IllegalArgumentException(
                    "Informe um valor válido."
            );
        }

        BigDecimal convertido = new BigDecimal(
                valor.trim().replace(",", ".")
        );

        if (convertido.signum() <= 0) {

            throw new IllegalArgumentException(
                    "A meta deve ser maior que zero."
            );
        }

        return convertido;
    }
}
