package com.example.iot_project.Entity;

import com.example.iot_project.Enum.DeviceType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document("notification")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {
    @Id
    String id = UUID.randomUUID().toString();
    String message;
    String deviceName;
    LocalDateTime timestamp;
}
