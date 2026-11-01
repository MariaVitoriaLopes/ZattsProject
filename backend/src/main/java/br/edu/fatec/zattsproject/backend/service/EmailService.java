
package br.edu.fatec.zattsproject.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    @Value("${app.url}")
    private String appUrl;

    public void enviarRecuperacao(String email, String token) {

        String link = appUrl + "/redefinirSenha?token=" + token;

        try {
            MimeMessage mensagem = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mensagem,
                    false,
                    "UTF-8"
            );

            helper.setFrom(
                    new InternetAddress(remetente, "Zatts | Suporte", "UTF-8")
            );

            helper.setTo(email);
            helper.setSubject("Zatts | Recuperação de senha");

            String corpo = """
                    Olá!

                    Recebemos uma solicitação para redefinir a senha da sua conta Zatts.

                    Para criar uma nova senha, acesse o link abaixo:

                    %s

                    Este link é válido por 15 minutos.

                    Se você não solicitou a recuperação de senha,
                    pode ignorar este e-mail com segurança.

                    Atenciosamente,
                    Equipe Zatts
                    """.formatted(link);

            helper.setText(corpo, false);

            mailSender.send(mensagem);

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException(
                    "Erro ao preparar e-mail de recuperação de senha.",
                    e
            );
        }
    }
}
