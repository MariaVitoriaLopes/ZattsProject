package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.model.Configuracao;
import br.edu.fatec.zattsproject.backend.service.ConfiguracaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuracoes")
@RequiredArgsConstructor
public class ConfiguracaoController {

    private final ConfiguracaoService service;

    @PostMapping
    public Configuracao salvar(@RequestBody Configuracao configuracao) {
        return service.salvar(configuracao);
    }

    @GetMapping("/usuario/{usuarioId}")
    public Configuracao buscarPorUsuario(@PathVariable String usuarioId) {
        return service.buscarPorUsuario(usuarioId);
    }
}