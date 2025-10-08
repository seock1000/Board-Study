package seock1000.board.articleread.api;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import seock1000.board.articleread.service.response.ArticleReadPageResponse;
import seock1000.board.articleread.service.response.ArticleReadResponse;

import java.util.List;

public class ArticleReadApiTest {
    RestClient articleReadRestClient = RestClient.create("http://localhost:9005");
    RestClient articleRestClient = RestClient.create("http://localhost:9000");

    /**
     * 최신 데이터를 조회하는 경우에는 cache 조회 타 서비스 fetch 미수행
     * 이전 데이터를 조회하는 경우에는 fetch 발생
     */
    @Test
    void readTest() {
        ArticleReadResponse body = articleReadRestClient.get()
                .uri("v1/articles/{articleId}", 231389917402669056L)
                .retrieve()
                .body(ArticleReadResponse.class);
        System.out.println("body = " + body);
    }

    @Test
    void readAllTest() {
        ArticleReadPageResponse response1 = articleReadRestClient.get()
                .uri("v1/articles?boardId=%s&page=%s&pageSize=%s".formatted(1, 1, 5))
                .retrieve()
                .body(ArticleReadPageResponse.class);

        System.out.println("response1.getArticleCount() = " + response1.getArticleCount());
        for(ArticleReadResponse article : response1.getArticles()) {
            System.out.println("article.getArticleId() = " + article.getArticleId());
        }

        ArticleReadPageResponse response2 = articleRestClient.get()
                .uri("v1/articles?boardId=%s&page=%s&pageSize=%s".formatted(1, 1, 5))
                .retrieve()
                .body(ArticleReadPageResponse.class);

        System.out.println("response2.getArticleCount() = " + response2.getArticleCount());
        for(ArticleReadResponse article : response2.getArticles()) {
            System.out.println("article.getArticleId() = " + article.getArticleId());
        }
    }

    @Test
    void readAllInfiniteScrollTest() {
        // 최신
        var response1 = articleReadRestClient.get()
                .uri("v1/articles/infinite-scroll?boardId=%s&pageSize=%s".formatted(1, 5))
                .retrieve()
                .body(new ParameterizedTypeReference<List<ArticleReadResponse>>() {
                });

        for(ArticleReadResponse article : response1) {
            System.out.println("article.getArticleId() = " + article.getArticleId());
        }

        // 이전
        var response2 = articleRestClient.get()
                .uri("v1/articles/infinite-scroll?boardId=%s&pageSize=%s".formatted(1, 5))
                .retrieve()
                .body(new ParameterizedTypeReference<List<ArticleReadResponse>>() {
                });

        for(ArticleReadResponse article : response2) {
            System.out.println("article.getArticleId() = " + article.getArticleId());
        }
    }
}
