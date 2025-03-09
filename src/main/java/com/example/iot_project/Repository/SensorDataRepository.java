package com.example.iot_project.Repository;

import com.example.iot_project.Entity.SensorData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SensorDataRepository extends MongoRepository<SensorData, String> {
}