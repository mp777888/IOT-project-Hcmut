package com.example.iot_project.Repository;

import com.example.iot_project.Entity.FeedData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FeedDataRepository extends MongoRepository<FeedData, String> {
    // Tìm theo feedName và sắp xếp theo timestamp giảm dần
    List<FeedData> findByFeedNameOrderByTimestampDesc(String feedName);
    // Tìm theo feedName và khoảng thời gian
    List<FeedData> findByFeedNameAndTimestampBetween(String feedName, LocalDateTime startTime, LocalDateTime endTime);
    // Tìm theo khoảng thời gian
    List<FeedData> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);
}