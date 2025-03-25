package com.example.iot_project.DTO.Response;

import com.example.iot_project.Enum.DeviceType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ThresholdResponse {
    DeviceType deviceType;
    Double minValue;
    Double maxValue;
}
