package com.example.iot_project.Repository;

import com.example.iot_project.Entity.FeedData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FeedDataRepository extends MongoRepository<FeedData, String> {
    List<FeedData> findByFeedNameOrderByTimestampDesc(String feedName);
    List<FeedData> findByFeedNameAndTimestampBetween(String feedName, long startTime, long endTime);
    List<FeedData> findByTimestampBetween(long startTime, long endTime);
    List<String> findDistinctFeedNameBy();
}