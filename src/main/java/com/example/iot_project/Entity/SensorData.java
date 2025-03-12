package com.example.iot_project.Entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document("sensor_data")
@Data
@Builder
public class SensorData {
    @Id
    private String id;
    private String feedName;
    private String value;
    private LocalDate timestamp;

    private Double numericValue;
}