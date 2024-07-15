//package com.piggy.microservice.auth.controller;
//
//import com.piggy.microservice.auth.domain.User;
//import com.piggy.microservice.auth.repository.UserRepository;
//import com.piggy.microservice.auth.service.UserService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Iterator;
//import java.util.List;
//
//@RestController
//@RequestMapping("/users")
//public class UserController {
//
//    private static final Logger log = LoggerFactory.getLogger(UserController.class);
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @RequestMapping(method = RequestMethod.POST)
//    public ResponseEntity<String> addUser(@RequestBody User user) {
//        log.atInfo().log("Attempting to add user: " + user.getUsername());
//
//        String responseMessage = userService.createUser(user);
//
//        if (responseMessage.startsWith("User already exists")) {
//            return ResponseEntity.status(HttpStatus.CONFLICT).body(responseMessage);
//        }
//        return ResponseEntity.status(HttpStatus.CREATED).body(responseMessage);
//    }
//
//
//    @RequestMapping(method = RequestMethod.GET)
//    public ResponseEntity<?> getUsers() {
//        log.atInfo().log("Attempting to get users");
//        Iterable<String> users = userService.getAllUsers();
//        if (!users.iterator().hasNext()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No users found.");
//        }
//        return ResponseEntity.ok(users);
//    }
//
//
//}
//
