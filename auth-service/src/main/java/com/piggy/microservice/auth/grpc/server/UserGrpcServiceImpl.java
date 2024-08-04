package com.piggy.microservice.auth.grpc.server;

import com.google.protobuf.Empty;
import com.piggy.microservice.auth.domain.User;
import com.piggy.microservice.auth.grpc.UserProto;
import com.piggy.microservice.auth.grpc.UserServiceGrpc;
import com.piggy.microservice.auth.repository.UserRepository;
import com.piggy.microservice.auth.service.UserService;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Service
public class UserGrpcServiceImpl extends UserServiceGrpc.UserServiceImplBase {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final UserRepository userRepository;

    public UserGrpcServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();


    @Override
    public void addUser(UserProto.UserRequest request, StreamObserver<UserProto.UserResponse> responseObserver) {
        User user = new User();
        user.setUsername(request.getUser().getUsername());
        user.setPassword(encoder.encode(request.getUser().getPassword()));
        userRepository.save(user);
        UserProto.UserResponse response = UserProto.UserResponse.newBuilder()
                .setMessage("New user has been created:" + user.getUsername())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

//    @Override
//    public void getUsers(Empty request, StreamObserver<UserProto.UserListResponse> responseObserver) {
//        // Retrieve the list of usernames from the user service
//        Iterable<String> usernames = userService.getAllUsers();
//
//        // Convert the Iterable<String> to List<String>
//        List<String> usernameList = StreamSupport.stream(usernames.spliterator(), false)
//                .collect(Collectors.toList());
//
//        // Build the response with the list of usernames
//        UserProto.UserListResponse response = UserProto.UserListResponse.newBuilder()
//                .addAllUsers(usernameList)  // Add all usernames to the response
//                .build();
//
//        // Send the response to the client and mark the RPC call as completed
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }

    @Override
    public void updateUser(UserProto.UserRequest request, StreamObserver<UserProto.UpdateMessage> responseObserver) {
        String username = request.getUser().getUsername();
        User existingUser = userRepository.findByUsername(username);

        if (existingUser != null) {
            existingUser.setPassword(request.getUser().getPassword());
            existingUser.setUsername(username);
            userRepository.save(existingUser);
        } else {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(encoder.encode(request.getUser().getPassword()));
            userRepository.save(newUser);
        }

        UserProto.UpdateMessage response = UserProto.UpdateMessage.newBuilder()
                .setSuccessMessage("User has been updated: " + username)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


}
