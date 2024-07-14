package com.piggy.microservice.notification.grpc.client;

import com.google.protobuf.Timestamp;
import com.piggy.microservice.notification.grpc.NotificationProto;
import com.piggy.microservice.notification.grpc.NotificationServiceGrpc;
import com.piggy.microservice.notification.domain.Frequency;
import com.piggy.microservice.notification.domain.NotificationSettings;
import com.piggy.microservice.notification.domain.NotificationType;
import com.piggy.microservice.notification.domain.Recipient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

//@EnableDiscoveryClient
@Component
public class NotificationClientImpl implements NotificationClient {
    private static final Logger logger = Logger.getLogger(NotificationClientImpl.class.getName());
    private final NotificationServiceGrpc.NotificationServiceBlockingStub notificationService;
    private final ManagedChannel channel;

    @Autowired
    public NotificationClientImpl(@Value("${notification.service.host:localhost}") String host,
                                  @Value("${notification.server.port:9092}") int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext().build();
        this.notificationService = NotificationServiceGrpc.newBlockingStub(channel);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            channel.shutdown();
        }
    }

    @Override
    public Recipient getNotificationSetting(String name) {
        NotificationProto.GetRecipientRequest request = NotificationProto.GetRecipientRequest.newBuilder()
                .setName(name)
                .build();

        NotificationProto.RecipientResponse response;
        try {
            response = notificationService.getRecipient(request);
        } catch (Exception e) {
            logger.severe("RPC failed: " + e.getMessage());
            return null;
        }

        return convertToDomainRecipient(response.getRecipient());
    }

    @Override
    public Recipient updateNotificationSetting(String name, Recipient recipient) {
        NotificationProto.Recipient protoRecipient = convertToProtobufRecipient(recipient);

        NotificationProto.UpdateRecipientRequest request = NotificationProto.UpdateRecipientRequest.newBuilder()
                .setName(name)
                .setRecipient(protoRecipient)
                .build();

        NotificationProto.RecipientResponse response;
        try {
            response = notificationService.updateRecipient(request);
        } catch (Exception e) {
            logger.severe("RPC failed: " + e.getMessage());
            return null;
        }

        return convertToDomainRecipient(response.getRecipient());
    }

    private NotificationProto.Recipient convertToProtobufRecipient(Recipient recipient) {
        NotificationProto.Recipient.Builder protobufRecipientBuilder = NotificationProto.Recipient.newBuilder()
                .setAccountName(recipient.getAccountName())
                .setEmail(recipient.getEmail());

        if (recipient.getScheduledNotifications() != null) {
            for (Map.Entry<NotificationType, NotificationSettings> entry : recipient.getScheduledNotifications().entrySet()) {
                NotificationProto.NotificationEntry protoEntry = NotificationProto.NotificationEntry.newBuilder()
                        .setType(convertToProtobufNotificationType(entry.getKey()))
                        .setSettings(convertToProtobufNotificationSettings(entry.getValue()))
                        .build();
                protobufRecipientBuilder.addScheduledNotifications(protoEntry);
            }
        }

        return protobufRecipientBuilder.build();
    }

    private Recipient convertToDomainRecipient(NotificationProto.Recipient protobufRecipient) {
        Recipient recipient = new Recipient();
        recipient.setAccountName(protobufRecipient.getAccountName());
        recipient.setEmail(protobufRecipient.getEmail());

        Map<NotificationType, NotificationSettings> domainMap = protobufRecipient.getScheduledNotificationsList().stream()
                .collect(Collectors.toMap(
                        entry -> convertToDomainNotificationType(entry.getType()),
                        entry -> convertToDomainNotificationSettings(entry.getSettings())
                ));

        recipient.setScheduledNotifications(domainMap);

        return recipient;
    }

    private NotificationProto.NotificationType convertToProtobufNotificationType(NotificationType type) {
        switch (type) {
            case BACKUP:
                return NotificationProto.NotificationType.BACKUP;
            case REMIND:
                return NotificationProto.NotificationType.REMIND;
            default:
                return NotificationProto.NotificationType.BACKUP;
        }
    }

    private NotificationType convertToDomainNotificationType(NotificationProto.NotificationType protoType) {
        switch (protoType) {
            case BACKUP:
                return NotificationType.BACKUP;
            case REMIND:
                return NotificationType.REMIND;
            default:
                return NotificationType.BACKUP;
        }
    }

    private NotificationProto.NotificationSettings convertToProtobufNotificationSettings(NotificationSettings settings) {
        NotificationProto.NotificationSettings.Builder builder = NotificationProto.NotificationSettings.newBuilder()
                .setActive(settings.getActive())
                .setFrequency(convertToProtobufFrequency(settings.getFrequency()));

        if (settings.getLastNotified() != null) {
            Timestamp timestamp = Timestamp.newBuilder()
                    .setSeconds(settings.getLastNotified().getTime() / 1000)
                    .setNanos((int) ((settings.getLastNotified().getTime() % 1000) * 1000000))
                    .build();
            builder.setLastNotified(timestamp);
        }

        return builder.build();
    }

    private NotificationSettings convertToDomainNotificationSettings(NotificationProto.NotificationSettings protoSettings) {
        NotificationSettings settings = new NotificationSettings();
        settings.setActive(protoSettings.getActive());
        settings.setFrequency(convertToDomainFrequency(protoSettings.getFrequency()));

        if (protoSettings.hasLastNotified()) {
            Timestamp timestamp = protoSettings.getLastNotified();
            Date lastNotified = new Date(timestamp.getSeconds() * 1000 + timestamp.getNanos() / 1000000);
            settings.setLastNotified(lastNotified);
        }

        return settings;
    }

    private NotificationProto.Frequency convertToProtobufFrequency(Frequency frequency) {
        switch (frequency) {
            case LOW:
                return NotificationProto.Frequency.LOW;
            case MEDIUM:
                return NotificationProto.Frequency.MEDIUM;
            case HIGH:
                return NotificationProto.Frequency.HIGH;
            default:
                return NotificationProto.Frequency.LOW;
        }
    }

    private Frequency convertToDomainFrequency(NotificationProto.Frequency protoFrequency) {
        switch (protoFrequency) {
            case LOW:
                return Frequency.LOW;
            case MEDIUM:
                return Frequency.MEDIUM;
            case HIGH:
                return Frequency.HIGH;
            default:
                return Frequency.LOW;
        }
    }
}
