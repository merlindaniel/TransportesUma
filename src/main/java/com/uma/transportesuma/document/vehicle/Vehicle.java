package com.uma.transportesuma.document.vehicle;

import com.mongodb.lang.NonNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Document(collection = "Vehicle")
public class Vehicle implements Serializable {

    @Id
    @NonNull
    private String id;

    @NonNull
    private String registration;

    @NonNull
    // Owner id (User)
    private String owner;

    @NonNull
    private String name;

    @NonNull
    private String model;

    @NonNull
    private Combustible combustible;

    @NonNull
    private int seats;

    public Vehicle(@NonNull String registration, @NonNull String owner, @NonNull String name, @NonNull String model, @NonNull Combustible combustible, @NonNull int seats) {
        this.registration = registration;
        this.owner = owner;
        this.name = name;
        this.model = model;
        this.combustible = combustible;
        this.seats = seats;
    }
}
