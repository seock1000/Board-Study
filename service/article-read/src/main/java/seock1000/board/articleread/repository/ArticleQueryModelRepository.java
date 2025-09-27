package seock1000.board.articleread.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import seock1000.board.common.dataserializer.DataSerializer;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ArticleQueryModelRepository {
    private final StringRedisTemplate redisTemplate;
    //article-read::article::{articleId}
    private static final String KEY_FORMAT = "article-read::article::%s";


    public void create(ArticleQueryModel articleQueryModel, Duration ttl) {
        redisTemplate.opsForValue()
                .set(generateKey(articleQueryModel), DataSerializer.serialize(articleQueryModel), ttl);
    }

    // ttl은 생성 시에만 설정
    public void update(ArticleQueryModel articleQueryModel) {
        // 값이 존재할 때만 업데이트
        redisTemplate.opsForValue()
                .setIfPresent(generateKey(articleQueryModel), DataSerializer.serialize(articleQueryModel));
    }

    public void delete(Long articleId) {
        redisTemplate.delete(generateKey(articleId));
    }

    public Optional<ArticleQueryModel> read(Long articleId) {
        //
        return Optional.ofNullable(
                redisTemplate.opsForValue().get(generateKey(articleId))
        ).map(json -> DataSerializer.deserialize(json, ArticleQueryModel.class));
    }

    private String generateKey(ArticleQueryModel articleQueryModel) {
        return generateKey(articleQueryModel.getArticleId());
    }

    private String generateKey(Long articleId) {
        return KEY_FORMAT.formatted(articleId);
    }
}
