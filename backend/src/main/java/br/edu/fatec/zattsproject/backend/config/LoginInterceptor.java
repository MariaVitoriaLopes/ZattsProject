package br.edu.fatec.zattsproject.backend.config;

import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.service.SessaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginInterceptor
        implements HandlerInterceptor {

    private final SessaoService sessaoService;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler)
            throws IOException {

        response.setHeader(
                "Cache-Control",
                "no-cache, no-store, must-revalidate"
        );

        response.setHeader(
                "Pragma",
                "no-cache"
        );

        response.setDateHeader(
                "Expires",
                0
        );

        HttpSession session =
                request.getSession(false);

        if (session == null) {
            return negarAcesso(
                    request,
                    response
            );
        }

        Usuario usuario =
                (Usuario) session.getAttribute(
                        "usuarioLogado"
                );

        if (usuario == null) {

            session.invalidate();

            return negarAcesso(
                    request,
                    response
            );
        }

        boolean sessaoValida =
                sessaoService.validarEAtualizar(
                        session.getId(),
                        usuario.getId()
                );

        if (!sessaoValida) {

            session.invalidate();

            return negarAcesso(
                    request,
                    response
            );
        }

        return true;
    }

    private boolean negarAcesso(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        if (ehApiMobile(request)) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.setCharacterEncoding(
                    "UTF-8"
            );

            response.setContentType(
                    "application/json"
            );

            response.getWriter().write(
                    "{\"erro\":\"Sessão não encontrada ou expirada.\"}"
            );

            return false;
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/login"
        );

        return false;
    }

    private boolean ehApiMobile(
            HttpServletRequest request) {

        String uri =
                request.getRequestURI();

        String contexto =
                request.getContextPath();

        return uri.startsWith(
                contexto + "/api/mobile/"
        );
    }
}