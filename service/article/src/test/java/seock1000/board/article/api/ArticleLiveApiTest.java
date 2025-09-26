package seock1000.board.article.api;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import seock1000.board.article.service.response.ArticleResponse;

public class ArticleLiveApiTest {

    RestClient restClient = RestClient.create("http://localhost:9000");

    @Test
    void createLiveAppCallTest() {
        ArticleResponse response = restClient.post()
                .uri("/v1/articles")
                .body(new ArticleApiTest.ArticleCreateRequest(
                        "hi", "content", 1L, 1L
                ))
                .retrieve()
                .body(ArticleResponse.class);
    }
}
