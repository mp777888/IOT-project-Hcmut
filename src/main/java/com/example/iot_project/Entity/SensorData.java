package com.example.iot_project.Entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("sensor_data")
@Data
@Builder
public class SensorData {
    @Id
    private String id;
    private String feedName;
    private String value;
    private long timestamp;
}