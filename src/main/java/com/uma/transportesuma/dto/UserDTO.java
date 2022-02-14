package com.uma.transportesuma.dto;

import lombok.*;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserDTO {
    @NotNull
    private String username;

    @NotNull
    private String email;

    @NotNull
    private String name;

    @NotNull
    private String password;

    private String picture;

    private String stripeAccount;

}
