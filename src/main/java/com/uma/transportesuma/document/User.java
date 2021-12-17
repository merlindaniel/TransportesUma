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

    @NonNull
    private Set<String> organizedJourneys;

    @NonNull
    private Set<String> participatedJourneys;

    @NonNull
    private Set<String> vehicles;

    public User(@NonNull String username, @NonNull String name, @NonNull String email, @NonNull String password) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.password = password;
        this.organizedJourneys = new HashSet<>();
        this.participatedJourneys = new HashSet<>();
        this.vehicles = new HashSet<>();
    }

    public void addOrganizedJourney(Journey journey) {
        organizedJourneys.add(journey.getId());
    }

    public void removeOrganizedJourney(Journey journey) {
        organizedJourneys.remove(journey.getId());
    }

    public void addParticipatedJourney(Journey journey) {
        participatedJourneys.add(journey.getId());
    }

    public void removeParticipatedJourney(Journey journey) {
        participatedJourneys.remove(journey.getId());
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle.getId());
    }

    public void removeVehicle(Vehicle vehicle) {
        vehicles.remove(vehicle.getId());
    }
}
