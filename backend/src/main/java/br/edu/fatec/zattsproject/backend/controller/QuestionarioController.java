package br.edu.fatec.zattsproject.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/questionario")
public class QuestionarioController {

    @GetMapping
    public String inicio() {
        return "questionario-inicio";
    }

    @GetMapping("/perguntas")
    public String perguntas() {
        return "questionario-questao";
    }
}