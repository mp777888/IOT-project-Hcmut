package com.example.iot_project.Repository;

import com.example.iot_project.Entity.InvalidToken;
import com.example.iot_project.Entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidatedTokenRepository extends MongoRepository<InvalidToken,String> {
}
