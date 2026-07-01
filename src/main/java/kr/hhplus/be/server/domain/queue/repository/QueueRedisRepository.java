package kr.hhplus.be.server.domain.queue.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class QueueRedisRepository implements QueueRepository {

    private final StringRedisTemplate redisTemplate;

    // ZSET (member=userId, score=현재시각 밀리초)
    private static final String WAITING_KEY = "queue:waiting";
    // UUID 토큰
    private static final String TOKEN_KEY_PREFIX = "queue:token:";

    @Override
    public boolean isUserInQueue(Long userId) {
        return redisTemplate.opsForZSet()
            .score(WAITING_KEY, userId.toString()) != null;
    }

    @Override
    public long addToQueue(Long userId) {
        double score = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(WAITING_KEY, userId.toString(), score);
        Long rank = redisTemplate.opsForZSet().rank(WAITING_KEY, userId.toString());
        return rank != null ? rank : 0L;
    }

    @Override
    public Long getRank(Long userId) {
        return redisTemplate.opsForZSet().rank(WAITING_KEY, userId.toString());
    }

    @Override
    public String getToken(Long userId) {
        return redisTemplate.opsForValue().get(TOKEN_KEY_PREFIX + userId);
    }

    @Override
    public void storeToken(Long userId, String token) {
        redisTemplate.opsForValue().set(TOKEN_KEY_PREFIX + userId, token);
    }
}
