
package br.edu.fatec.zattsproject.backend.controller.mobile;

import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.QuestionarioService;

import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile/questionario")
@RequiredArgsConstructor
public class QuestionarioMobileController {

    private final QuestionarioService questionarioService;

    public record QuestionarioRequest(
            Integer qtdPessoas,
            String concessionaria,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate vencimentoUltimaConta,
            String valorMes1,
            String valorMes2,
            String valorMes3,
            String consumoMes1,
            String consumoMes2,
            String consumoMes3,
            String definirMeta,
            String tipoMeta,
            String valorMeta
    ) {}

    @PostMapping
    public ResponseEntity<?> salvar(
            @RequestBody QuestionarioRequest dados,
            HttpSession session
    ) {

        Usuario usuario = (Usuario)
                session.getAttribute("usuarioLogado");

        // Verificar autenticação
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "erro", "Sessão não encontrada ou expirada."
                    ));
        }

        // Verificar se já respondeu
        if (questionarioService.concluido(usuario.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "erro", "O questionário já foi concluído."
                    ));
        }

        try {

            questionarioService.salvar(
                    usuario.getId(),
                    dados.qtdPessoas(),
                    dados.concessionaria(),
                    dados.vencimentoUltimaConta(),
                    dados.valorMes1(),
                    dados.valorMes2(),
                    dados.valorMes3(),
                    dados.consumoMes1(),
                    dados.consumoMes2(),
                    dados.consumoMes3(),
                    dados.definirMeta(),
                    dados.tipoMeta(),
                    dados.valorMeta()
            );

            // Atualizar usuário na sessão
            usuario.setQuestionarioRespondido(true);

            session.setAttribute("usuarioLogado", usuario);

            return ResponseEntity.ok(
                    Map.of(
                            "mensagem", "Questionário concluído com sucesso!",
                            "questionarioRespondido", true
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    Map.of("erro", e.getMessage())
            );

        } catch (IllegalStateException e) {

            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("erro", e.getMessage())
            );
        }
    }
}
