package com.example.iot_project.Repository;

import com.example.iot_project.Enity.GreenHouseDevice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GreenHouseRepository extends MongoRepository<GreenHouseDevice, String> {
}
