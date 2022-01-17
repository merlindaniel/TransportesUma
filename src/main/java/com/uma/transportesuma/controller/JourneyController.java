package com.uma.transportesuma.controller;

import com.uma.transportesuma.document.Journey;
import com.uma.transportesuma.document.User;
import com.uma.transportesuma.document.vehicle.Vehicle;
import com.uma.transportesuma.service.JourneyService;
import com.uma.transportesuma.service.UserService;
import com.uma.transportesuma.service.VehicleService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/journeys")
@AllArgsConstructor
public class JourneyController {

    @Autowired
    private final JourneyService journeyService;

    @Autowired
    private final UserService userService;

    @Autowired
    private final VehicleService vehicleService;

    @GetMapping("")
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










    @GetMapping("my/journey/{id}")
    public ResponseEntity<Journey> findAllMyJourney(@PathVariable final String id) {
        try {

            List<Journey> listOrganizing = this.findMyOrganizingJounerys().getBody();
            List<Journey> listParticipating = this.findMyParticipatingJounerys().getBody();
            Journey returnedJourney = null;

            for(Journey j : listOrganizing){
                if(j.getId().equals(id)){
                    returnedJourney = j;
                    break;
                }
            }

            if(returnedJourney==null){
                for(Journey j : listParticipating){
                    if(j.getId().equals(id)){
                        returnedJourney = j;
                        break;
                    }
                }
            }

            if(returnedJourney == null)
                throw new Exception("Journey not found");


            return new ResponseEntity<>(returnedJourney, HttpStatus.OK);
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

    @GetMapping("/user/participating")
    public ResponseEntity<List<Journey>> findMyParticipatingJounerys(){

        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String usuarioActual = userDetails.getUsername();

            Optional<User> u = userService.findUserByUsername(usuarioActual);
            User user = null;
            if(u.isEmpty()){
                throw new Exception("Error: Usuario no encontrado.");
            } else {
                user = u.get();
            }

            return this.findParticipatedJourneysByUser(user.getId(), null);
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

    @GetMapping("/user/organizing")
    public ResponseEntity<List<Journey>> findMyOrganizingJounerys(){

        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String usuarioActual = userDetails.getUsername();

            Optional<User> u = userService.findUserByUsername(usuarioActual);
            User user = null;
            if(u.isEmpty()){
                throw new Exception("Error: Usuario no encontrado.");
            } else {
                user = u.get();
            }

            return this.findOrganizedJourneysByUser(user.getId(), null);

        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    @PostMapping("")
    public ResponseEntity<Journey> addJourney(@RequestBody Journey journey) {
        try {

            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String usuarioActual = userDetails.getUsername();

            Optional<User> u = userService.findUserByUsername(usuarioActual);
            User user = null;
            if(u.isEmpty()){
                throw new Exception("Error: Usuario no encontrado.");
            } else {
                user = u.get();
            }

            journey.setOrganizer(user.getId());
            return new ResponseEntity<>(journeyService.addJourney(journey), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Journey> updateJourney(@PathVariable final String id, @RequestBody final Journey journey){
        try {

            Journey journeyUpdated = journeyService.updateJourney(id, journey);
            System.out.println("Journey was successfully updated. " + journey.toString());

            return new ResponseEntity<>(journeyUpdated, HttpStatus.OK);
        } catch (Exception ex) {
            System.out.println("Journey not found: " + journey.toString());
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
    @GetMapping("/participants/{journeyId}/{participantId}")
    public ResponseEntity<Journey> addParticipant(@PathVariable final String journeyId, @PathVariable final String participantId) {
        try {
            Journey journey = journeyService.findJourneyById(journeyId).get();
            User user = userService.findUser(participantId).get();

            Journey result = journeyService.addParticipant(journey, user);
            if(result != null)
                return new ResponseEntity<>(result, HttpStatus.OK);
            else
                return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
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

            return new ResponseEntity<>(journeyService.removeParticipant(journey, participant), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Busca viajes por origen, destino, numero de participantes y Fecha (que estén activos) sin usuario logueado
    @GetMapping("/search/{origin}/{destination}/{participants}/{date}")
    public ResponseEntity<List<Journey>> searchJourney(@PathVariable final String origin, @PathVariable final String destination, @PathVariable final Integer participants, @PathVariable final String date){
        try{
            return new ResponseEntity<>(journeyService.searchJourney(origin, destination, participants, date), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Busca viajes por origen, destino, numero de participantes y Fecha (que estén activos y el usuario no sea organizador o participante) con usuario logueado
    @GetMapping("/search/{origin}/{destination}/{participants}/{date}/{user}")
    public ResponseEntity<List<Journey>> searchJourneyWUSer(@PathVariable final String origin, @PathVariable final String destination, @PathVariable final Integer participants, @PathVariable final String date, @PathVariable final String user){
        try{
            return new ResponseEntity<>(journeyService.searchJourneyWUser(origin, destination, participants, date, user), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Busca todos los viajes activos con usuario logueado (que estén activos y el usuario no sea organizador o participante)
    @GetMapping("/search/{user}")
    public ResponseEntity<List<Journey>> searchAllJourneyWUSer(@PathVariable final String user){
        try{
            return new ResponseEntity<>(journeyService.searchAllJourneyWUser(user), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Busca todos los viajes activos
    @GetMapping("/search/")
    public ResponseEntity<List<Journey>> searchAllJourney(){
        try{
            return new ResponseEntity<>(journeyService.searchAllJourney(), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
