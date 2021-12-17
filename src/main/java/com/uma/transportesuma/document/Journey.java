package com.uma.transportesuma.document;

import com.mongodb.lang.NonNull;
import com.uma.transportesuma.dto.Place;
import com.uma.transportesuma.dto.PlaceByOrigin;
import com.uma.transportesuma.document.vehicle.Vehicle;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Document(collection = "Journey")
public class Journey implements Serializable {

    @Id
    @NonNull
    private String id;

    @NonNull
    private String name;

    @NonNull
    private String description;

    @NonNull
    private Place origin;

    @NonNull
    private Place destination;

    @NonNull
    // Organizer id (User)
    private String organizer;

    @NonNull
    private int numberParticipants;

    @NonNull
    // Travellers id (User)
    private Set<String> participants;

    @NonNull
    private Vehicle vehicle;

    @NonNull
    private double price;

    @NonNull
    private Date startDate;

    @NonNull
    private boolean exam;

    @NonNull
    private boolean finished;

    public Journey(@NonNull String name, @NonNull String description, @NonNull Place origin, @NonNull Place destination, @NonNull int numberParticipants, @NonNull Vehicle vehicle, double price, Date startDate, boolean exam, boolean finished) {
        this(name, description, origin, destination, null, new HashSet<>(), numberParticipants, vehicle, price, startDate, exam, finished);
    }

    public Journey(@NonNull String name, @NonNull String description, @NonNull Place origin, @NonNull Place destination, @NonNull String organizer, @NonNull Set<String> participants, @NonNull int numberParticipants, @NonNull Vehicle vehicle, double price, Date startDate, boolean exam, boolean finished) {
        this.origin = origin;
        this.destination = destination;
        this.organizer = organizer;
        this.participants = participants;
        this.numberParticipants = numberParticipants;
        this.vehicle = vehicle;
        this.price = price;
        this.startDate = startDate;
        this.exam = exam;
        this.finished = finished;
    }

    public void addParticipant(User user) {
        participants.add(user.getId());
    }

    public void removeParticipant(User user) {
        participants.remove(user.getId());
    }
}
