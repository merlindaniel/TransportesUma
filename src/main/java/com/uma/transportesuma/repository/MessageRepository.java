package com.uma.transportesuma.repository;

import com.uma.transportesuma.document.chat.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {
}
