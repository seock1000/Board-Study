package seock1000.board.articleread.api;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import seock1000.board.articleread.service.response.ArticleReadResponse;

public class ArticleReadApiTest {
    RestClient restClient = RestClient.create("http://localhost:9005");

    /**
     * 최신 데이터를 조회하는 경우에는 cache 조회 타 서비스 fetch 미수행
     * 이전 데이터를 조회하는 경우에는 fetch 발생
     */
    @Test
    void readTest() {
        ArticleReadResponse body = restClient.get()
                .uri("v1/articles/{articleId}", 231389917402669056L)
                .retrieve()
                .body(ArticleReadResponse.class);
        System.out.println("body = " + body);
    }
}
