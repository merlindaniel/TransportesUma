package com.uma.transportesuma.document.chat;

import com.mongodb.lang.NonNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Document(collection = "Chat")
public class Chat implements Serializable {
    @Id
    @NonNull
    private String id;

    @NonNull
    private String user1;

    @NonNull
    private String user2;

    private List<Message> messages;
}
