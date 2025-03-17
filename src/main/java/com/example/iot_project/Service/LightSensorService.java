package com.example.iot_project.Service;

import com.example.iot_project.Entity.DHT20Sensor;
import com.example.iot_project.Entity.Device;
import com.example.iot_project.Enum.DeviceType;
import com.example.iot_project.Repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LightSensorService {

    private final DeviceRepository deviceRepository;

}