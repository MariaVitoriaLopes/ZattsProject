package br.edu.fatec.zattsproject.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingPage {
    @GetMapping("/")
    public String landingpage() {
        return "landingpage";
    }
}
