package com.example.iot_project.Service;

import com.example.iot_project.DTO.Request.ThresholdRequest;
import com.example.iot_project.DTO.Response.ThresholdResponse;
import com.example.iot_project.Entity.Threshold;
import com.example.iot_project.Enum.DeviceType;
import com.example.iot_project.Repository.ThresholdRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Service
public class ThresholdService {

    final ThresholdRepository thresholdRepository;
//    final JavaMailSender mailSender;
    final NotificationService notificationService;
    final AdafruitService AdafruitService;

    @Value("${adafruit.io.feeds.waterPump}")
    private String waterPumpFeed;

    @Value("${adafruit.io.feeds.led}")
    private String ledFeed;

    @Value("${adafruit.io.feeds.relay}")
    private String relayFeed;


    @Value("${app.notification.email}")
    String notificationEmail;

    // Cooldown để tránh gửi email liên tục (5 phút)
    final Map<DeviceType, Long> lastNotificationTimes = new HashMap<>();
    static long NOTIFICATION_COOLDOWN = 5 * 60 * 1000; // 5 phút

    // Cập nhật ngưỡng chung cho bất kỳ thiết bị nào
    public void updateThreshold(DeviceType type, ThresholdRequest request) {
        Double minValue = request.getMinValue();
        Double maxValue = request.getMaxValue();

        if (minValue != null && maxValue != null && minValue > maxValue) {
            throw new IllegalArgumentException("Min value must be less than or equal to max value, and both must not be null");
        }

        Threshold threshold = thresholdRepository.findByType(type)
                .orElse(Threshold.builder()
                        .type(type)
                        .minValue(null)
                        .maxValue(null)
                        .build());
        if(minValue != null){
            threshold.setMinValue(minValue);
        }
        if(maxValue != null){
            threshold.setMaxValue(maxValue);
        }
        thresholdRepository.save(threshold);
        log.info("Updated threshold for {}: min={}, max={}", type, minValue, maxValue);
    }

    // Cập nhật ngưỡng cho nhiệt độ (DHT20_TEMPERATURE)
    public void updateDHT20ThresholdTemperature(ThresholdRequest request) {
        updateThreshold(DeviceType.DHT20_TEMPERATURE, request);
    }

    // Cập nhật ngưỡng cho độ ẩm (DHT20_HUMIDITY)
    public void updateDHT20ThresholdHumidity(ThresholdRequest request) {
        updateThreshold(DeviceType.DHT20_HUMIDITY, request);
    }

    // Cập nhật ngưỡng cho độ ẩm đất (SOIL_MOISTURE)
    public void updateSoilMoistureThreshold(ThresholdRequest request) {
        updateThreshold(DeviceType.SOIL_MOISTURE, request);
    }

    // Cập nhật ngưỡng cho ánh sáng (LIGHT)
    public void updateLightThreshold(ThresholdRequest request) {
        updateThreshold(DeviceType.LIGHT, request);
    }


    // Kiểm tra giá trị cảm biến và gửi email nếu nằm ngoài khoảng ngưỡng
    public void checkAndNotify(DeviceType type, Double currentValue) {
        Threshold threshold = thresholdRepository.findByType(type).orElse(null);
        if (threshold == null || (threshold.getMinValue() == null && threshold.getMaxValue() == null)) {
            log.warn("No threshold defined for device type: {}", type);
            return;
        }

        Double minValue = threshold.getMinValue();
        Double maxValue = threshold.getMaxValue();
        if (currentValue != null) {
            long currentTime = System.currentTimeMillis();
            Long lastNotificationTime = lastNotificationTimes.getOrDefault(type, 0L);

            if (currentTime - lastNotificationTime < NOTIFICATION_COOLDOWN) {
                log.info("Notification for {} skipped due to cooldown", type);
                return;
            }

            if (minValue != null && currentValue < minValue) {
                String message = String.format(
                        "Cảnh báo cho thiết bị " + type.toString() + " : Giá trị %s quá thấp! Giá trị hiện tại: %.2f, Ngưỡng tối thiểu: %.2f",
                        type, currentValue, minValue
                );
//                sendEmail(message);
                lastNotificationTimes.put(type, currentTime);
                notificationService.lowerBoundMessage(message,type.toString());
                log.info("Sent notification for {}: currentValue={} is below min={}", type, currentValue, minValue);
            }
            else if (maxValue != null && currentValue > maxValue) {
                String message = String.format(
                        "Cảnh báo cho thiết bị " + type.toString() + ": Giá trị %s quá cao! Giá trị hiện tại: %.2f, Ngưỡng tối đa: %.2f",
                        type, currentValue, maxValue
                );
//                sendEmail(message);
                lastNotificationTimes.put(type, currentTime);
                notificationService.upperBoundMessage(message,type.toString());
                log.info("Sent notification for {}: currentValue={} is above max={}", type, currentValue, maxValue);
            }
        }
    }

    // Kiểm tra giá trị cảm biến và gửi email nếu nằm ngoài khoảng ngưỡng
    public void checkAndActivate(DeviceType type, Double currentValue) {
        Threshold threshold = thresholdRepository.findByType(type).orElse(null);
        if (threshold == null || threshold.getMinValue() == null || threshold.getMaxValue() == null) {
            log.warn("No threshold defined for device type: {}", type);
            return;
        }

        String deviceFeed;

        if (type == DeviceType.SOIL_MOISTURE || type == DeviceType.DHT20_HUMIDITY) {
            deviceFeed = waterPumpFeed;
        } else if (type == DeviceType.LIGHT) {
            deviceFeed = ledFeed;
        } else {
            log.warn("No feed defined for device type: {}", type);
            return;
        }

        Double minValue = threshold.getMinValue();
        Double maxValue = threshold.getMaxValue();
        if (currentValue != null) {
            //long currentTime = System.currentTimeMillis();
            

            if (currentValue < minValue) {
                AdafruitService.publishToFeed(deviceFeed, (type == DeviceType.LIGHT)? "white" : "1"); // Bật thiết bị
                log.info("activate {}: currentValue={} is below min={}", deviceFeed, currentValue, minValue);
            }
            else if (currentValue > maxValue) {
                AdafruitService.publishToFeed(deviceFeed, (type == DeviceType.LIGHT)? "black" :"0"); // Tắt thiết bị
                log.info("Sent notification for {}: currentValue={} is above max={}", deviceFeed, currentValue, maxValue);
            }
        }
    }


    public List<ThresholdResponse> getAllThresholds() {
        List<Threshold> thresholds = thresholdRepository.findAll();
        return thresholds.stream()
                .map(threshold -> ThresholdResponse.builder()
                        .deviceType(threshold.getType())
                        .minValue(threshold.getMinValue())
                        .maxValue(threshold.getMaxValue())
                        .build())
                .collect(Collectors.toList());
    }

    // Gửi email thông báo
    private void sendEmail(String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(notificationEmail);
            mailMessage.setSubject("Cảnh báo vượt ngưỡng từ hệ thống IoT");
            mailMessage.setText(message);
            mailMessage.setFrom("anhkhoabuivu2004@gmail.com");

//            mailSender.send(mailMessage);
            log.info("Email sent to {}: {}", notificationEmail, message);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", notificationEmail, e.getMessage(), e);
        }
    }
}