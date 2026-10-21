package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class homeController {


    private boolean usuarioLogado(HttpSession session){

        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogado");

        return usuario != null;
    }



    @GetMapping("/home")
    public String home(
            Model model,
            HttpSession session) {


        if(!usuarioLogado(session)){
            return "redirect:/login";
        }


        model.addAttribute(
                "paginaAtual",
                "home"
        );


        return "home";
    }




    @GetMapping("/historico")
    public String historico(
            Model model,
            HttpSession session) {


        if(!usuarioLogado(session)){
            return "redirect:/login";
        }


        model.addAttribute(
                "paginaAtual",
                "historico"
        );


        return "historico";
    }




    @GetMapping("/alertas")
    public String alertas(
            Model model,
            HttpSession session) {


        if(!usuarioLogado(session)){
            return "redirect:/login";
        }


        model.addAttribute(
                "paginaAtual",
                "alertas"
        );


        return "alertas";
    }




    @GetMapping("/notificacoes")
    public String notificacoes(
            Model model,
            HttpSession session) {


        if(!usuarioLogado(session)){
            return "redirect:/login";
        }


        model.addAttribute(
                "paginaAtual",
                "notificacoes"
        );


        return "notificacoes";
    }




    @GetMapping("/configuracoes")
    public String configuracoes(
            Model model,
            HttpSession session) {


        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogado");


        if(usuario == null){
            return "redirect:/login";
        }



        model.addAttribute(
                "paginaAtual",
                "configuracoes"
        );


        model.addAttribute(
                "usuario",
                usuario
        );


        return "configuracoes";
    }

}