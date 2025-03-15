package com.example.iot_project.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document("sensor_data")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedData {
    @Id
    private String id;
    private String feedName;
    private LocalDate timestamp;

    private Double numericValue;
}