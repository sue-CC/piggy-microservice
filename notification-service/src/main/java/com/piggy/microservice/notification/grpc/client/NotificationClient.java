package com.piggy.microservice.notification.grpc.client;

import com.piggy.microservice.notification.domain.Recipient;

public interface NotificationClient {
    Recipient getRecipient(String name);
    Recipient updateRecipient(String name, Recipient recipient);
}
