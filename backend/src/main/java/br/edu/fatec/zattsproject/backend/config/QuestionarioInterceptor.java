
package br.edu.fatec.zattsproject.backend.config;

import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class QuestionarioInterceptor implements HandlerInterceptor {

    private final UsuarioRepository usuarioRepository;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        var session = request.getSession(false);

        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        Usuario usuarioSessao =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuarioSessao == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        Usuario usuario = usuarioRepository
                .findById(usuarioSessao.getId())
                .orElse(null);

        if (usuario == null) {
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        if (!usuario.isQuestionarioRespondido()) {
            response.sendRedirect(request.getContextPath() + "/questionario");
            return false;
        }

        return true;
    }
}
