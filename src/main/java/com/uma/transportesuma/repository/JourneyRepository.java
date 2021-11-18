package com.uma.transportesuma.repository;

import com.uma.transportesuma.document.Journey;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JourneyRepository extends MongoRepository<Journey, String> {
}
