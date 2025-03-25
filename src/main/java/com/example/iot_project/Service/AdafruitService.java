package com.example.iot_project.Service;

import com.example.iot_project.Entity.FeedData;
import com.example.iot_project.Enum.DeviceType;
import com.example.iot_project.Repository.FeedDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdafruitService {

    private final FeedDataRepository feedDataRepository;
    private final MessageChannel mqttOutboundChannel;
    private final ThresholdService thresholdService;

    @Value("${adafruit.io.username}")
    private String username;
    @Value("${adafruit.io.feeds.temperature}")
    private String temperatureFeed;
    @Value("${adafruit.io.feeds.humidity}")
    private String humidityFeed;
    @Value("${adafruit.io.feeds.soilMoisture}")
    private String soilMoistureFeed;
    @Value("${adafruit.io.feeds.light}")
    private String lightFeed;

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<?> message) {
        try {
            String topic = Objects.requireNonNull(message.getHeaders().get("mqtt_receivedTopic")).toString();
            String payload = message.getPayload().toString();
            String feedName = topic.split("/")[2];

            // Xây dựng đối tượng SensorData với trường numericValue nếu có thể parse
            FeedData.FeedDataBuilder builder = FeedData.builder()
                    .feedName(feedName)
                    .timestamp(LocalDateTime.now());

            // Thử chuyển đổi payload thành số nếu có thể
            Double numericValue = null;
            try {
                numericValue = Double.parseDouble(payload);
                builder.numericValue(numericValue);
            } catch (NumberFormatException e) {
                // Nếu không chuyển đổi được, để numericValue là null
                log.debug("Payload không phải số: {}", payload);
            }

            FeedData feedData = builder.build();
            feedDataRepository.save(feedData);

//            if (feedName.equals(temperatureFeed)) {
//                thresholdService.checkAndNotify(DeviceType.DHT20_TEMPERATURE, numericValue);
//            }
//            else if (feedName.equals(humidityFeed)) {
//                thresholdService.checkAndNotify(DeviceType.DHT20_HUMIDITY, numericValue);
//            }
//            else if (feedName.equals(soilMoistureFeed)) {
//                thresholdService.checkAndNotify(DeviceType.SOIL_MOISTURE, numericValue);
//            }
//            else if (feedName.equals(lightFeed)) {
//                thresholdService.checkAndNotify(DeviceType.LIGHT, numericValue);
//            }

            log.info("Đã lưu vào MongoDB - Feed: {}, Value: {}", feedName, payload);
        } catch (Exception e) {
            log.error("Lỗi xử lý tin nhắn MQTT: {}", e.getMessage(), e);
        }
    }

    // Gửi dữ liệu đến Adafruit IO (để điều khiển thiết bị)
    public void publishToFeed(String feedName, String value) {
        String topic = username + "/feeds/" + feedName;
        mqttOutboundChannel.send(MessageBuilder.withPayload(value)
                .setHeader("mqtt_topic", topic)
                .build());
        log.info("Đã gửi lên Adafruit IO - Topic: {}, Value: {}", topic, value);
    }

}