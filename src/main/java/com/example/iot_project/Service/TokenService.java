package com.example.iot_project.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    public void invalidateToken(String jti, Date expiryDate) {
        if (jti == null) {
            log.warn("Attempted to invalidate token with null JTI");
            return;
        }

        long ttlMillis = expiryDate.getTime() - System.currentTimeMillis();
        if (ttlMillis > 0) {
            redisTemplate.opsForValue().set(
                    TOKEN_BLACKLIST_PREFIX + jti,
                    "revoked",
                    ttlMillis,
                    TimeUnit.MILLISECONDS
            );
            log.info("Token invalidated: {}", jti);
        } else {
            log.info("Token already expired, not adding to blacklist: {}", jti);
        }
    }

    public boolean isTokenInvalidated(String jti) {
        if (jti == null) return false;
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + jti)
        );
    }

    public void storeRefreshToken(String jti, long ttlSeconds) {
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + jti,
                "valid",
                ttlSeconds,
                TimeUnit.SECONDS
        );
        log.info("Stored refresh token with JTI: {}", jti);
    }

    public boolean isRefreshTokenValid(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(REFRESH_TOKEN_PREFIX + jti));
    }

    public void invalidateRefreshToken(String jti) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + jti);
        log.info("Invalidated refresh token with JTI: {}", jti);
    }
}
