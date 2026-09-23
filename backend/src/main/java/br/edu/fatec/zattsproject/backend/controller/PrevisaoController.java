package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.model.Previsao;
import br.edu.fatec.zattsproject.backend.service.PrevisaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/previsoes")
@RequiredArgsConstructor
public class PrevisaoController {

    private final PrevisaoService service;

    @PostMapping
    public Previsao salvar(@RequestBody Previsao previsao) {
        return service.salvar(previsao);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Previsao> listarPorUsuario(@PathVariable String usuarioId) {
        return service.listarPorUsuario(usuarioId);
    }
}