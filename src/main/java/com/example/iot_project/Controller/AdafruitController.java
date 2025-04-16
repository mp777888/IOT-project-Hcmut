package com.example.iot_project.Controller;

import com.example.iot_project.DTO.Response.LatestResponse;
import com.example.iot_project.Entity.FeedData;
import com.example.iot_project.Exception.ApiResponse;
import com.example.iot_project.Repository.FeedDataRepository;
import com.example.iot_project.Service.AdafruitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/adafruit")
@RequiredArgsConstructor
@Slf4j
public class AdafruitController {

    private final AdafruitService adafruitService;
    private final FeedDataRepository feedDataRepository;

    @Value("${adafruit.io.feeds.temperature}")
    private String temperatureFeed;

    @Value("${adafruit.io.feeds.humidity}")
    private String humidityFeed;

    @Value("${adafruit.io.feeds.soilMoisture}")
    private String soilMoistureFeed;

    @Value("${adafruit.io.feeds.light}")
    private String lightFeed;

    @Value("${adafruit.io.feeds.waterPump}")
    private String waterPumpFeed;

    @Value("${adafruit.io.feeds.led}")
    private String ledFeed;

    @Value("${adafruit.io.feeds.relay}")
    private String relayFeed;

    // Đọc tất cả dữ liệu từ MongoDB với phân trang
    @GetMapping("/data")
    public ApiResponse<Page<FeedData>> getAllData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<FeedData> dataPage = feedDataRepository.findAll(pageable);

        log.info("Fetched {} sensor data records (page {}, size {})",
                dataPage.getNumberOfElements(), page, size);

        return ApiResponse.<Page<FeedData>>builder()
                .code(HttpStatus.OK.value())
                .message("Dữ liệu cảm biến được tải thành công")
                .result(dataPage)
                .build();
    }

    @GetMapping("/data/temperature")
    public ApiResponse<List<FeedData>> getTemperatureData(
            @RequestParam(defaultValue = "20") int limit) {

        List<FeedData> data = feedDataRepository.findByFeedNameOrderByTimestampDesc(temperatureFeed)
                .stream()
                .limit(limit)
                .toList();

        log.info("Fetched {} records for feed: {}", data.size(), temperatureFeed);

        return ApiResponse.<List<FeedData>>builder()
                .code(HttpStatus.OK.value())
                .message("Dữ liệu nhiệt độ từ feed " + temperatureFeed)
                .result(data)
                .build();
    }


    @GetMapping("/data/humidity")
    public ApiResponse<List<FeedData>> getHumidityData(
            @RequestParam(defaultValue = "20") int limit) {

        List<FeedData> data = feedDataRepository.findByFeedNameOrderByTimestampDesc(humidityFeed)
                .stream()
                .limit(limit)
                .toList();

        log.info("Fetched {} records for feed: {}", data.size(), humidityFeed);

        return ApiResponse.<List<FeedData>>builder()
                .code(HttpStatus.OK.value())
                .message("Dữ liệu độ ẩm từ feed " + humidityFeed)
                .result(data)
                .build();
    }

    @GetMapping("/data/soil-moisture")
    public ApiResponse<List<FeedData>> getSoilMoistureData(
            @RequestParam(defaultValue = "20") int limit) {

        List<FeedData> data = feedDataRepository.findByFeedNameOrderByTimestampDesc(soilMoistureFeed)
                .stream()
                .limit(limit)
                .toList();

        log.info("Fetched {} records for feed: {}", data.size(), soilMoistureFeed);

        return ApiResponse.<List<FeedData>>builder()
                .code(HttpStatus.OK.value())
                .message("Dữ liệu độ ẩm đất từ feed " + soilMoistureFeed)
                .result(data)
                .build();
    }

    @GetMapping("/data/light")
    public ApiResponse<List<FeedData>> getLightData(
            @RequestParam(defaultValue = "20") int limit) {

        List<FeedData> data = feedDataRepository.findByFeedNameOrderByTimestampDesc(lightFeed)
                .stream()
                .limit(limit)
                .toList();

        log.info("Fetched {} records for feed: {}", data.size(), lightFeed);

        return ApiResponse.<List<FeedData>>builder()
                .code(HttpStatus.OK.value())
                .message("Dữ liệu ánh sáng từ feed " + lightFeed)
                .result(data)
                .build();
    }

    // Lấy dữ liệu trong khoảng thời gian
    @GetMapping("/data/timerange")
    public ApiResponse<List<FeedData>> getDataByTimeRange(
            @RequestParam(required = false) String feedName,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {

        List<FeedData> data;
        if (feedName != null && !feedName.isEmpty()) {
            data = feedDataRepository.findByFeedNameAndTimestampBetween(
                    feedName, startTime, endTime);
            log.info("Fetched {} records for feed: {} in time range", data.size(), feedName);
        }
        else {
            data = feedDataRepository.findByTimestampBetween(startTime, endTime);
            log.info("Fetched {} records in time range", data.size());
        }

        return ApiResponse.<List<FeedData>>builder()
                .code(HttpStatus.OK.value())
                .message("Dữ liệu trong khoảng thời gian")
                .result(data)
                .build();
    }

    // Lấy giá trị mới nhất của tất cả các feeds
    @GetMapping("/latest")
    public ApiResponse<LatestResponse> getLatestValues() {
        // Khởi tạo giá trị mặc định cho LatestResponse
        LatestResponse.LatestResponseBuilder responseBuilder = LatestResponse.builder()
                .temperature(0.0)
                .lastTemperatureUpdate(null)
                .humidity(0.0)
                .lastHumidityUpdate(null)
                .lightIntensity(0.0)
                .lastLightIntensityUpdate(null)
                .soilMoisture(0.0)
                .lastSoilMoistureUpdate(null);

        // Lấy dữ liệu mới nhất từ feed input-temperature
        List<FeedData> temperatureData = feedDataRepository.findByFeedNameOrderByTimestampDesc(temperatureFeed);
        if (!temperatureData.isEmpty()) {
            FeedData latest = temperatureData.getFirst();
            responseBuilder.temperature(latest.getNumericValue() != null ? latest.getNumericValue() : 0.0);
            responseBuilder.lastTemperatureUpdate(latest.getTimestamp());
        }

        // Lấy dữ liệu mới nhất từ feed input-humidity
        List<FeedData> humidityData = feedDataRepository.findByFeedNameOrderByTimestampDesc(humidityFeed);
        if (!humidityData.isEmpty()) {
            FeedData latest = humidityData.getFirst();
            responseBuilder.humidity(latest.getNumericValue() != null ? latest.getNumericValue() : 0.0);
            responseBuilder.lastHumidityUpdate(latest.getTimestamp());
        }

        // Lấy dữ liệu mới nhất từ feed input-soilMoisture
        List<FeedData> soilMoistureData = feedDataRepository.findByFeedNameOrderByTimestampDesc(soilMoistureFeed);
        if (!soilMoistureData.isEmpty()) {
            FeedData latest = soilMoistureData.getFirst();
            responseBuilder.soilMoisture(latest.getNumericValue() != null ? latest.getNumericValue() : 0.0);
            responseBuilder.lastSoilMoistureUpdate(latest.getTimestamp());
        }

        // Lấy dữ liệu mới nhất từ feed input-light
        List<FeedData> lightData = feedDataRepository.findByFeedNameOrderByTimestampDesc(lightFeed);
        if (!lightData.isEmpty()) {
            FeedData latest = lightData.getFirst();
            responseBuilder.lightIntensity(latest.getNumericValue() != null ? latest.getNumericValue() : 0.0);
            responseBuilder.lastLightIntensityUpdate(latest.getTimestamp());
        }

        LatestResponse response = responseBuilder.build();
        log.info("Fetched latest values: temperature={}, humidity={}, soilMoisture={}, lightIntensity={}",
                response.getTemperature(), response.getHumidity(), response.getSoilMoisture(), response.getLightIntensity());

        return ApiResponse.<LatestResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Giá trị mới nhất của các cảm biến")
                .result(response)
                .build();
    }

    // Điều khiển máy bơm nước (water-pump), chỉ nhận giá trị 0 hoặc 1
    @PostMapping("/control/water-pump")
    public ApiResponse<String> controlWaterPump(
            @RequestParam String value) {
        try {
            // Kiểm tra giá trị hợp lệ: 0 hoặc 1
            if (!value.equals("0") && !value.equals("1")) {
                return ApiResponse.<String>builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .message("Giá trị không hợp lệ. Chỉ nhận 0 (tắt) hoặc 1 (bật) cho máy bơm.")
                        .build();
            }

            adafruitService.publishToFeed(waterPumpFeed, value);
            log.info("Command sent to water pump (feed: {}), value: {}", waterPumpFeed, value);

            return ApiResponse.<String>builder()
                    .code(HttpStatus.OK.value())
                    .message("Lệnh đã được gửi thành công")
                    .result("Đã gửi lệnh đến máy bơm với giá trị: " + value)
                    .build();
        } catch (Exception e) {
            log.error("Error sending command to water pump (feed: {}), value: {}", waterPumpFeed, value, e);

            return ApiResponse.<String>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi khi gửi lệnh: " + e.getMessage())
                    .build();
        }
    }

    // Điều khiển relay (output-relay), chỉ nhận giá trị 0 hoặc 1
    @PostMapping("/control/relay")
    public ApiResponse<String> controlRelay(
            @RequestParam String value) {
        try {
            // Kiểm tra giá trị hợp lệ: 0 hoặc 1
            if (!value.equals("0") && !value.equals("1")) {
                return ApiResponse.<String>builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .message("Giá trị không hợp lệ. Chỉ nhận 0 (tắt) hoặc 1 (bật) cho relay.")
                        .build();
            }

            adafruitService.publishToFeed(relayFeed, value);
            log.info("Command sent to relay (feed: {}), value: {}", relayFeed, value);

            return ApiResponse.<String>builder()
                    .code(HttpStatus.OK.value())
                    .message("Lệnh đã được gửi thành công")
                    .result("Đã gửi lệnh đến relay với giá trị: " + value)
                    .build();
        } catch (Exception e) {
            log.error("Error sending command to relay (feed: {}), value: {}", relayFeed, value, e);

            return ApiResponse.<String>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi khi gửi lệnh: " + e.getMessage())
                    .build();
        }
    }

    // Điều khiển LED (output-led), chỉ nhận giá trị red, white, green, blue
    @PostMapping("/control/led")
    public ApiResponse<String> controlLed(
            @RequestParam String value) {
        try {
            // Kiểm tra giá trị hợp lệ: red, white, green, blue
            Set<String> validColors = Set.of("red", "white", "green", "blue");
            if (!validColors.contains(value.toLowerCase())) {
                return ApiResponse.<String>builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .message("Giá trị không hợp lệ. Chỉ nhận red, white, green, blue cho LED.")
                        .build();
            }

            adafruitService.publishToFeed(ledFeed, value.toLowerCase());
            log.info("Command sent to LED (feed: {}), value: {}", ledFeed, value);

            return ApiResponse.<String>builder()
                    .code(HttpStatus.OK.value())
                    .message("Lệnh đã được gửi thành công")
                    .result("Đã gửi lệnh đến LED với màu: " + value)
                    .build();
        } catch (Exception e) {
            log.error("Error sending command to LED (feed: {}), value: {}", ledFeed, value, e);

            return ApiResponse.<String>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi khi gửi lệnh: " + e.getMessage())
                    .build();
        }
    }
}