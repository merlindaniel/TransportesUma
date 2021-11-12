package com.uma.transportesuma.controller;

import com.uma.transportesuma.document.User;
import com.uma.transportesuma.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/users/")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private final UserService userService;

    @GetMapping("/")
    public List<User> findAllUsers() {
        return userService.findAllUsers();
    }

    @GetMapping("/{id}")
    public User findUser(@PathVariable String id) {
        return userService.findUser(id).orElse(null);
    }

    @PostMapping("/")
    public User addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) {
        Optional<User> optUser = userService.findUser(id);

        optUser.ifPresent(userService::removeUser);
    }
}
