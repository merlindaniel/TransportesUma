package com.uma.transportesuma.service;

import com.uma.transportesuma.document.User;
import com.uma.transportesuma.document.vehicle.Vehicle;
import com.uma.transportesuma.repository.VehicleRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class VehicleService {

    @Autowired
    private final VehicleRepository vehicleRepository;

    @Autowired
    private final JourneyService journeyService;

    @Autowired
    private final UserService userService;

    // ---------- Queries ----------

    public List<Vehicle> findAllVehicles() {
        return vehicleRepository.findAll();
    }

    public Optional<Vehicle> findVehicleById(String id) {
        return vehicleRepository.findById(id);
    }

    public List<Vehicle> findVehiclesByOwner(User owner) {
        return vehicleRepository.findVehiclesByOwner(owner.getId());
    }

    // ---------- Operations ----------

    public Vehicle addVehicle(Vehicle vehicle) {
        User owner = userService.findUserById(vehicle.getOwner()).orElse(null);

        if (owner != null) {
            return vehicleRepository.save(vehicle);
        }

        return null;
    }

    public void removeVehicle(Vehicle vehicle) {
        User owner = userService.findUserById(vehicle.getOwner()).orElse(null);

        if (owner != null) {
            vehicleRepository.delete(vehicle);
        }
    }

    public Vehicle updateVehicle(String id, Vehicle vehicle) {
        Vehicle vehicleInDB = findVehicleById(id).orElse(null);

        if (vehicleInDB != null) {
            vehicleInDB.setCombustible(vehicle.getCombustible());
            vehicleInDB.setModel(vehicle.getModel());
            vehicleInDB.setName(vehicle.getName());
            vehicleInDB.setRegistration(vehicle.getRegistration());
            vehicleInDB.setSeats(vehicle.getSeats());

            return vehicleRepository.save(vehicleInDB);
        }

        return null;
    }
}
