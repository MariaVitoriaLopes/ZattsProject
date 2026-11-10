package br.edu.fatec.zattsproject.backend.controller.mobile;

import br.edu.fatec.zattsproject.backend.model.Alerta;
import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.AlertaService;
import br.edu.fatec.zattsproject.backend.service.AlertaService.ResultadoAlerta;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mobile/alertas")
@RequiredArgsConstructor
public class AlertaMobileController {

    private final AlertaService alertaService;

    @GetMapping
    public ResponseEntity<?> listar(
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute(
                        "usuarioLogado"
                );

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "erro",
                                    "Sua sessão expirou."
                            )
                    );
        }

        return ResponseEntity.ok(
                alertaService.listar(
                        usuario.getId()
                )
        );
    }

    @PostMapping
    public ResponseEntity<?> criar(
            @RequestBody Alerta alerta,
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute(
                        "usuarioLogado"
                );

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "erro",
                                    "Sua sessão expirou."
                            )
                    );
        }

        alerta.setUsuarioId(
                usuario.getId()
        );

        alerta.setId(null);

        ResultadoAlerta resultado =
                alertaService.cadastrar(
                        alerta
                );

        if (resultado != ResultadoAlerta.SUCESSO) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "erro",
                                    resultado.name()
                            )
                    );
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(alerta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(
            @PathVariable String id,
            @RequestBody Alerta dados,
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute(
                        "usuarioLogado"
                );

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "erro",
                                    "Sua sessão expirou."
                            )
                    );
        }

        ResultadoAlerta resultado =
                alertaService.atualizar(
                        id,
                        usuario.getId(),
                        dados
                );

        if (resultado ==
                ResultadoAlerta.NAO_ENCONTRADO) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        if (resultado !=
                ResultadoAlerta.SUCESSO) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "erro",
                                    resultado.name()
                            )
                    );
        }

        return ResponseEntity.ok(
                Map.of(
                        "mensagem",
                        "Alerta atualizado com sucesso."
                )
        );
    }

    @PostMapping("/{id}/ativar")
    public ResponseEntity<?> ativar(
            @PathVariable String id,
            @RequestParam boolean ativo,
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute(
                        "usuarioLogado"
                );

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "erro",
                                    "Sua sessão expirou."
                            )
                    );
        }

        ResultadoAlerta resultado =
                alertaService.ativar(
                        id,
                        usuario.getId(),
                        ativo
                );

        if (resultado ==
                ResultadoAlerta.NAO_ENCONTRADO) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                Map.of(
                        "ativo",
                        ativo
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(
            @PathVariable String id,
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute(
                        "usuarioLogado"
                );

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "erro",
                                    "Sua sessão expirou."
                            )
                    );
        }

        ResultadoAlerta resultado =
                alertaService.excluir(
                        id,
                        usuario.getId()
                );

        if (resultado ==
                ResultadoAlerta.NAO_ENCONTRADO) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                Map.of(
                        "mensagem",
                        "Alerta excluído com sucesso."
                )
        );
    }
}