package com.uma.transportesuma.service;

import com.uma.transportesuma.document.User;
import com.uma.transportesuma.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findUserById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findUser(String id) {
        Optional<User> optUser = findUserById(id);

        if (optUser.isEmpty())
            optUser = findUserByUsername(id);

        if (optUser.isEmpty())
            optUser = findUserByEmail(id);

        return optUser;
    }

    public User addUser(User user) {
        userRepository.save(user);

        return user;
    }

    public void removeUser(User user) {
        userRepository.delete(user);
    }
}
