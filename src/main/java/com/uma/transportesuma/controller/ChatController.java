package com.uma.transportesuma.controller;

import com.uma.transportesuma.document.User;
import com.uma.transportesuma.document.chat.Chat;
import com.uma.transportesuma.document.vehicle.Vehicle;
import com.uma.transportesuma.service.ChatService;
import com.uma.transportesuma.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/chats")
@AllArgsConstructor
public class ChatController {
    @Autowired
    private final UserService userService;

    @Autowired
    private final ChatService chatService;

    @GetMapping("")
    public ResponseEntity<List<Chat>> findAllChats() {
        try {
            return new ResponseEntity<>(chatService.findAllChats(), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/findChat/{id}")
    public ResponseEntity<Chat> findChatById(@PathVariable final String id) {
        try {
            return new ResponseEntity<>(chatService.findChatById(id).get(), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<Chat>> findAllChatsByUser(@PathVariable final String id) {
        try {
            User user = userService.findUser(id).get();
            return new ResponseEntity<>(chatService.findChatsByUser(user), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("")
    public ResponseEntity<Chat> addChat(@RequestBody Chat chat) {
        try {
            Chat result = chatService.addChat(chat);
            if (result == null) {
                throw new Exception("Error: Alguno de los participantes del chat, no existen.");
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Chat> updateChat(@PathVariable final String id, @RequestBody Chat chat) {
        try {
            return new ResponseEntity<>(chatService.updateChat(id, chat), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Chat> deleteChat(@PathVariable final String id) {
        try {
            Chat chat = chatService.findChatById(id).get();
            chatService.removeChat(chat);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // ---------------- With authentication ----------------

    @GetMapping("/current")
    public ResponseEntity<List<Chat>> findAllChatsForCurrentUser() {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String usuarioActual = userDetails.getUsername();
            Optional<User> u = userService.findUserByUsername(usuarioActual);
            User user = null;
            List<Chat> chats = null;
            if(u.isEmpty()){
                throw new Exception("Error: Usuario no encontrado.");
            } else {
                user = u.get();
                chats = chatService.findChatsByUser(user);
                System.out.println(chats.toString());
            }
            return new ResponseEntity<>(chats, HttpStatus.OK);
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        }
    }

    @GetMapping("/current/{id}")
    public ResponseEntity<Chat> findChatForCurrentUser(@PathVariable final String id) {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String usuarioActual = userDetails.getUsername();
            Optional<User> u = userService.findUserByUsername(usuarioActual);
            User user = null;
            Chat chat = null;
            if(u.isEmpty()){
                throw new Exception("Error: Usuario no encontrado.");
            } else {
                user = u.get();
                chat = chatService.findChatById(id).orElse(null);

                if (chat == null) {
                    throw new Exception("Error: Chat no encontrado.");
                } else if (!(chat.getUser1().equals(user.getId()) || (chat.getUser2().equals(user.getId())))) {
                    throw new Exception("Error: El usuario logeado no tiene acceso a este chat.");
                }
            }
            return new ResponseEntity<>(chat, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/current/newChat")
    public ResponseEntity<Chat> addChatForCurrentUser(@RequestBody Chat chat) {
        try {
            System.out.println("Estamos en el add chat function");
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String usuarioActual = userDetails.getUsername();
            Optional<User> u = userService.findUserByUsername(usuarioActual);
            User user = null;
            if(u.isEmpty()){
                throw new Exception("Error: Usuario no encontrado.");

            } else {
                user = u.get();

                if (chat == null) {
                    throw new Exception("Error: Chat no encontrado.");
                } else if (!(chat.getUser1().equals(user.getId()) || (chat.getUser2().equals(user.getId())))) {
                    throw new Exception("Error: El usuario logeado no tiene acceso a este chat.");
                }
            }

            Chat result = chatService.addChat(chat);
            if (result == null) {
                throw new Exception("Error: Alguno de los participantes del chat, no existen.");
            }

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @PutMapping("/current/{id}")
    public ResponseEntity<Chat> updateChatForCurrentUser(@PathVariable final String id, @RequestBody Chat chat) {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String usuarioActual = userDetails.getUsername();
            Optional<User> u = userService.findUserByUsername(usuarioActual);
            User user = null;
            Chat chatInBD = null;
            if(u.isEmpty()){
                throw new Exception("Error: Usuario no encontrado.");
            } else {
                user = u.get();
                chatInBD = chatService.findChatById(id).orElse(null);

                if (chatInBD == null) {
                    throw new Exception("Error: Chat no encontrado.");
                } else if (!(chatInBD.getUser1().equals(user.getId()) || (chatInBD.getUser2().equals(user.getId())))) {
                    throw new Exception("Error: El usuario logeado no tiene acceso a este chat.");
                }
            }
            return new ResponseEntity<>(chatService.updateChat(id, chat), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
}
