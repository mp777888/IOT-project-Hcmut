package com.example.iot_project.Configuration;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
@Slf4j
public class MqttConfig {

    @Value("${adafruit.io.username}")
    private String username;

    @Value("${adafruit.io.key}")
    private String key;

    @Value("${adafruit.io.mqtt.host}")
    private String host;

    @Value("${adafruit.io.mqtt.clientId}")
    private String clientId;

    @Value("${adafruit.io.list_feeds}")
    private String[] feeds;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{host});
        options.setUserName(username);
        options.setPassword(key.toCharArray());
        options.setCleanSession(true);
        options.setKeepAliveInterval(30); // Kiểm tra kết nối mỗi 30 giây
        options.setAutomaticReconnect(true); // Tự động kết nối lại khi mất kết nối
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        // Chuyển đổi tên feeds thành topics đầy đủ (thêm username/feeds/ ở đầu)
        String[] topics = new String[feeds.length];
        for (int i = 0; i < feeds.length; i++) {
            topics[i] = username + "/feeds/" + feeds[i];
        }

        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(clientId + "_inbound", mqttClientFactory(), topics);
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1); // Đảm bảo tin nhắn được gửi ít nhất một lần
        adapter.setOutputChannel(mqttInputChannel());

        log.info("Đã cấu hình MQTT inbound adapter với các topics: {}", String.join(", ", topics));
        return adapter;
    }

    // Chú ý: Xóa bean handler() vì nó trùng với phương thức handleMessage() trong AdafruitService
    // Nếu giữ lại cả hai, sẽ có hai handler xử lý cùng một tin nhắn

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(clientId + "_outbound", mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultQos(1);
        messageHandler.setDefaultRetained(false);
        return messageHandler;
    }
}