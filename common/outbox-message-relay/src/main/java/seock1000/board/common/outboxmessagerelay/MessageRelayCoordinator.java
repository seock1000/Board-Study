package seock1000.board.common.outboxmessagerelay;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class MessageRelayCoordinator {
    private final StringRedisTemplate redisTemplate;

    @Value("${spring.application.name}")
    private String applicationName;

    private final String APP_ID = UUID.randomUUID().toString();

    private static final int PING_INTERVAL_SECONDS = 3;
    private static final int PING_FAILURE_THRESHOLD = 3;

    public AssignedShard assignedShard() {
        return AssignedShard.of(APP_ID, findAppIds(), MessageRelayConstants.SHARD_COUNT);
    }

    private List<String> findAppIds() {
        //ZSet에서 범위 0부터 -1까지(모든 값) 역순으로 조회
        return redisTemplate.opsForZSet().reverseRange(generateKey(), 0, -1).stream()
                .sorted() // 살아있는 application 항상 동일 순서로 유지
                .toList();
    }

    /**
     * 주기적으로 Redis에 PING 전송하여 자신이 살아있음을 알림
     * 죽은 어플리케이션은 coordinator 목록에서 제거
     */
    @Scheduled(fixedDelay = PING_INTERVAL_SECONDS, timeUnit = TimeUnit.SECONDS)
    public void ping() {
        // executePipelined: 여러 Redis 명령을 하나의 네트워크 요청으로 묶어서 처리
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection conn = (StringRedisConnection) action;
            String key = generateKey();
            // ZADD 명령어로 정렬된 집합에 현재 시간으로 appId 추가 또는 갱신
            conn.zAdd(key, Instant.now().toEpochMilli(), APP_ID);
            // PING_FAILURE_THRESHOLD * PING_INTERVAL_SECONDS 초 이상 응답 없는 appId 죽은 것으로 판단하고 제거
            conn.zRemRangeByScore(
                    key,
                    Double.NEGATIVE_INFINITY,
                    Instant.now().minusSeconds(PING_INTERVAL_SECONDS * PING_FAILURE_THRESHOLD).toEpochMilli()
            );
            return null;
        });
    }

    @PreDestroy
    public void leave() {
        // 어플리케이션 종료 시 coordinator 목록에서 자신의 ID 제거
        redisTemplate.opsForZSet().remove(generateKey(), APP_ID);
    }

    /**
     * 각 마이크로 서비스에서 독립적인 키로 동작하도록 설계
     */
    private String generateKey() {
        return "message-relay-coordinator::app-list::%s".formatted(applicationName);
    }
}
