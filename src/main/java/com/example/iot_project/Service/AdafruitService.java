package com.example.iot_project.Service;

import com.example.iot_project.Entity.SensorData;
import com.example.iot_project.Repository.SensorDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdafruitService {

    private final SensorDataRepository sensorDataRepository;
    private final MessageChannel mqttOutboundChannel;

    @Value("${adafruit.io.username}")
    private String username;

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<?> message) {
        try {
            String topic = message.getHeaders().get("mqtt_receivedTopic").toString();
            String payload = message.getPayload().toString();
            String feedName = topic.split("/")[2];

            // Xây dựng đối tượng SensorData với trường numericValue nếu có thể parse
            SensorData.SensorDataBuilder builder = SensorData.builder()
                    .feedName(feedName)
                    .value(payload)
                    .timestamp(System.currentTimeMillis());

            // Thử chuyển đổi payload thành số nếu có thể
            try {
                Double numericValue = Double.parseDouble(payload);
                builder.numericValue(numericValue);
            } catch (NumberFormatException e) {
                // Nếu không chuyển đổi được, để numericValue là null
                log.debug("Payload không phải số: {}", payload);
            }

            SensorData sensorData = builder.build();
            sensorDataRepository.save(sensorData);

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

    // Thêm các phương thức tiện ích để truy vấn dữ liệu
    public List<SensorData> getLatestDataForFeed(String feedName) {
        return sensorDataRepository.findByFeedNameOrderByTimestampDesc(feedName);
    }

    public List<SensorData> getDataByTimeRange(String feedName, long startTime, long endTime) {
        return sensorDataRepository.findByFeedNameAndTimestampBetween(feedName, startTime, endTime);
    }
}