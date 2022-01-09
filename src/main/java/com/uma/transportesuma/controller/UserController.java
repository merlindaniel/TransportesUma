package com.uma.transportesuma.controller;

import com.uma.transportesuma.document.User;
import com.uma.transportesuma.document.vehicle.Vehicle;
import com.uma.transportesuma.dto.UserDTO;
import com.uma.transportesuma.exception.UserError;
import com.uma.transportesuma.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/users")
@AllArgsConstructor
//@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private final UserService userService;

    @GetMapping("")
    public ResponseEntity<List<User>> findAllUsers() {
        try {
            return new ResponseEntity<>(userService.findAllUsers(), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> findUser(@PathVariable final String id) {
        try {
            return new ResponseEntity<>(userService.findUser(id).get(), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("")
    public ResponseEntity<User> addUser(@Valid @RequestBody final UserDTO userDto) {
        try {
            return new ResponseEntity<>(userService.addUser(userDto), HttpStatus.OK);
        } catch (Exception ex) {
            UserError ue = new UserError();
            ue.setError(ex.getMessage());
            return new ResponseEntity<>(ue, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<User> deleteUser(@PathVariable final String id) {
        try {
            Optional<User> optUser = userService.findUser(id);
            userService.removeUser(optUser.get());

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable final String id, @RequestBody final User user) {
        User updatedUser = userService.updateUser(id, user);

        if (updatedUser != null) {
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/recovery/password")
    public ResponseEntity<?> recoverPassword(@RequestBody String email){
        try{
            this.userService.recoverPasswordByEmail(email);
            return null;
        } catch (Exception ex){
            return null;
        }
    }

    @GetMapping("/current")
    public ResponseEntity<User> getCurrentUser(){

        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String usuarioActual = userDetails.getUsername();

            Optional<User> u = userService.findUserByUsername(usuarioActual);
            User user = null;
            if(u.isEmpty()){
                throw new Exception("Error: Usuario no encontrado.");
            } else {
                user = u.get();
            }

            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/current")
    public ResponseEntity<User> editCurrentUser(@RequestBody final User user) {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String usuarioActual = userDetails.getUsername();

            Optional<User> u = userService.findUserByUsername(usuarioActual);
            User userInBD = null;
            if(u.isEmpty()){
                throw new Exception("Error: Usuario no encontrado.");
            } else {
                userInBD = u.get();
            }

            return new ResponseEntity<>(userService.updateUser(userInBD.getId(), user), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/current")
    public ResponseEntity<User> deleteCurrentUser() {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String usuarioActual = userDetails.getUsername();

            Optional<User> u = userService.findUserByUsername(usuarioActual);
            User userInBD = null;
            if(u.isEmpty()){
                throw new Exception("Error: Usuario no encontrado.");
            } else {
                userInBD = u.get();
            }

            // Borrar usuario
            userService.removeUser(userInBD);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
