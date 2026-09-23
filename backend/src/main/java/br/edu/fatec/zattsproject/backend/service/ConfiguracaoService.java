package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.Configuracao;
import br.edu.fatec.zattsproject.backend.repository.ConfiguracaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfiguracaoService {

    private final ConfiguracaoRepository repository;

    public Configuracao salvar(Configuracao configuracao) {
        return repository.save(configuracao);
    }

    public Configuracao buscarPorUsuario(String usuarioId) {
        return repository.findByUsuarioId(usuarioId).orElse(null);
    }
}