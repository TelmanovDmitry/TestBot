package com.example.vaiechotestbot2.service;

import com.example.vaiechotestbot2.model.User;
import com.example.vaiechotestbot2.model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRepositoryService {

    private final UserRepository userRepository;

    @Autowired
    public UserRepositoryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findUserById(long chatId){
        return userRepository.findById(chatId);
    }

    public void save(User user){
        userRepository.save(user);
    }

}
