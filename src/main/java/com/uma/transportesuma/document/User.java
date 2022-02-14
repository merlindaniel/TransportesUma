package com.uma.transportesuma.document;


import com.mongodb.lang.NonNull;
import com.uma.transportesuma.dto.UserDTO;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

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

    private String picture;

    private String stripeAccount;

    public User(@NonNull String username, @NonNull String name, @NonNull String email, @NonNull String password) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.password = password;
        this.picture = "";
    }

    @Transient
    public static User getUserFromDto(UserDTO userDTO){
        User user = new User(userDTO.getUsername(), userDTO.getName(), userDTO.getEmail(), userDTO.getPassword());
        if(userDTO.getPicture() == null)
            user.setPicture("");
        else
            user.setPicture(userDTO.getPicture());

        if(userDTO.getStripeAccount() == null)
            user.setStripeAccount("");
        else
            user.setStripeAccount(userDTO.getStripeAccount());

        return user;
    }

    @NonNull
    public String getId() {
        return id;
    }
}
