package com.example.iot_project.Repository;

import com.example.iot_project.Entity.SensorData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SensorDataRepository extends MongoRepository<SensorData, String> {
    List<SensorData> findByFeedNameOrderByTimestampDesc(String feedName);
    List<SensorData> findByFeedNameAndTimestampBetween(String feedName, long startTime, long endTime);
    List<SensorData> findByTimestampBetween(long startTime, long endTime);
    List<String> findDistinctFeedNameBy();
}