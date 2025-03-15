package com.example.iot_project.Entity;

import com.example.iot_project.Enum.DeviceType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.UUID;

@Document
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class Device {
    @Id
    String id = UUID.randomUUID().toString();;
    String feedName;
    Boolean status;
    @Field("device_type")
    DeviceType type;
    String location;
    LocalDate timestamp;


    @DBRef
    Report report;

}
