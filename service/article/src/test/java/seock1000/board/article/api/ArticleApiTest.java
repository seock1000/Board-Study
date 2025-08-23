package seock1000.board.article.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import seock1000.board.article.ArticleApplication;
import seock1000.board.article.service.response.ArticlePageResponse;
import seock1000.board.article.service.response.ArticleResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIterator;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.RequestEntity.put;

@SpringBootTest(
        classes = ArticleApplication.class,
        webEnvironment = RANDOM_PORT
)
public class ArticleApiTest {

    @Autowired
    TestRestTemplate client;

    @Test
    void createTest() {
        ArticleResponse response = create(new ArticleCreateRequest(
                "hi", "content", 1L, 1L
        ));
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("hi");
        assertThat(response.getContent()).isEqualTo("content");
        assertThat(response.getWriterId()).isEqualTo(1L);
        assertThat(response.getBoardId()).isEqualTo(1L);
    }

    ArticleResponse create(ArticleCreateRequest request) {
        return client.postForObject(
                "/v1/articles",
                request,
                ArticleResponse.class
        );
    }

    @Test
    void readTest() {
        ArticleResponse createResponse = create(new ArticleCreateRequest(
                "hi", "content", 1L, 1L
        ));
        Long articleId = createResponse.getArticleId();
        ArticleResponse response = read(articleId);
        assertThat(response).isNotNull();
        assertThat(response.getArticleId()).isEqualTo(articleId);
    }

    ArticleResponse read(Long articleId) {
        return client.getForObject(
                "/v1/articles/" + articleId,
                ArticleResponse.class
        );
    }

    @Test
    void updateTest() {
        ArticleResponse createResponse = create(new ArticleCreateRequest(
                "hi", "content", 1L, 1L
        ));
        Long articleId = createResponse.getArticleId();
        ArticleUpdateRequest updateRequest = new ArticleUpdateRequest(
                "updated title", "updated content"
        );
        ArticleResponse response = update(articleId, updateRequest);
        assertThat(response).isNotNull();
        assertThat(response.getArticleId()).isEqualTo(articleId);
        assertThat(response.getTitle()).isEqualTo("updated title");
        assertThat(response.getContent()).isEqualTo("updated content");
        assertThat(response.getWriterId()).isEqualTo(1L);
        assertThat(response.getBoardId()).isEqualTo(1L);
    }

    ArticleResponse update(Long articleId, ArticleUpdateRequest request) {
        return client.exchange(
                put("/v1/articles/" + articleId)
                        .body(request),
                ArticleResponse.class
        ).getBody();
    }

    @Test
    void deleteTest() {
        ArticleResponse createResponse = create(new ArticleCreateRequest(
                "hi", "content", 1L, 1L
        ));
        Long articleId = createResponse.getArticleId();
        client.delete("/v1/articles/" + articleId);

        //TODO ResponseEntity로 변경 후 수정
        ArticleResponse read = read(articleId);
        assertThat(read.getArticleId()).isNull();
    }

    @Test
    void delete(Long articleId) {
        client.delete("/v1/articles/" + articleId);
    }

    @Test
    void readAllTest() {
        Long boardId = 1L;
        Long page = 1L;
        Long pageSize = 30L;

        Long expectedCount = pageSize * page + 1;
        String uri = String.format(
                "/v1/articles?boardId=%d&page=%d&pageSize=%d",
                boardId, page, pageSize
        );

        ArticlePageResponse response = client.getForObject(
                uri,
                ArticlePageResponse.class
        );

        assertThat(response).isNotNull();
        assertThat(response.getArticles().size()).isLessThanOrEqualTo(pageSize.intValue());
        assertThat(response.getArticles().stream().allMatch(a -> a.getBoardId().equals(boardId))).isTrue();
        assertThat(response.getArticleCount()).isGreaterThanOrEqualTo(expectedCount);
    }

    @Test
    void readAllTest2() {
        Long boardId = 1L;
        Long page = 50000L;
        Long pageSize = 30L;

        Long expectedCount = pageSize * page + 1;
        String uri = String.format(
                "/v1/articles?boardId=%d&page=%d&pageSize=%d",
                boardId, page, pageSize
        );

        ArticlePageResponse response = client.getForObject(
                uri,
                ArticlePageResponse.class
        );

        assertThat(response).isNotNull();
        assertThat(response.getArticles().size()).isLessThanOrEqualTo(pageSize.intValue());
        assertThat(response.getArticleCount()).isGreaterThanOrEqualTo(expectedCount);
    }

    @Getter
    @AllArgsConstructor
    static class ArticleCreateRequest {
        private String title;
        private String content;
        private Long writerId;
        private Long boardId;
    }

    @Getter
    @AllArgsConstructor
    static class ArticleUpdateRequest {
        private String title;
        private String content;
    }
}
