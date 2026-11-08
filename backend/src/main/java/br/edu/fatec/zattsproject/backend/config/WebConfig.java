
package br.edu.fatec.zattsproject.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final QuestionarioInterceptor questionarioInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // Verifica se o usuário está logado
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns(
                        "/home",
                        "/historico",
                        "/alertas",
                        "/notificacoes",
                        "/configuracoes",
                        "/configuracoes/**",
                        "/questionario",
                        "/questionario/**",
                        "/api/mobile/**"
                )
                .excludePathPatterns(
                        "/api/mobile/auth/login",
                        "/api/mobile/auth/cadastro"
                );

        // Verifica se o usuário já respondeu ao questionário
        registry.addInterceptor(questionarioInterceptor)
                .addPathPatterns(
                        "/home",
                        "/historico",
                        "/alertas",
                        "/notificacoes",
                        "/configuracoes",
                        "/configuracoes/**"
                );
    }
}
