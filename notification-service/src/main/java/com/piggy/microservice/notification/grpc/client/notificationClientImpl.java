package com.piggy.microservice.notification.grpc.client;

import com.piggy.microservice.notification.domain.Recipient;

public class notificationClientImpl implements NotificationClient {
    @Override
    public Recipient getNotificationSetting(String name) {
        return null;
    }

    @Override
    public Recipient updateNotificationSetting(String name, Recipient recipient) {
        return null;
    }
}
