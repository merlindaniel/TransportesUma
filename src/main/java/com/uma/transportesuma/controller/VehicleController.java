package com.uma.transportesuma.controller;

import com.uma.transportesuma.document.User;
import com.uma.transportesuma.document.vehicle.Vehicle;
import com.uma.transportesuma.repository.UserRepository;
import com.uma.transportesuma.repository.VehicleRepository;
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

@RestController
@RequestMapping(path = "/api/vehicles")
@AllArgsConstructor
public class VehicleController {

    @Autowired
    private final JourneyService journeyService;

    @Autowired
    private final UserService userService;

    @Autowired
    private final VehicleService vehicleService;

    @GetMapping("")
    public ResponseEntity<List<Vehicle>> findAllVehicles()  {
        try {
            return new ResponseEntity<>(vehicleService.findAllVehicles(), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> findVehicleById(@PathVariable final String id)  {
        try {
            return new ResponseEntity<>(vehicleService.findVehicleById(id).get(), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Where "id" is the user's id/name/email
    @GetMapping("/user/{id}")
    public ResponseEntity<List<Vehicle>> findVehiclesByOwner(@PathVariable final String id)  {
        try {
            User user = userService.findUser(id).get();
            return new ResponseEntity<>(vehicleService.findVehiclesByOwner(user), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/myVehicles")
    public ResponseEntity<List<Vehicle>> findMyVehicles(){

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

            return this.findVehiclesByOwner(user.getId());
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    @PostMapping("")
    public ResponseEntity<Vehicle> addVehicle(@RequestBody final Vehicle vehicle) {
        try {
            return new ResponseEntity<>(vehicleService.addVehicle(vehicle), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable final String id, @RequestBody final Vehicle vehicle) {
        try {
            return new ResponseEntity<>(vehicleService.updateVehicle(id, vehicle), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Vehicle> removeVehicle(@PathVariable final String id) {
        try {
            Vehicle vehicle = vehicleService.findVehicleById(id).get();
            vehicleService.removeVehicle(vehicle);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
