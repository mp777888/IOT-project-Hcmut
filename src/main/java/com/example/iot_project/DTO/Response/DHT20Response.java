package com.example.iot_project.DTO.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DHT20Response {
    Double temperature;
    Double humidity;
    String lastTemperatureUpdate;
    String lastHumidityUpdate;
}
