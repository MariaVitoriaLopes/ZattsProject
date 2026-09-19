package br.edu.fatec.zattsproject.backend.controller;

import br.edu.fatec.zattsproject.backend.model.Consumo;
import br.edu.fatec.zattsproject.backend.repository.ConsumoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consumos")
@RequiredArgsConstructor
public class ConsumoController {

    private final ConsumoRepository repository;

    @PostMapping
    public Consumo salvar(@RequestBody Consumo consumo) {
        return repository.save(consumo);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Consumo> listarPorUsuario(@PathVariable String usuarioId) {
        return repository.findByUsuarioIdOrderByDataHoraDesc(usuarioId);
    }
}