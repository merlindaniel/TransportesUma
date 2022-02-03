package com.uma.transportesuma.repository;

import com.uma.transportesuma.document.chat.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRepository extends MongoRepository<Chat, String> {
}
