package com.uma.transportesuma.document.chat;

import com.mongodb.lang.NonNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
@NoArgsConstructor
@Getter
@Setter
@ToString
@Document(collection = "BasicUser")
public class BasicUser implements Serializable {
    @Id
    @NonNull
    private String id;

    @NonNull
    private String name;

    @NonNull
    private String email;

    @NonNull
    private String username;
    private String picture;

    public BasicUser(@NonNull String id, @NonNull String name,
                     @NonNull String email, @NonNull String username, String picture){
        this.id = id;
        this.name = name;
        this.email = email;
        this.username = username;
        this.picture = picture;
    }

    @NonNull
    public String getId() {
        return id;
    }

}
