package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.Previsao;
import br.edu.fatec.zattsproject.backend.repository.PrevisaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrevisaoService {

    private final PrevisaoRepository repository;

    public Previsao salvar(Previsao previsao) {
        return repository.save(previsao);
    }

    public List<Previsao> listarPorUsuario(String usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }
}