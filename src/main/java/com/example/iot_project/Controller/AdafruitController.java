package com.example.iot_project.Controller;

import com.example.iot_project.Entity.SensorData;
import com.example.iot_project.Exception.ApiResponse;
import com.example.iot_project.Repository.SensorDataRepository;
import com.example.iot_project.Service.AdafruitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/adafruit")
@RequiredArgsConstructor
public class AdafruitController {

    private final AdafruitService adafruitService;
    private final SensorDataRepository sensorDataRepository;

    // Đọc tất cả dữ liệu từ MongoDB
    @GetMapping("/data")
    public ApiResponse<List<SensorData>> getAllData() {
        return ApiResponse.<List<SensorData>>builder()
                .code(200)
                .result(sensorDataRepository.findAll())
                .build();
    }

    // Gửi lệnh đến Adafruit IO (ví dụ: bật/tắt LED)
    @PostMapping("/control/{feedName}")
    public ApiResponse<String> controlDevice(@PathVariable String feedName, @RequestParam String value) {
        adafruitService.publishToFeed(feedName, value);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Command sent to " + feedName + " with value: " + value)
                .build();
    }
}