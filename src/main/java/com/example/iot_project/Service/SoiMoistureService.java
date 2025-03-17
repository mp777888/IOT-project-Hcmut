package com.example.iot_project.Service;

import com.example.iot_project.Repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SoiMoistureService {

    private final DeviceRepository deviceRepository;



}