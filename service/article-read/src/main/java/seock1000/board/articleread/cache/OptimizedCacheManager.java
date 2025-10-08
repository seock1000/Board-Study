package seock1000.board.articleread.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import seock1000.board.common.dataserializer.DataSerializer;

import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@Component
@RequiredArgsConstructor
public class OptimizedCacheManager {
    private final StringRedisTemplate redisTemplate;
    private final OptimizedCacheLockProvider optimizedCacheLockProvider;

    public static final String DELIMITER = "::";

    public Object process(String type, long ttlSeconds, Object[] args, Class<?> returnType,
                          OptimizedCacheOriginDataSupplier<?> originDataSupplier) throws Throwable {
        String key = generateKey(type, args);
        String cachedData = redisTemplate.opsForValue().get(key);
        // 캐시에 데이터가 없을 때
        if(cachedData == null) {
            // 캐시 갱신
            return refresh(originDataSupplier, key, ttlSeconds);
        }
        // 캐시에 데이터가 있을 때
        OptimizedCache optimizedCache = DataSerializer.deserialize(cachedData, OptimizedCache.class);
        // 캐시가 제대로 파싱되지 않았을 때
        if(optimizedCache == null) {
            // 캐시 갱신
            return refresh(originDataSupplier, key, ttlSeconds);
        }

        // 캐시가 만료되지 않았을 때
        if(!optimizedCache.isExpired()) {
            // 캐시 데이터 반환
            return optimizedCache.parseData(returnType);
        }

        // 논리적 캐시가 만료되었으며, 락을 획득하지 못했을 때
        if(!optimizedCacheLockProvider.lock(key)) {
            // 기존 캐시 데이터 반환
            return optimizedCache.parseData(returnType);
        }

        // 논리적 캐시가 만료되었으며, 락을 획득했을 때
        try {
            // 캐시 갱신
            return refresh(originDataSupplier, key, ttlSeconds);
        } finally {
            optimizedCacheLockProvider.unlock(key); // 락 해제
        }
    }

    /**
     * 캐시에 데이터가 없을 때,
     * 원본 데이터 조회 후 캐시에 데이터를 적재하고 반환
     */
    private Object refresh(OptimizedCacheOriginDataSupplier<?> originDataSupplier, String key, long ttlSeconds) throws Throwable {
        Object result = originDataSupplier.get(); // 원본 데이터

        OptimizedCacheTTL cacheTTL = OptimizedCacheTTL.of(ttlSeconds); // 논리적 TTL 설정
        OptimizedCache optimizedCache = OptimizedCache.of(result, cacheTTL.getLogicalTTL());// 캐시 생성

        redisTemplate.opsForValue()
                .set(
                        key,
                        DataSerializer.serialize(optimizedCache), // 캐시 데이터 직렬화
                        cacheTTL.getPhysicalTTL() // 물리적 TTL 설정
                );
        return result;
    }

    private String generateKey(String prefix, Object[] args) {
        return prefix + DELIMITER +
                Arrays.stream(args)
                        .map(String::valueOf)
                        .collect(joining(DELIMITER));
    }
}
