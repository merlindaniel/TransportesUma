package com.uma.transportesuma.service;

import com.uma.transportesuma.document.User;
import com.uma.transportesuma.dto.UserDTO;
import com.uma.transportesuma.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    // ---------- Queries ----------

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

    // ---------- Operations ----------

    public User addUser(UserDTO userDto) {
        BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();
        userDto.setPassword(bcpe.encode(userDto.getPassword()));
        User user = User.getUserFromDto(userDto);
        return userRepository.save(user);
    }

    public void removeUser(User user) {
        userRepository.delete(user);
    }

    public void removeAllUsers() {
        userRepository.deleteAll();
    }

    public User updateUser(String id, User user) {
        Optional<User> optUserInBD = findUserById(id);
        if (optUserInBD.isPresent()) {
            User userInBD = optUserInBD.get();
            userInBD.setName(user.getName());
            userInBD.setUsername(user.getUsername());
            userInBD.setEmail(user.getEmail());
            userInBD.setPassword(user.getPassword());
            userInBD.setPicture(user.getPicture());

            return userRepository.save(userInBD);
        }

        return null;
    }

    public User updateUser(User user) {
        return updateUser(user.getId(), user);
    }
}
