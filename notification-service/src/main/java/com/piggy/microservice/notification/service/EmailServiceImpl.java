package com.piggy.microservice.notification.service;

import com.piggy.microservice.notification.domain.NotificationType;
import com.piggy.microservice.notification.domain.Recipient;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.MessageFormat;

@Service
public class EmailServiceImpl implements EmailService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final JavaMailSender mailSender;
    private final Environment env;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender, Environment env) {
        this.mailSender = mailSender;
        this.env = env;
    }

    @Override
    public void sendMail(NotificationType type, Recipient recipient, String attachment) throws MessagingException, IOException {
        String subject = env.getProperty(type.getSubject());
        String textTemplate = env.getProperty(type.getText());

        if (subject == null || textTemplate == null) {
            log.error("Email subject or text template is missing for type {}", type);
            throw new IllegalArgumentException("Email subject or text template is missing");
        }

        String text = MessageFormat.format(textTemplate, recipient.getAccountName());

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(recipient.getEmail());
        helper.setReplyTo(recipient.getEmail());
        helper.setSubject(subject);
        helper.setText(text);

        if (StringUtils.hasLength(attachment)) {
            helper.addAttachment(env.getProperty(type.getAttachment()), new ByteArrayResource(attachment.getBytes()));
        }

        mailSender.send(message);

        log.info("{} email notification has been sent to {}", type, recipient.getEmail());
    }
}
