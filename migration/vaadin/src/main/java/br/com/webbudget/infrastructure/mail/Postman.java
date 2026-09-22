package br.com.webbudget.infrastructure.mail;

import br.com.webbudget.domain.events.MailMessageEvent;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@ConditionalOnBean(JavaMailSender.class)
public class Postman {

    private final Logger logger = LoggerFactory.getLogger(Postman.class);

    private final JavaMailSender mailSender;

    public Postman(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @EventListener
    public void send(MailMessageEvent event) {
        final MailMessage mailMessage = event.mailMessage();
        try {
            final MimeMessage message = this.mailSender.createMimeMessage();
            message.setFrom(mailMessage.getFrom());
            message.setSubject(mailMessage.getTitle());
            message.setRecipients(Message.RecipientType.TO, mailMessage.getAddressees());
            message.setRecipients(Message.RecipientType.CC, mailMessage.getCcs());
            message.setText(mailMessage.getContent(), "UTF-8", "html");
            message.setSentDate(new Date());
            this.mailSender.send(message);
        } catch (Exception ex) {
            logger.error("Failed to send e-mail", ex);
        }
    }
}
