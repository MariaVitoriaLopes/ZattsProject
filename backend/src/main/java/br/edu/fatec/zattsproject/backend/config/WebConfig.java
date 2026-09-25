package br.edu.fatec.zattsproject.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    private final LoginInterceptor loginInterceptor;


    public WebConfig(LoginInterceptor loginInterceptor){
        this.loginInterceptor = loginInterceptor;
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry){

        registry.addInterceptor(loginInterceptor)
                .addPathPatterns(
                        "/home",
                        "/historico",
                        "/alertas",
                        "/notificacoes",
                        "/configuracoes"
                );

    }

}