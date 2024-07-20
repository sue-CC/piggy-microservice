package com.piggy.microservice.notification.service;

import com.piggy.microservice.notification.client.AccountServiceClient;
import com.piggy.microservice.notification.domain.NotificationType;
import com.piggy.microservice.notification.domain.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private AccountServiceClient accountService;

    @Autowired
    private RecipientService recipientService;

    @Autowired
    private EmailService emailService;

    public NotificationServiceImpl(AccountServiceClient accountService) {
        this.accountService = accountService;
    }


    @Override
    @Scheduled(cron = "${backup.cron}")
    public void sendBackupNotifications() {

        final NotificationType type = NotificationType.BACKUP;

        List<Recipient> recipients = recipientService.findReadyToNotify(type);
        log.info("found {} recipients for backup notification", recipients.size());

        recipients.forEach(recipient -> CompletableFuture.runAsync(() -> {
            try {
                String attachment = accountService.getAccount(recipient.getAccountName());
                emailService.sendMail(type, recipient, attachment);
                recipientService.markNotified(type, recipient);
            } catch (Throwable t) {
                log.error("an error during backup notification for {}", recipient, t);
            }
        }));
    }

    @Override
    @Scheduled(cron = "${remind.cron}")
    public void sendRemindNotifications() {

        final NotificationType type = NotificationType.REMIND;

        List<Recipient> recipients = recipientService.findReadyToNotify(type);
        log.info("found {} recipients for remind notification", recipients.size());

        recipients.forEach(recipient -> CompletableFuture.runAsync(() -> {
            try {
                emailService.sendMail(type, recipient, null);
                recipientService.markNotified(type, recipient);
            } catch (Throwable t) {
                log.error("an error during remind notification for {}", recipient, t);
            }
        }));
    }
}
