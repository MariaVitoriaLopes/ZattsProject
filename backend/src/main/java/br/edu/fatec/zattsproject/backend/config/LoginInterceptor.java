package br.edu.fatec.zattsproject.backend.config;

import br.edu.fatec.zattsproject.backend.model.Usuario;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;


@Component
public class LoginInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws IOException {


        Usuario usuario =
                (Usuario) request.getSession()
                        .getAttribute("usuarioLogado");


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


        if(usuario == null){

            response.sendRedirect("/login");
            return false;

        }


        return true;
    }
}