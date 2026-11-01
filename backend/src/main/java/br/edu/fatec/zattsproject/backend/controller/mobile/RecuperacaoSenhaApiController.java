package br.edu.fatec.zattsproject.backend.controller.mobile;

import br.edu.fatec.zattsproject.backend.service.RecuperacaoSenhaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RecuperacaoSenhaApiController {

    private final RecuperacaoSenhaService service;

    @PostMapping("/recuperarSenha")
    public ResponseEntity<String> solicitar(
            @RequestParam String email) {

        try {
            service.solicitar(email);
        } catch (RuntimeException e) {
            System.err.println("ERRO AO ENVIAR EMAIL DE RECUPERACAO:");
            e.printStackTrace();
        }

        return ResponseEntity.ok(
                "Se o e-mail estiver cadastrado, você receberá um link de recuperação."
        );
    }
}
