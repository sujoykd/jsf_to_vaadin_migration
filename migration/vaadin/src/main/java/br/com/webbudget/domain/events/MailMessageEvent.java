package br.com.webbudget.domain.events;

import br.com.webbudget.infrastructure.mail.MailMessage;

public record MailMessageEvent(MailMessage mailMessage) {}
