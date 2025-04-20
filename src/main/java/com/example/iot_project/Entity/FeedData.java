package com.example.iot_project.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("feed_data")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedData {
    @Id
    private String id;
    @Indexed
    private String feedName;
    @Indexed
    private LocalDateTime timestamp;
    private Double numericValue;
}