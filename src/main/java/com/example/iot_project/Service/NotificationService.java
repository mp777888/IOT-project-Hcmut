package com.example.iot_project.Service;

import com.example.iot_project.DTO.Response.NotificationResponse;
import com.example.iot_project.Entity.Notification;
import com.example.iot_project.Repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;


    public void lowerBoundMessage(String message,String device) {
        log.info("Lower bound notification sent");
        try{
            notificationRepository.save(Notification.builder()
                .deviceName(device)
                .message(message)
                .timestamp(java.time.LocalDateTime.now())
                .build());
        }catch (Exception e){
            log.error("Error saving notification: {}", e.getMessage());
        }
    }

    public void upperBoundMessage(String message,String device) {
        log.info("Upper bound notification sent");
        try{
            notificationRepository.save(Notification.builder()
                .deviceName(device)
                .message(message)
                .timestamp(java.time.LocalDateTime.now())
                .build());
        }catch (Exception e){
            log.error("Error saving notification: {}", e.getMessage());
        }
    }

    public Page<NotificationResponse> getAllNotifications(int page,int size) {
        log.info("Fetching all notifications");
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findAll(pageable)
                .map(notification -> NotificationResponse.builder()
                        .name_device(notification.getDeviceName())
                        .message(notification.getMessage())
                        .timestamp(notification.getTimestamp())
                        .build());
    }
}
