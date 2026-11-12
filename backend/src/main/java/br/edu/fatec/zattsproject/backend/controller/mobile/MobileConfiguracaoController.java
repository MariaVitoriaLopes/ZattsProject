package br.edu.fatec.zattsproject.backend.controller.mobile;

import br.edu.fatec.zattsproject.backend.model.Configuracao;
import br.edu.fatec.zattsproject.backend.service.ConfiguracaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mobile/configuracoes")
@RequiredArgsConstructor
public class MobileConfiguracaoController {

    private final ConfiguracaoService configuracaoService;

    // Atualiza/Cria a meta via App Mobile
    @PutMapping("/meta")
    public ResponseEntity<?> atualizarMetaMobile(
            @RequestHeader("usuarioId") String usuarioId,
            @RequestParam String tipoMeta, // "CONSUMO" ou "VALOR"
            @RequestParam BigDecimal valorMeta) {
        try {
            Configuracao atualizada = configuracaoService.editarMeta(usuarioId, tipoMeta, valorMeta);
            return ResponseEntity.ok(atualizada);
        } catch (IllegalStateException e) {
            // Se ainda não existia, cria do zero
            Configuracao criada = configuracaoService.criarMeta(usuarioId, tipoMeta, valorMeta);
            return ResponseEntity.ok(criada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}