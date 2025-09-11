package seock1000.board.view.api;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.client.RestTemplate;
import seock1000.board.view.ViewApplication;
import seock1000.board.view.repository.ArticleViewCountBackUpRepository;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(
        classes = ViewApplication.class,
        webEnvironment = RANDOM_PORT
)
@Slf4j
public class ViewApiTest {
    @Autowired
    TestRestTemplate client;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ArticleViewCountBackUpRepository articleViewCountBackUpRepository;

    private static final String KEY_FORMAT = "view::article::%s::view_count";

    @Test
    void viewTest() {
        //given
        Long articleId = 1L;
        Long userId = 1L;
        String key = String.format(KEY_FORMAT, articleId);
        redisTemplate.delete(key);
        String uri = String.format(
                "/v1/article-views/articles/%d/users/%d",
                articleId, userId
        );

        //when
        Long count = client.postForObject(
                uri,
                null,
                Long.class
        );
        //then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void concurrentIncreaseTest() {
        //given
        Long articleId = 1L;
        int requestCount = 10000;
        String key = String.format(KEY_FORMAT, articleId);
        redisTemplate.delete(key);
        String uriFormat = "/v1/article-views/articles/%d/users/%d";

        //when
        List<CompletableFuture<Long>> futures = Arrays.stream(new CompletableFuture[requestCount])
                .map((v) -> CompletableFuture.supplyAsync(() -> {
                    Long userId = (long) (Math.random() * 100);
                    return client.postForObject(
                            String.format(uriFormat, articleId, userId),
                            null,
                            Long.class
                    );
                }))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        //then
        Long count = client.getForObject(
                String.format("/v1/article-views/articles/%d/count", articleId),
                Long.class
        );
        Long backupCount = articleViewCountBackUpRepository.findById(articleId)
                .orElseThrow()
                .getViewCount();
        assertThat(count).isEqualTo(requestCount);
        assertThat(backupCount).isEqualTo(requestCount);
    }

}
