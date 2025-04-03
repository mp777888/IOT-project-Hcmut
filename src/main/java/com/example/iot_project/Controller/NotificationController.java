package com.example.iot_project.Controller;

import com.example.iot_project.DTO.Response.NotificationResponse;
import com.example.iot_project.Exception.ApiResponse;
import com.example.iot_project.Service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/all")
    public ApiResponse<Page<NotificationResponse>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching all notifications");
        return ApiResponse.<Page<NotificationResponse>>builder()
                .code(200)
                .result(notificationService.getAllNotifications(page, size))
                .build();
    }

}
