package br.edu.fatec.zattsproject.backend.controller.mobile;

import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.AjudaService;
import br.edu.fatec.zattsproject.backend.service.AjudaService.ResultadoAjuda;

import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mobile/configuracoes/ajuda")
@RequiredArgsConstructor
public class AjudaMobileController {

    private final AjudaService ajudaService;

    public record ContatoRequest(
            String assunto,
            String mensagem
    ) {}

    @PostMapping("/contato")
    public ResponseEntity<?> enviarContato(
            @RequestBody ContatoRequest dados,
            HttpSession session
    ) {

        Usuario usuario =
                (Usuario) session.getAttribute(
                        "usuarioLogado"
                );

        ResultadoAjuda resultado =
                ajudaService.enviar(
                        usuario,
                        dados.assunto(),
                        dados.mensagem()
                );

        return switch (resultado) {

            case SUCESSO ->
                    ResponseEntity.ok(
                            Map.of(
                                    "mensagem",
                                    "Mensagem enviada com sucesso."
                            )
                    );

            case USUARIO_NAO_AUTENTICADO ->
                    ResponseEntity
                            .status(HttpStatus.UNAUTHORIZED)
                            .body(
                                    Map.of(
                                            "erro",
                                            "Sua sessão expirou. Faça login novamente."
                                    )
                            );

            case ASSUNTO_INVALIDO ->
                    ResponseEntity.badRequest()
                            .body(
                                    Map.of(
                                            "erro",
                                            "Selecione um assunto válido."
                                    )
                            );

            case MENSAGEM_OBRIGATORIA ->
                    ResponseEntity.badRequest()
                            .body(
                                    Map.of(
                                            "erro",
                                            "Informe uma mensagem."
                                    )
                            );

            case MENSAGEM_MUITO_LONGA ->
                    ResponseEntity.badRequest()
                            .body(
                                    Map.of(
                                            "erro",
                                            "A mensagem deve ter no máximo 1000 caracteres."
                                    )
                            );

            case ERRO_ENVIO ->
                    ResponseEntity
                            .status(
                                    HttpStatus.INTERNAL_SERVER_ERROR
                            )
                            .body(
                                    Map.of(
                                            "erro",
                                            "Não foi possível enviar sua mensagem. Tente novamente."
                                    )
                            );
        };
    }
}