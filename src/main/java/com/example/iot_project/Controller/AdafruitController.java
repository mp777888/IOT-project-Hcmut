package com.example.iot_project.Controller;

import com.example.iot_project.Entity.FeedData;
import com.example.iot_project.Exception.ApiResponse;
import com.example.iot_project.Repository.FeedDataRepository;
import com.example.iot_project.Service.AdafruitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/adafruit")
@RequiredArgsConstructor
@Slf4j
public class AdafruitController {

    private final AdafruitService adafruitService;
    private final FeedDataRepository feedDataRepository;

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

    // Lấy dữ liệu theo tên feed
    @GetMapping("/data/{feedName}")
    public ApiResponse<List<FeedData>> getDataByFeed(
            @PathVariable String feedName,
            @RequestParam(defaultValue = "20") int limit) {

        // Sử dụng phương thức đã tạo trong repository để lấy dữ liệu mới nhất
        List<FeedData> data = feedDataRepository.findByFeedNameOrderByTimestampDesc(feedName)
                .stream()
                .limit(limit)
                .toList();

        log.info("Fetched {} records for feed: {}", data.size(), feedName);

        return ApiResponse.<List<FeedData>>builder()
                .code(HttpStatus.OK.value())
                .message("Dữ liệu cho feed " + feedName)
                .result(data)
                .build();
    }

    // Lấy dữ liệu trong khoảng thời gian
    @GetMapping("/data/timerange")
    public ApiResponse<List<FeedData>> getDataByTimeRange(
            @RequestParam(required = false) String feedName,
            @RequestParam long startTime,
            @RequestParam long endTime) {

        List<FeedData> data;
        if (feedName != null && !feedName.isEmpty()) {
            data = feedDataRepository.findByFeedNameAndTimestampBetween(
                    feedName, startTime, endTime);
            log.info("Fetched {} records for feed: {} in time range", data.size(), feedName);
        } else {
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
    public ApiResponse<Map<String, Object>> getLatestValues() {
        List<String> allFeeds = feedDataRepository.findDistinctFeedNameBy();
        Map<String, Object> latestValues = new HashMap<>();

        for (String feed : allFeeds) {
            List<FeedData> latestData = feedDataRepository.findByFeedNameOrderByTimestampDesc(feed);
            if (!latestData.isEmpty()) {
                FeedData latest = latestData.get(0);
                Map<String, Object> feedData = new HashMap<>();
//                feedData.put("value", latest.getValue());
                feedData.put("timestamp", latest.getTimestamp());
                feedData.put("numericValue", latest.getNumericValue());
                latestValues.put(feed, feedData);
            }
        }

        log.info("Fetched latest values for {} feeds", latestValues.size());

        return ApiResponse.<Map<String, Object>>builder()
                .code(HttpStatus.OK.value())
                .message("Giá trị mới nhất của các feeds")
                .result(latestValues)
                .build();
    }

    // Gửi lệnh đến Adafruit IO (ví dụ: bật/tắt LED)
    @PostMapping("/control/{feedName}")
    public ApiResponse<String> controlDevice(
            @PathVariable String feedName,
            @RequestParam String value) {

        try {
            adafruitService.publishToFeed(feedName, value);
            log.info("Command sent to feed: {}, value: {}", feedName, value);

            return ApiResponse.<String>builder()
                    .code(HttpStatus.OK.value())
                    .message("Lệnh đã được gửi thành công")
                    .result("Đã gửi lệnh đến " + feedName + " với giá trị: " + value)
                    .build();
        } catch (Exception e) {
            log.error("Error sending command to feed: {}, value: {}", feedName, value, e);

            return ApiResponse.<String>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi khi gửi lệnh: " + e.getMessage())
                    .build();
        }
    }

    // Thêm endpoint nhận nhiều lệnh cùng lúc (hữu ích cho điều khiển nhiều thiết bị)
    @PostMapping("/control-multiple")
    public ApiResponse<Map<String, String>> controlMultipleDevices(
            @RequestBody Map<String, String> commands) {

        Map<String, String> results = new HashMap<>();

        try {
            for (Map.Entry<String, String> entry : commands.entrySet()) {
                String feedName = entry.getKey();
                String value = entry.getValue();

                adafruitService.publishToFeed(feedName, value);
                results.put(feedName, "Đã gửi giá trị: " + value);
                log.info("Command sent to feed: {}, value: {}", feedName, value);
            }

            return ApiResponse.<Map<String, String>>builder()
                    .code(HttpStatus.OK.value())
                    .message("Tất cả lệnh đã được gửi thành công")
                    .result(results)
                    .build();
        } catch (Exception e) {
            log.error("Error sending multiple commands", e);

            return ApiResponse.<Map<String, String>>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi khi gửi nhiều lệnh: " + e.getMessage())
                    .result(results)
                    .build();
        }
    }
}