package com.example.iot_project.DTO.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LatestResponse {
    Double temperature;
    LocalDateTime lastTemperatureUpdate;
    Double humidity;
    LocalDateTime lastHumidityUpdate;
    Double lightIntensity;
    LocalDateTime lastLightIntensityUpdate;
    Double soilMoisture;
    LocalDateTime lastSoilMoistureUpdate;
}
