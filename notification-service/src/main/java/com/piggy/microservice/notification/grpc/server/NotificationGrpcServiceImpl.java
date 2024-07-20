package com.piggy.microservice.notification.grpc.server;

import com.piggy.microservice.notification.grpc.NotificationProto;
import com.piggy.microservice.notification.grpc.NotificationServiceGrpc;
import com.piggy.microservice.notification.domain.Frequency;
import com.piggy.microservice.notification.domain.NotificationSettings;
import com.piggy.microservice.notification.domain.NotificationType;
import com.piggy.microservice.notification.domain.Recipient;
import com.piggy.microservice.notification.service.RecipientService;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

import static com.piggy.microservice.notification.domain.Frequency.*;

@Service
public class NotificationGrpcServiceImpl extends NotificationServiceGrpc.NotificationServiceImplBase {
    public final RecipientService recipientService;

    @Autowired
    public NotificationGrpcServiceImpl(RecipientService recipientService) {
        this.recipientService = recipientService;
    }

    @Override
    public void getRecipient(NotificationProto.GetRecipientRequest request,
                             StreamObserver<NotificationProto.RecipientResponse> responseObserver) {
        String name = request.getName();
        Recipient recipient = recipientService.findByAccountName(name);

        NotificationProto.RecipientResponse response = NotificationProto.RecipientResponse.newBuilder()
                .setRecipient(convertToProtobufRecipient(recipient))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateRecipient(NotificationProto.UpdateRecipientRequest request,
                                StreamObserver<NotificationProto.RecipientResponse> responseObserver) {
        String name = request.getName();
        Recipient recipient = convertToDomainRecipient(request.getRecipient());
        recipientService.save(name, recipient);

        NotificationProto.RecipientResponse response = NotificationProto.RecipientResponse.newBuilder()
                .setRecipient(request.getRecipient())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
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

        // Convert repeated NotificationEntry to Map<NotificationType, NotificationSettings>.
        Map<NotificationType, NotificationSettings> domainMap = protobufRecipient.getScheduledNotificationsList().stream()
                .collect(Collectors.toMap(
                        entry -> convertToDomainNotificationType(entry.getType()),
                        entry -> convertToDomainNotificationSettings(entry.getSettings())
                ));

        recipient.setScheduledNotifications(domainMap);

        return recipient;
    }

    private NotificationProto.NotificationType convertToProtobufNotificationType(NotificationType type) {
        return switch (type) {
            case BACKUP -> NotificationProto.NotificationType.BACKUP;
            case REMIND -> NotificationProto.NotificationType.REMIND;
            // Add other cases if there are more types
            default -> NotificationProto.NotificationType.BACKUP;
        };
    }

    private NotificationType convertToDomainNotificationType(NotificationProto.NotificationType protoType) {
        return switch (protoType) {
            case BACKUP -> NotificationType.BACKUP;
            case REMIND -> NotificationType.REMIND;
            // Add other cases if there are more types
            default -> NotificationType.BACKUP;
        };
    }

    private NotificationProto.NotificationSettings convertToProtobufNotificationSettings(NotificationSettings settings) {
        NotificationProto.NotificationSettings.Builder builder = NotificationProto.NotificationSettings.newBuilder()
                .setActive(settings.getActive())
                .setFrequency(convertToProtobufFrequency(settings.getFrequency()));

        return builder.build();
    }

    private NotificationSettings convertToDomainNotificationSettings(NotificationProto.NotificationSettings protoSettings) {
        NotificationSettings settings = new NotificationSettings();
        settings.setActive(protoSettings.getActive());
        settings.setFrequency(convertToDomainFrequency(protoSettings.getFrequency()));

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
                return LOW;
            case MEDIUM:
                return MEDIUM;
            case HIGH:
                return HIGH;
            default:
                return LOW;
        }
    }
}
