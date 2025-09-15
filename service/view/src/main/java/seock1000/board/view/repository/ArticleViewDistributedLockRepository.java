package seock1000.board.view.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class ArticleViewDistributedLockRepository {

    private final StringRedisTemplate redisTemplate;

    // view::article::{article_id}::user::{user_id}::lock
    private final static String KEY_FORMAT = "view::article::%s::user::%s::lock";

    public boolean lock(Long articleId, Long userId, Duration ttl) {
        String key = generateKey(articleId, userId);
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "", ttl);
        return success != null && success;
    }

    private String generateKey(Long articleId, Long userId) {
        return String.format(KEY_FORMAT, articleId, userId);
    }


}
