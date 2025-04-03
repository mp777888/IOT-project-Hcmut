package com.example.iot_project.Controller;

import com.example.iot_project.DTO.Request.ThresholdRequest;
import com.example.iot_project.DTO.Response.ThresholdResponse;
import com.example.iot_project.Exception.ApiResponse;
import com.example.iot_project.Service.ThresholdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/threshold")
@RequiredArgsConstructor
@Slf4j
public class ThresholdController {
    private final ThresholdService thresholdService;

    // Đặt ngưỡng cho nhiệt độ (DHT20_TEMPERATURE)
    @PostMapping("/temperature")
    public ApiResponse<String> setTemperatureThreshold(
            @RequestBody ThresholdRequest request) {
        thresholdService.updateDHT20ThresholdTemperature(request);
        return ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Ngưỡng kích hoạt đã được cập nhật")
                .result("Đã cập nhật ngưỡng kích hoạt cho nhiệt độ: min=" + request.getMinValue() + ", max=" + request.getMaxValue())
                .build();
    }

    // Đặt ngưỡng cho độ ẩm (DHT20_HUMIDITY)
    @PostMapping("/humidity")
    public ApiResponse<String> setHumidityThreshold(
            @RequestBody ThresholdRequest request) {
        thresholdService.updateDHT20ThresholdHumidity(request);
        return ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Ngưỡng kích hoạt đã được cập nhật")
                .result("Đã cập nhật ngưỡng kích hoạt cho độ ẩm không khí: min=" + request.getMinValue() + ", max=" + request.getMaxValue())
                .build();
    }

    // Đặt ngưỡng cho ánh sáng (LIGHT)
    @PostMapping("/light")
    public ApiResponse<String> setLightThreshold(
            @RequestBody ThresholdRequest request) {
        thresholdService.updateLightThreshold(request);
        return ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Ngưỡng kích hoạt đã được cập nhật")
                .result("Đã cập nhật ngưỡng kích hoạt cho ánh sáng: min=" + request.getMinValue() + ", max=" + request.getMaxValue())
                .build();
    }

    // Đặt ngưỡng cho độ ẩm đất (SOIL_MOISTURE)
    @PostMapping("/soil")
    public ApiResponse<String> setSoilMoistureThreshold(
            @RequestBody ThresholdRequest request) {
        thresholdService.updateSoilMoistureThreshold(request);
        return ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Ngưỡng kích hoạt đã được cập nhật")
                .result("Đã cập nhật ngưỡng kích hoạt cho độ ẩm đất: min=" + request.getMinValue() + ", max=" + request.getMaxValue())
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<List<ThresholdResponse>> getAllThresholds() {
        List<ThresholdResponse> thresholds = thresholdService.getAllThresholds();
        return ApiResponse.<List<ThresholdResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách ngưỡng thành công")
                .result(thresholds)
                .build();
    }
}
