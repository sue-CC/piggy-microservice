package com.piggy.microservice.auth.grpc.server;

import com.google.protobuf.Empty;
import com.piggy.microservice.auth.domain.User;
import com.piggy.microservice.auth.grpc.UserProto;
import com.piggy.microservice.auth.grpc.UserServiceGrpc;
import com.piggy.microservice.auth.service.UserService;
import io.grpc.stub.StreamObserver;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UserGrpcServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;

    public UserGrpcServiceImpl(UserService userService) {
        this.userService = userService;
    }


    @Override
    public void addUser(UserProto.UserRequest request, StreamObserver<UserProto.UserResponse> responseObserver) {
        User user = new User();
        user.setUsername(request.getUser().getUsername());
        user.setPassword(request.getUser().getPassword());

        String responseMessage = userService.createUser(user);
        UserProto.UserResponse response = UserProto.UserResponse.newBuilder()
                .setMessage(responseMessage)
                .build();

        if (responseMessage.startsWith("User already exists")) {
            responseObserver.onError(new RuntimeException(responseMessage));
        } else {
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getUsers(Empty request, StreamObserver<UserProto.UserListResponse> responseObserver) {
        // Retrieve the list of usernames from the user service
        Iterable<String> usernames = userService.getAllUsers();

        // Convert the Iterable<String> to List<String>
        List<String> usernameList = StreamSupport.stream(usernames.spliterator(), false)
                .collect(Collectors.toList());

        // Build the response with the list of usernames
        UserProto.UserListResponse response = UserProto.UserListResponse.newBuilder()
                .addAllUsers(usernameList)  // Add all usernames to the response
                .build();

        // Send the response to the client and mark the RPC call as completed
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteUsers(UserProto.UserRequest request, StreamObserver<UserProto.DeleteMessage> responseObserver) {
        User user = new User();
        user.setUsername(request.getUser().getUsername());
        user.setPassword(request.getUser().getPassword());
            userService.deleteUser(user);
            String responseMessage = "User deleted successfully";
            UserProto.DeleteMessage response = UserProto.DeleteMessage.newBuilder()
                    .setDeleteMessage(responseMessage)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

}
