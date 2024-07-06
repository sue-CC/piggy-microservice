package com.piggy.microservice.notification.service;

import com.piggy.microservice.notification.domain.NotificationType;
import com.piggy.microservice.notification.domain.Recipient;
import jakarta.mail.MessagingException;

import java.io.IOException;

public interface EmailService {

    void sendMail(NotificationType type, Recipient recipient, String attachment) throws MessagingException, IOException;
}
