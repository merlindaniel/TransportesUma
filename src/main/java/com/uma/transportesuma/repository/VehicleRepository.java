package com.uma.transportesuma.repository;

import com.uma.transportesuma.document.vehicle.Vehicle;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VehicleRepository extends MongoRepository<Vehicle, String> {
    public List<Vehicle> findVehiclesByOwner(String owner);
}
