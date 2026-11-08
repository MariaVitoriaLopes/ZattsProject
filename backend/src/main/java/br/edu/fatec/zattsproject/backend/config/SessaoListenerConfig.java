package br.edu.fatec.zattsproject.backend.config;

import br.edu.fatec.zattsproject.backend.service.SessaoService;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SessaoListenerConfig {

    @Bean
    public ServletListenerRegistrationBean<SessaoHttpSessionListener>
    sessaoHttpSessionListener(
            SessaoService sessaoService) {

        SessaoHttpSessionListener listener =
                new SessaoHttpSessionListener(
                        sessaoService
                );

        return new ServletListenerRegistrationBean<>(
                listener
        );
    }
}