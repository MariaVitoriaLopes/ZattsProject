package br.edu.fatec.zattsproject.backend.config;

import br.edu.fatec.zattsproject.backend.service.SessaoService;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SessaoHttpSessionListener
        implements HttpSessionListener {

    private final SessaoService sessaoService;

    @Override
    public void sessionDestroyed(
            HttpSessionEvent event) {

        sessaoService.encerrarPorSessionId(
                event.getSession().getId()
        );
    }
}