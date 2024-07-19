package com.piggy.microservice.auth.grpc.client;

import com.piggy.microservice.auth.domain.User;
import com.piggy.microservice.auth.grpc.UserProto;
import com.piggy.microservice.auth.grpc.UserServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.google.protobuf.Empty;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.logging.Logger;

@Component
public class authGrpcClientImpl implements AuthClient{

    private static final Logger logger = Logger.getLogger(authGrpcClientImpl.class.getName());
    private final UserServiceGrpc.UserServiceBlockingStub authService;
    private final ManagedChannel channel;

    @Autowired
    public authGrpcClientImpl(@Value("${auth.server.host:auth-service-grpc}") String host,
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
    public String addUser(String username, String password) {
        UserProto.User grpcUser = UserProto.User.newBuilder()
                .setUsername(username)
                .setPassword(password)
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

    public List<String> getUsers() {
        Empty request = Empty.newBuilder().build();

        UserProto.UserListResponse response;
        try {
            response = authService.getUsers(request);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get users: " + e.getMessage());
        }

        // Extract usernames from the response and return them as a List<String>
        return response.getUsersList(); // Directly use getUsernamesList() to get a list of usernames
    }

    @Override
    public String updateUser(User user) {
        UserProto.User grpcUser = UserProto.User.newBuilder()
                .setUsername(user.getUsername())
                .setPassword(user.getPassword())
                .build();
        UserProto.UserRequest request = UserProto.UserRequest.newBuilder().setUser(grpcUser).build();
        UserProto.UpdateMessage response;
        try {
            response = authService.updateUser(request);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete users: " + e.getMessage());
        }
        return response.getSuccessMessage();
    }
}
