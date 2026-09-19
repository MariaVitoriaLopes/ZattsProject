package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.model.Configuracao;
import br.edu.fatec.zattsproject.backend.repository.ConfiguracaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuracoes")
@RequiredArgsConstructor
public class ConfiguracaoController {

    private final ConfiguracaoRepository repository;

    @PostMapping
    public Configuracao salvar(@RequestBody Configuracao configuracao) {
        return repository.save(configuracao);
    }

    @GetMapping("/usuario/{usuarioId}")
    public Configuracao buscarPorUsuario(@PathVariable String usuarioId) {
        return repository.findByUsuarioId(usuarioId).orElse(null);
    }
}