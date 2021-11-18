package com.uma.transportesuma.controller;

import com.uma.transportesuma.document.Journey;
import com.uma.transportesuma.document.User;
import com.uma.transportesuma.service.JourneyService;
import com.uma.transportesuma.service.UserService;
import com.uma.transportesuma.service.VehicleService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/journeys/")
@AllArgsConstructor
public class JourneyController {

    @Autowired
    private final JourneyService journeyService;

    @Autowired
    private final UserService userService;

    @Autowired
    private final VehicleService vehicleService;

    @GetMapping("/")
    public ResponseEntity<List<Journey>> findAllJourneys() {
        try {
            return new ResponseEntity<>(journeyService.findAllJourneys(), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Journey> findJourneyById(@PathVariable final String id) {
        try {
            return new ResponseEntity<>(journeyService.findJourneyById(id).get(), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Where "id" is the user's id/name/email
    @GetMapping("/participating/{id}")
    public ResponseEntity<List<Journey>> findParticipatedJourneysByUser(@PathVariable final String id,
                                                                        @RequestParam(required = false) Boolean active) {
        try {
            User user = userService.findUser(id).get();
            List<Journey> journeys = journeyService.findParticipatedJourneys(user);

            if (active != null) {
                journeys = journeys
                        .stream()
                        .filter(j -> !Boolean.valueOf(j.isFinished()).equals(active))
                        .collect(Collectors.toList());
            }

            return new ResponseEntity<>(journeys, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Where "id" is the user's id/name/email
    @GetMapping("/organizing/{id}")
    public ResponseEntity<List<Journey>> findOrganizedJourneysByUser(@PathVariable final String id,
                                                                     @RequestParam(required = false) Boolean active) {
        try {
            User user = userService.findUser(id).get();
            List<Journey> journeys = journeyService.findOrganizedJourneys(user);

            if (active != null) {
                journeys = journeys
                        .stream()
                        .filter(j -> !Boolean.valueOf(j.isFinished()).equals(active))
                        .collect(Collectors.toList());
            }

            return new ResponseEntity<>(journeys, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/")
    public ResponseEntity<Journey> addJourney(@RequestBody Journey journey) {
        try {
            return new ResponseEntity<>(journeyService.addJourney(journey), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Journey> updateJourney(@PathVariable final String id) {
        try {
            Journey journey = journeyService.findJourneyById(id).get();

            return new ResponseEntity<>(journeyService.updateJourney(journey), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Journey> deleteJourney(@PathVariable final String id) {
        try {
            Journey journey = journeyService.findJourneyById(id).get();
            journeyService.removeJourney(journey);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/participants/{id}")
    public ResponseEntity<List<User>> findParticipantsByJourney(@PathVariable final String id) {
        try {
            Journey journey = journeyService.findJourneyById(id).get();
            List<User> users = journey.getParticipants()
                    .stream()
                    .map(p -> userService.findUserById(p).get())
                    .collect(Collectors.toList());

            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Where "ParticipantId" is the user's id/name/email
    @PostMapping("/participants/{journeyId}/{participantId}")
    public ResponseEntity<Journey> addParticipant(@PathVariable final String journeyId, @PathVariable final String participantId) {
        try {
            Journey journey = journeyService.findJourneyById(journeyId).get();
            User user = userService.findUser(participantId).get();

            return new ResponseEntity<>(journeyService.addParticipant(journey, user), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Where "ParticipantId" is the user's id/name/email
    @DeleteMapping("/participants/{journeyId}/{participantId}")
    public ResponseEntity<Journey> removeParticipant(@PathVariable final String journeyId, @PathVariable final String participantId) {
        try {
            Journey journey = journeyService.findJourneyById(journeyId).get();
            User participant = userService.findUser(participantId).get();
            journeyService.removeParticipant(journey, participant);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}