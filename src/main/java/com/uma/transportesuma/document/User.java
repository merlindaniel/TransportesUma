package com.uma.transportesuma.document;

import com.mongodb.lang.NonNull;
import com.uma.transportesuma.document.vehicle.Vehicle;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Document(collection = "User")
public class User implements Serializable {

    @Id
    @NonNull
    private String id;

    @Indexed(unique = true)
    @NonNull
    private String username;

    @Indexed(unique = true)
    @NonNull
    private String email;

    @NonNull
    private String name;

    @NonNull
    private String password;

    public User(@NonNull String username, @NonNull String name, @NonNull String email, @NonNull String password) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
