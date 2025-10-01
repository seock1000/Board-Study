package seock1000.board.articleread.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ArticleListRepository {
    private final StringRedisTemplate redisTemplate;

    // article-read::board::{boardId}::article-list
    private static final String KEY_FORMAT = "article-read::board::%s::article-list";

    public void add(Long boardId, Long articleId, Long limit) {
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection conn = (StringRedisConnection) action;
            String key = generateKey(boardId);
            // score 0으로 고정, articleId를 20자리 0으로 패딩된 문자열로 저장
            // score는 double 타입이므로 Long 타입을 그대로 사용 시 유실 가능성이 있음
            // 동일한 socre일 때 value를 통해 정렬되므로, articleId를 20자리 0으로 패딩된 문자열로 저장
            conn.zAdd(key, 0, toPaddedString(articleId));
            conn.zRemRange(key, 0, - limit - 1); // limit 초과된 항목 제거
            return null;
        });
    }

    public void delete(Long boardId, Long articleId) {
        redisTemplate.opsForZSet().remove(generateKey(boardId), toPaddedString(articleId));
    }

    public List<Long> readAll(Long boardId, Long offset, Long limit) {
        String key = generateKey(boardId);
        // ZREVRANGE 명령어로 정렬된 집합에서 offset부터 limit개 항목을 내림차순으로 조회
        return redisTemplate.opsForZSet().reverseRange(key, offset, offset + limit - 1).stream()
                .map(Long::valueOf)
                .toList();
    }

    public List<Long> readAllInfiniteScroll(Long boardId, Long lastArticleId, Long limit) {
        String key = generateKey(boardId);
        return redisTemplate.opsForZSet().reverseRangeByLex(
                key,
                lastArticleId == null ?
                        Range.unbounded() :
                        // lastArticleId의 왼쪽 범위는 제외
                        Range.leftUnbounded(Range.Bound.exclusive(toPaddedString(lastArticleId))),
                Limit.limit().count(limit.intValue())
                ).stream().map(Long::valueOf).toList();
    }

    /**
     * Long값으로 받은 articleId를 20자리의 0으로 고정된 길이 문자열로 변환
     * ex) 1 -> 00000000000000000001, 123 -> 00000000000000000123
     */
    private String toPaddedString(Long articleId) {
        return String.format("%020d", articleId);
    }

    private String generateKey(Long boardId) {
        return KEY_FORMAT.formatted(boardId);
    }
}
