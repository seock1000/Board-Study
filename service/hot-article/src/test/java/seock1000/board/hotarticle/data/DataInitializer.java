package seock1000.board.hotarticle.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.random.RandomGenerator;

public class DataInitializer {
    RestClient articleServiceClient = RestClient.create("http://localhost:9000");
    RestClient commentServiceClient = RestClient.create("http://localhost:9001");
    RestClient likeServiceClient = RestClient.create("http://localhost:9002");
    RestClient viewServiceClient = RestClient.create("http://localhost:9003");

    @Test
    void initialize() {
        for(int i = 0; i < 30; i++) {
            Long articleId = createArticle();
            // 난수로 댓글, 좋아요, 조회수 개수 생성
            long commentCount = RandomGenerator.getDefault().nextLong(10);
            long likeCount = RandomGenerator.getDefault().nextLong(10);
            long viewCount = RandomGenerator.getDefault().nextLong(200);

            createComment(articleId, commentCount);
            createLike(articleId, likeCount);
            createView(articleId, viewCount);
        }
    }

    private void createView(Long articleId, long viewCount) {
        for(int i = 0; i < viewCount; i++) {
            viewServiceClient.post()
                    .uri("/v1/article-views/articles/{articleId}/users/{userId}", articleId, Long.valueOf(i))
                    .retrieve();
        }
    }

    private void createLike(Long articleId, long likeCount) {
        for(int i = 0; i < likeCount; i++) {
            likeServiceClient.post()
                    .uri("/v1/article-likes/articles/{articleId}/users/{userId}/pessimistic-lock-1", articleId, Long.valueOf(i))
                    .retrieve();
        }
    }

    private void createComment(Long articleId, long commentCount) {
        for(int i = 0; i < commentCount; i++) {
            commentServiceClient.post()
                    .uri("/v1/comments")
                    .body(new CommentCreateRequest(
                            articleId,
                            "content",
                            Long.valueOf(i)
                    ))
                    .retrieve();
        }
    }

    private Long createArticle() {
        return articleServiceClient.post()
                .uri("/v1/articles")
                .body(new ArticleCreateRequest(
                        "title",
                        "content",
                        1L,
                        1L
                ))
                .retrieve()
                .body(ArticleResponse.class)
                .getArticleId();
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
    static class ArticleResponse {
        private Long articleId;
    }

    @Getter
    @AllArgsConstructor
    static class CommentCreateRequest {
        private Long articleId;
        private String content;
        private Long writerId;
    }

    @Getter
    @AllArgsConstructor
    static class LikeCreateRequest {
        private Long articleId;
        private Long memberId;
    }
}
