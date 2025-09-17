package seock1000.board.hotarticle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class HotArticleListRepository {
    private final StringRedisTemplate redisTemplate;

    // hot-article::list::{yyyyMMdd}
    private static final String KEY_FORMAT = "hot-article::list::%s";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public void add(Long articleId, LocalDateTime time, Long score, Long limit, Duration ttl) {
        // executePipelined: 여러 Redis 명령을 하나의 네트워크 요청으로 묶어서 처리
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            // RedisConnection을 StringRedisConnection으로 캐스팅
            StringRedisConnection conn = (StringRedisConnection) action;
            String key = generateKey(time);
            // ZADD 명령어로 정렬된 집합에 항목 추가
            conn.zAdd(key, score, String.valueOf(articleId));
            conn.zRemRange(key, 0, -limit - 1); // limit 초과된 항목 제거
            conn.expire(key, ttl.getSeconds()); // 키의 TTL 설정
            return null;
        });
    }

    private String generateKey(LocalDateTime time) {
        return generateKey(time.format(TIME_FORMATTER));
    }

    private String generateKey(String dateStr) {
        return KEY_FORMAT.formatted(dateStr);
    }

    public List<Long> readAll(String dateStr) {
        String key = generateKey(dateStr);
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1).stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .map(Long::valueOf)
                .toList();
    }
}
