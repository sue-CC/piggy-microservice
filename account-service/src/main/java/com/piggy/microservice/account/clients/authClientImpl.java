package com.piggy.microservice.account.clients;

import com.piggy.microservice.account.domain.User;
import com.piggy.microservice.auth.grpc.UserProto;
import com.piggy.microservice.auth.grpc.UserServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.logging.Logger;

@Component
public class authClientImpl implements AuthServiceClient{

    private static final Logger logger = Logger.getLogger(authClientImpl.class.getName());
    private final UserServiceGrpc.UserServiceBlockingStub authService;
    private final ManagedChannel channel;

    @Autowired
    public authClientImpl(@Value("${auth.server.host:auth-service-grpc}") String host,
                              @Value("${auth.server.port:9091}") int port){
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext().build();
        this.authService = UserServiceGrpc.newBlockingStub(channel);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            channel.shutdown();
        }
    }

    @Override
    public String createUser(User user) {
        UserProto.User grpcUser = UserProto.User.newBuilder()
                .setUsername(user.getUsername())
                .setPassword(user.getPassword())
                .build();

        UserProto.UserRequest request = UserProto.UserRequest.newBuilder()
                .setUser(grpcUser)
                .build();

        UserProto.UserResponse response;
        try {
            response = authService.addUser(request);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return "Failed to add user: " + e.getMessage();
        }
        return response.getMessage();
    }
}
