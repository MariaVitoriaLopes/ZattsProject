package br.edu.fatec.zattsproject.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class homeController {


    @GetMapping("/home")
    public String home(Model model) {

        model.addAttribute("paginaAtual", "home");

        return "home";
    }



    @GetMapping("/historico")
    public String historico(Model model) {

        model.addAttribute("paginaAtual", "historico");

        return "historico";
    }



    @GetMapping("/alertas")
    public String alertas(Model model) {

        model.addAttribute("paginaAtual", "alertas");

        return "alertas";
    }



    @GetMapping("/notificacoes")
    public String notificacoes(Model model) {

        model.addAttribute("paginaAtual", "notificacoes");

        return "notificacoes";
    }



    @GetMapping("/configuracoes")
    public String configuracoes(Model model) {

        model.addAttribute("paginaAtual", "configuracoes");

        return "configuracoes";
    }

}