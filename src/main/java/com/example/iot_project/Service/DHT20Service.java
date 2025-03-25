//package com.example.iot_project.Service;
//
//import com.example.iot_project.Entity.DHT20Sensor;
//import com.example.iot_project.Entity.Device;
//import com.example.iot_project.Enum.DeviceType;
//import com.example.iot_project.Repository.DeviceRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class DHT20Service {
//
//    private final DeviceRepository deviceRepository;
//
//    @Value("${adafruit.io.feeds.temperature}")
//    private String temperatureFeed;
//
//
//    // Cập nhật nhiệt độ từ feed
//    public void updateTemperature(String value) {
//        try {
//            log.info("Đã nhận giá trị nhiệt độ: {}", value);
//            double temperature = Double.parseDouble(value);
//            DHT20Sensor sensor = getOrCreateDHT20Sensor();
//
//            sensor.setTemperature(temperature);
//            sensor.setLastTemperatureUpdate(LocalDateTime.now());
//            sensor.setTimestamp(LocalDate.now());
//            sensor.setStatus(true);
//
//            deviceRepository.save(sensor);
//            log.info("Đã cập nhật nhiệt độ: {} cho cảm biến DHT20", temperature);
//        } catch (NumberFormatException e) {
//            log.error("Lỗi parse giá trị nhiệt độ: {}", value);
//        }
//    }
//
//    // Cập nhật độ ẩm từ feed
//    public void updateHumidity(String value) {
//        try {
//            double humidity = Double.parseDouble(value);
//            DHT20Sensor sensor = getOrCreateDHT20Sensor();
//
//            sensor.setHumidity(humidity);
//            sensor.setLastHumidityUpdate(LocalDateTime.now());
//            sensor.setTimestamp(LocalDate.now());
//            sensor.setStatus(true);
//
//            deviceRepository.save(sensor);
//            log.info("Đã cập nhật độ ẩm: {} cho cảm biến DHT20", humidity);
//        } catch (NumberFormatException e) {
//            log.error("Lỗi parse giá trị độ ẩm: {}", value);
//        }
//    }
//
//    // Lấy hoặc tạo mới đối tượng DHT20Sensor
//    private DHT20Sensor getOrCreateDHT20Sensor() {
//        // Tìm kiếm theo feed nhiệt độ (hoặc bạn có thể tìm theo một id cố định)
//        Optional<Device> deviceOpt = deviceRepository.findByFeedName(temperatureFeed);
//
//        // Tạo mới nếu chưa có
//        return deviceOpt.map(device -> (DHT20Sensor) device).orElseGet(() -> DHT20Sensor.builder()
//                .feedName(temperatureFeed)
//                .type(DeviceType.DHT20)
//                .status(true)
//                .location("Default Location")  // Cập nhật location phù hợp
//                .timestamp(LocalDate.now())
//                .build());
//    }
//}