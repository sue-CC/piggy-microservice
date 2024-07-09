package com.piggy.microservice.notification.grpc.client;

import com.piggy.microservice.notification.domain.Recipient;

public interface NotificationClient {
    Recipient getNotificationSetting(String name);
    Recipient updateNotificationSetting(String name, Recipient recipient);
}
