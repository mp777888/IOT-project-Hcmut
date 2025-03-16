package com.example.iot_project.Controller;

import com.example.iot_project.DTO.Response.DHT20Response;
import com.example.iot_project.Entity.DHT20Sensor;
import com.example.iot_project.Enum.DeviceType;
import com.example.iot_project.Exception.ApiResponse;
import com.example.iot_project.Repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
@Slf4j
public class DHT20Controller {
    private final DeviceRepository deviceRepository;

    @GetMapping("/dht20/latest")
    public ApiResponse<DHT20Response> getLatestDHT20Data() {
        List<DHT20Sensor> sensors = deviceRepository.findAll().stream()
                .filter(device -> device.getType() == DeviceType.SENSOR_DHT20)
                .map(device -> (DHT20Sensor) device)
                .toList();

        if (sensors.isEmpty()) {
            log.warn("Không tìm thấy cảm biến DHT20 nào");
            return ApiResponse.<DHT20Response>builder()
                    .code(404)
                    .message("Không tìm thấy cảm biến DHT20 nào")
                    .build();
        }

        return ApiResponse.<DHT20Response>builder()
                .code(200)
                .message("Dữ liệu cảm biến DHT20 được tải thành công")
                .result(DHT20Response.builder()
                        .temperature(sensors.getFirst().getTemperature())
                        .humidity(sensors.getFirst().getHumidity())
                        .lastTemperatureUpdate(String.valueOf(sensors.getFirst().getLastTemperatureUpdate()))
                        .lastHumidityUpdate(String.valueOf(sensors.getFirst().getLastHumidityUpdate()))
                        .build())
                .build();
    }
}
