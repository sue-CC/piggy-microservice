package com.piggy.microservice.auth.service;

import com.piggy.microservice.auth.domain.User;
import com.piggy.microservice.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService{

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final UserRepository userRepository;
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String createUser(User user) {
        String hash = encoder.encode(user.getPassword());
        user.setPassword(hash);

        userRepository.save(user);

        return ("New user has been created:" + user.getUsername());
    }


    @Override
    public Iterable<String> getAllUsers() {
        List<User> userList = new ArrayList<>();
        userRepository.findAll().forEach(userList::add);
        return userList.stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
    }

    @Override
    public String updateUser(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            User oldUser = userRepository.findByUsername(user.getUsername());
            oldUser.setPassword(user.getPassword());
            oldUser.setUsername(user.getUsername());
        }
        else userRepository.save(user);
        return ("User has been updated:" + user.getUsername());
    }

}
