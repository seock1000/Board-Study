package seock1000.board.like.api;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import seock1000.board.common.snowflake.Snowflake;
import seock1000.board.like.LikeApplication;
import seock1000.board.like.entity.ArticleLikeCount;
import seock1000.board.like.repository.ArticleLikeCountRepository;
import seock1000.board.like.repository.ArticleLikeRepository;
import seock1000.board.like.service.response.ArticleLikeResponse;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(
        classes = LikeApplication.class,
        webEnvironment = RANDOM_PORT
)
@Slf4j
public class LikeApiTest {
    @Autowired
    TestRestTemplate client;
    Snowflake snowflake = new Snowflake();

    @Autowired
    ArticleLikeCountRepository articleLikeCountRepository;
    @Autowired
    ArticleLikeRepository articleLikeRepository;

    private static final long TEST_ARTICLE_ID = 1L;

    @BeforeEach
    void setUp() {
        articleLikeCountRepository.save(
                ArticleLikeCount.init(TEST_ARTICLE_ID, 0L)
        );
    }

    @AfterEach
    void tearDown() {
        articleLikeRepository.deleteAll();
        articleLikeCountRepository.deleteAll();
    }

    @Test
    void likeAndRead() {
        Long articleId = 9999L;
        Long userId = 1L;
        String uri = String.format(
                "/v1/article-likes/articles/%d/users/%d",
                articleId, 1L
        );
        client.postForObject(
                uri,
                null,
                Void.class
        );
        ArticleLikeResponse response = client.getForObject(
                uri,
                ArticleLikeResponse.class
        );
        assertThat(response).isNotNull();
        assertThat(response.getArticleId()).isEqualTo(articleId);
        assertThat(response.getUserId()).isEqualTo(userId);
    }

    @Test
    void unlikeAndRead() {
        Long articleId = 9998L;
        Long userId = 1L;
        String uri = String.format(
                "/v1/article-likes/articles/%d/users/%d",
                articleId, userId
        );
        client.postForObject(
                uri,
                null,
                Void.class
        );
        client.delete(uri);
        ArticleLikeResponse response = client.getForObject(
                uri,
                ArticleLikeResponse.class
        );
        assertThat(response).isNotNull();
        assertThat(response.getArticleLikeId()).isNull();
        assertThat(response.getArticleId()).isNull();
        assertThat(response.getUserId()).isNull();
        assertThat(response.getCreatedAt()).isNull();
    }
    
    @Test
    void likePessimistic1ConcurrencyTest() {
        //given
        var articleId = TEST_ARTICLE_ID;
        var likes = like(articleId, "pessimistic1");
        //when
        Long start = System.currentTimeMillis();
        CompletableFuture.allOf(likes.toArray(new CompletableFuture[0])).join();
        Long end = System.currentTimeMillis();
        //then
        String countUri = String.format(
                "/v1/article-likes/articles/%d",
                articleId
        );
        var likeCount = client.getForEntity(
                countUri,
                Long.class
        );
        assertThat(likeCount.getBody().longValue()).isEqualTo(1000L);
        log.info("likes count in {} ms", end - start);
    }

    @Test
    void likePessimistic2ConcurrencyTest() {
        //given
        var articleId = TEST_ARTICLE_ID;
        var likes = like(articleId, "pessimistic2");
        //when
        Long start = System.currentTimeMillis();
        CompletableFuture.allOf(likes.toArray(new CompletableFuture[0])).join();
        Long end = System.currentTimeMillis();
        //then
        String countUri = String.format(
                "/v1/article-likes/articles/%d",
                articleId
        );
        var likeCount = client.getForEntity(
                countUri,
                Long.class
        );
        assertThat(likeCount.getBody().longValue()).isEqualTo(1000L);
        log.info("likes count in {} ms", end - start);
    }

    @Test
    void likeOptimisticConcurrencyTest() {
        //given
        var articleId = TEST_ARTICLE_ID;
        var likes = like(articleId, "optimistic");
        //when
        Long start = System.currentTimeMillis();
        CompletableFuture.allOf(likes.toArray(new CompletableFuture[0])).join();
        Long end = System.currentTimeMillis();
        //then
        String countUri = String.format(
                "/v1/article-likes/articles/%d",
                articleId
        );
        var likeCount = client.getForEntity(
                countUri,
                Long.class
        );
        assertThat(likeCount.getBody().longValue()).isEqualTo(1000L);
        log.info("likes count in {} ms", end - start);
    }

    List<CompletableFuture<Void>> like(Long articleId, String type) {
        switch (type) {
            case "pessimistic1" -> {
                return Arrays.stream(new CompletableFuture[1000])
                        .map(i -> CompletableFuture.runAsync(() -> {
                            String uri = String.format(
                                    "/v1/article-likes/articles/%d/users/%d/pessimistic-lock-1",
                                    articleId, snowflake.nextId()
                            );
                            crateRequestFuture(uri);
                        }))
                        .toList();
            }
            case "pessimistic2" -> {
                return Arrays.stream(new CompletableFuture[1000])
                        .map(i -> CompletableFuture.runAsync(() -> {
                            String uri = String.format(
                                    "/v1/article-likes/articles/%d/users/%d/pessimistic-lock-2",
                                    articleId, snowflake.nextId()
                            );
                            crateRequestFuture(uri);
                        }))
                        .toList();
            }
            case "optimistic" -> {
                return Arrays.stream(new CompletableFuture[1000])
                        .map(i -> CompletableFuture.runAsync(() -> {
                            String uri = String.format(
                                    "/v1/article-likes/articles/%d/users/%d/optimistic-lock",
                                    articleId, snowflake.nextId()
                            );
                            crateRequestFuture(uri);
                        }))
                        .toList();
            }
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    void crateRequestFuture(String uri) {
        client.postForObject(
                uri,
                null,
                Void.class
        );
    }
}
