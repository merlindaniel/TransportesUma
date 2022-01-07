package com.uma.transportesuma.service;

import com.uma.transportesuma.document.Journey;
import com.uma.transportesuma.document.User;
import com.uma.transportesuma.repository.JourneyRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class JourneyService {

    @Autowired
    private final JourneyRepository journeyRepository;

    @Autowired
    private final UserService userService;

    // ---------- Queries ----------

    public List<Journey> findAllJourneys() {
        return journeyRepository.findAll();
    }

    public Optional<Journey> findJourneyById(String id) {
        return journeyRepository.findById(id);
    }

    public List<Journey> findOrganizedJourneys(User user) {
        return findAllJourneys()
                .stream()
                .filter(j -> j.getOrganizer().equals(user.getId()))
                .collect(Collectors.toList());
    }

    public List<Journey> findActiveOrganizedJourneys(User user) {
        return findOrganizedJourneys(user)
                .stream()
                .filter(j -> !j.isFinished())
                .collect(Collectors.toList());
    }

    public List<Journey> findParticipatedJourneys(User user) {
        return findAllJourneys()
                .stream()
                .filter(j -> j.getParticipants().contains(user.getId()))
                .collect(Collectors.toList());
    }

    public List<Journey> findActiveParticipatedJourneys(User user) {
        return findParticipatedJourneys(user)
                .stream()
                .filter(j -> !j.isFinished())
                .collect(Collectors.toList());
    }

    // ---------- Operations ----------

    public Journey addJourney(Journey journey) {
        User organizer = userService.findUserById(journey.getOrganizer()).orElse(null);
        if (organizer != null) {
            return journeyRepository.save(journey);
        }

        return null;
    }

    public Journey updateJourney(String id, Journey journey) {
        Optional<Journey> optJourneyInBD = findJourneyById(id);

        if (optJourneyInBD.isPresent()) {
            Journey journeyInBD = optJourneyInBD.get();
            journeyInBD.setName(journey.getName());
            journeyInBD.setDescription(journey.getDescription());
            journeyInBD.setOrigin(journey.getOrigin());
            journeyInBD.setDestination(journey.getDestination());
            journeyInBD.setPrice(journey.getPrice());
            journeyInBD.setVehicle(journey.getVehicle());
            journeyInBD.setOrganizer(journey.getOrganizer());
            journeyInBD.setParticipants(journey.getParticipants());
            journeyInBD.setStartDate(journey.getStartDate());
            journeyInBD.setFinished(journey.isFinished());
            journeyInBD.setExam(journey.isExam());

            return journeyRepository.save(journeyInBD);
        }

        return null;
    }

    public Journey updateJourney(Journey journey) {
        return updateJourney(journey.getId(), journey);
    }

    public Journey addParticipant(Journey journey, User participant) {
        if(journey.getParticipants().size() + 1 < journey.getVehicle().getSeats()){
            journey.addParticipant(participant);
            return updateJourney(journey);
        }
        return null;

    }

    public Journey removeParticipant(Journey journey, User participant) {
        journey.removeParticipant(participant);

        return updateJourney(journey);
    }

    public void removeJourney(Journey journey) {
        // Delete organizer relation
//        Optional<User> optOrganizer = userService.findUserById(journey.getOrganizer());
//        if (optOrganizer.isPresent()) {
//            optOrganizer.get().removeOrganizedJourney(journey);
//            userService.updateUser(optOrganizer.get());
//        }

        // Delete participant relations
//        for (String userId : journey.getParticipants()) {
//            Optional<User> optParticipant = userService.findUserById(userId);
//
//            if (optParticipant.isPresent()) {
//                User participant = optParticipant.get();
//                participant.removeParticipatedJourney(journey);
//                userService.updateUser(participant);
//            }
//        }

        // Finally deletes journey from database
        journeyRepository.delete(journey);
    }
}
