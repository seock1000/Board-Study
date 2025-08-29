package seock1000.board.like.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import seock1000.board.like.LikeApplication;
import seock1000.board.like.service.response.ArticleLikeResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(
        classes = LikeApplication.class,
        webEnvironment = RANDOM_PORT
)
public class LikeApiTest {
    @Autowired
    TestRestTemplate client;

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
}
