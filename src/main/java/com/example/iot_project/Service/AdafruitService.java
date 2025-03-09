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
            SensorData sensorData = SensorData.builder()
                    .feedName(feedName)
                    .value(payload)
                    .timestamp(System.currentTimeMillis())
                    .build();
            sensorDataRepository.save(sensorData);
            System.out.println("Saved to MongoDB - Feed: " + feedName + ", Value: " + payload);
        } catch (Exception e) {
            System.out.println("Error handling message: " + e.getMessage());
        }
    }

    // Gửi dữ liệu đến Adafruit IO (để điều khiển YOLO Bit)
    public void publishToFeed(String feedName, String value) {
        String topic = username + "/feeds/" + feedName;
        mqttOutboundChannel.send(MessageBuilder.withPayload(value)
                .setHeader("mqtt_topic", topic)
                .build());
        System.out.println("Published to Adafruit IO - Topic: " + topic + ", Value: " + value);
    }
}