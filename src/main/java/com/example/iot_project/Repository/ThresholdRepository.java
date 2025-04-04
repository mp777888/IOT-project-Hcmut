package com.example.iot_project.Repository;

import com.example.iot_project.Entity.Threshold;
import com.example.iot_project.Enum.DeviceType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThresholdRepository extends MongoRepository<Threshold, String> {
    Optional<Threshold> findByType(DeviceType type);
}
