package com.uma.transportesuma.document.chat;

import com.mongodb.lang.NonNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Document(collection = "Message")
public class Message implements Serializable {
    @Id
    @NonNull
    private String id;

    @NonNull
    private String user;

    @NonNull
    private String message;

    @NonNull
    private Date date;
}
