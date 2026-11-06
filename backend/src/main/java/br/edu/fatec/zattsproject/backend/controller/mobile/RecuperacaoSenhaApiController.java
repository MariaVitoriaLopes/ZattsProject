package br.edu.fatec.zattsproject.backend.controller.mobile;

import br.edu.fatec.zattsproject.backend.service.RecuperacaoSenhaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/mobile/recuperacao")
@RequiredArgsConstructor
public class RecuperacaoSenhaApiController {

    private final RecuperacaoSenhaService service;

    public record RecuperacaoSenhaRequest(String email) {}

    @PostMapping("/solicitar")
    public ResponseEntity<?> solicitar(@RequestBody RecuperacaoSenhaRequest request) {

        if (request.email() == null || request.email().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "O e-mail deve ser informado."));
        }

        try {
            service.solicitar(request.email().trim());
        } catch (RuntimeException e) {
            log.error("Erro ao processar envio de e-mail de recuperação para {}: ", request.email(), e);
        }

        return ResponseEntity.ok(Map.of(
                "mensagem", "Se o e-mail estiver cadastrado, você receberá um link de recuperação."
        ));
    }
}