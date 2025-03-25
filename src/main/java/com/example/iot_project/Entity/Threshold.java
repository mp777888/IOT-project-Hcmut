package com.example.iot_project.Entity;


import com.example.iot_project.Enum.DeviceType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;

@Document("threshold")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Threshold {
    @Id
    String id = UUID.randomUUID().toString();
    @Field("device_type")
    DeviceType type;
    Double minValue;
    Double maxValue;
}
