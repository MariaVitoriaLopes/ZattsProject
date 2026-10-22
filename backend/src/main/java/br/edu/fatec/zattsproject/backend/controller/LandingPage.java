package br.edu.fatec.zattsproject.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingPage {

    @GetMapping("/")
    public String index() {
        return "landingpage"; // Procura o ficheiro templates/landingpage.html
    }

    @GetMapping("/landingpage")
    public String landingPage() {
        return "landingpage";
    }
}