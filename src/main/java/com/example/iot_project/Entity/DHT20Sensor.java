package com.example.iot_project.Entity;

import com.example.iot_project.Enum.DeviceType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DHT20Sensor extends Device{
    Double temperature;
    Double humidity;
    LocalDateTime lastTemperatureUpdate;
    LocalDateTime lastHumidityUpdate;

//    @Builder.Default
//    DeviceType type = DeviceType.SENSOR_DHT20;

}
