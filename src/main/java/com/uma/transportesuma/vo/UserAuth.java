package com.uma.transportesuma.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class UserAuth {
    @SerializedName("username")
    private String username;
    @SerializedName("password")
    private String password;
}
