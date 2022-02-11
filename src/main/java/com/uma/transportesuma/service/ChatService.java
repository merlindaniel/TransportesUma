package com.uma.transportesuma.service;

import com.uma.transportesuma.document.User;
import com.uma.transportesuma.document.chat.Chat;
import com.uma.transportesuma.repository.ChatRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ChatService {

    @Autowired
    private final UserService userService;

    @Autowired
    private final ChatRepository chatRepository;

    // ---------- Queries ----------
    public List<Chat> findAllChats() {
        return chatRepository.findAll();
    }

    public Optional<Chat> findChatById(String id) {
        return chatRepository.findById(id);
    }

    public List<Chat> findChatsByUser(User user) {
        return findAllChats()
                .stream()
                .filter(chat -> chat.getUser1().equals(user.getId()) || chat.getUser2().equals(user.getId()))
                .collect(Collectors.toList());
    }

    // ---------- Operations ----------

    public Chat addChat(Chat chat) {
        String user1Id = chat.getUser1(), user2Id = chat.getUser2();
        User user1 = userService.findUser(user1Id).orElse(null);
        User user2 = userService.findUser(user2Id).orElse(null);

        if (user1 != null && user2 != null) {
            return chatRepository.save(chat);
        }

        return  null;
    }

    public Chat updateChat(String id, Chat chat) {
        Optional<Chat> optChatInBD = findChatById(id);

        if (optChatInBD.isPresent()) {
            Chat chatInBD = optChatInBD.get();
            chatInBD.setMessages(chat.getMessages());

            return chatRepository.save(chatInBD);
        }

        return null;
    }

    public void removeChat(Chat chat) {
        chatRepository.delete(chat);
    }
}
