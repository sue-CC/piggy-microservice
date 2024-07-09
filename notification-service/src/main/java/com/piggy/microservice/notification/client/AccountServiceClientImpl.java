package com.piggy.microservice.notification.client;

import com.piggy.microservice.account.grpc.AccountProto;
import com.piggy.microservice.account.grpc.AccountServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.logging.Logger;

@Component
public class AccountServiceClientImpl implements AccountServiceClient {
    private static final Logger logger = Logger.getLogger(AccountServiceClientImpl.class.getName());
    private final AccountServiceGrpc.AccountServiceBlockingStub accountService;
    private final ManagedChannel channel;

    @Autowired
    public AccountServiceClientImpl(@Value("${order.service.host:localhost}") String host,
                                    @Value("${order.service.port:9090}") int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext() // Note: For production use, consider using encryption (TLS)
                .build();
        this.accountService = AccountServiceGrpc.newBlockingStub(channel);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            channel.shutdown();
        }
    }

    @Override
    public String getAccount(String name) {
        try {
            // Create the request
            AccountProto.GetAccountRequest request = AccountProto.GetAccountRequest.newBuilder()
                    .setName(name)
                    .build();

            // Make the call
            AccountProto.GetAccountResponse response = accountService.getAccountByName(request);

            // Process the response
            return response.getAccount().toString(); // Assuming 'getAccountDetails' method is correct
        } catch (Exception e) {
            logger.severe("Failed to get account details for " + name + ": " + e.getMessage());
            return null; // Handle errors appropriately
        }
    }
}
