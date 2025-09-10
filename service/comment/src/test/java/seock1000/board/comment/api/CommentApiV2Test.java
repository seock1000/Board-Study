package seock1000.board.comment.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import seock1000.board.comment.CommentApplication;
import seock1000.board.comment.service.response.CommentPageResponse;
import seock1000.board.comment.service.response.CommentResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(
        classes = CommentApplication.class,
        webEnvironment = RANDOM_PORT
)
public class CommentApiV2Test {
    @Autowired
    TestRestTemplate client;

    @Test
    void create() {
        CommentResponse response1 = createComment(new CommentApiV2Test.CommentCreateRequestV2(1L, "my content1", null, 1L));
        CommentResponse response2 = createComment(new CommentApiV2Test.CommentCreateRequestV2(1L, "my content2", response1.getPath(), 1L));
        CommentResponse response3 = createComment(new CommentApiV2Test.CommentCreateRequestV2(1L, "my content3", response2.getPath(), 1L));

        assertThat(response1).isNotNull();
        assertThat(response1.getContent()).isEqualTo("my content1");
        assertThat(response1.getArticleId()).isEqualTo(1L);

        assertThat(response2).isNotNull();
        assertThat(response2.getContent()).isEqualTo("my content2");
        assertThat(response2.getArticleId()).isEqualTo(1L);

        assertThat(response3).isNotNull();
        assertThat(response3.getContent()).isEqualTo("my content3");
        assertThat(response3.getArticleId()).isEqualTo(1L);

        System.out.println("Path 1: " + response1.getPath());
        System.out.println("Path 2: " + response2.getPath());
        System.out.println("Path 3: " + response3.getPath());
    }

    CommentResponse createComment(CommentApiV2Test.CommentCreateRequestV2 request) {
        return client.postForObject(
                "/v2/comments",
                request,
                CommentResponse.class
        );
    }

    @Test
    void read() {
        CommentResponse createResponse = createComment(new CommentApiV2Test.CommentCreateRequestV2(1L, "my content", null, 1L));
        CommentResponse response = client.getForObject(
                "/v2/comments/{commentId}",
                CommentResponse.class,
                createResponse.getCommentId()
        );
        assertThat(response).isNotNull();
        assertThat(response.getCommentId()).isEqualTo(createResponse.getCommentId());
        assertThat(response.getPath()).isEqualTo(createResponse.getPath());
        assertThat(response.getContent()).isEqualTo("my content");
        assertThat(response.getArticleId()).isEqualTo(1L);
    }

    @Test
    void readAll() {
        Long articleId = 1L;
        Long page = 1L;
        Long pageSize = 30L;
        String uri = String.format(
                "/v2/comments?articleId=%d&page=%d&pageSize=%d",
                articleId, page, pageSize
        );

        CommentPageResponse response = client.getForObject(
                uri,
                CommentPageResponse.class
        );

        for(CommentResponse comment : response.getComments()) {
            System.out.println("commentId = " + comment.getCommentId());
        }

        /**
         * commentId = 219440878138609665
         * commentId = 219440878247661574
         * commentId = 219440878247661587
         * commentId = 219440878247661596
         * commentId = 219440878247661606
         * commentId = 219440878251855878
         * commentId = 219440878251855888
         * commentId = 219440878251855898
         * commentId = 219440878251855907
         * commentId = 219440878251855916
         * commentId = 219440878256050185
         * commentId = 219440878256050193
         * commentId = 219440878256050203
         * commentId = 219440878256050215
         * commentId = 219440878260244481
         * commentId = 219440878260244489
         * commentId = 219440878260244504
         * commentId = 219440878260244514
         * commentId = 219440878264438792
         * commentId = 219440878264438802
         * commentId = 219440878264438812
         * commentId = 219440878264438821
         * commentId = 219440878264438831
         * commentId = 219440878268633094
         * commentId = 219440878268633103
         * commentId = 219440878268633112
         * commentId = 219440878268633123
         * commentId = 219440878268633130
         * commentId = 219440878272827392
         * commentId = 219440878272827401
         */
    }

    @Test
    void infiniteScroll() {
        Long articleId = 1L;
        Long pageSize = 30L;
        String uri = String.format(
                "/v2/comments/infinite-scroll?articleId=%d&pageSize=%d",
                articleId, pageSize
        );
        List<CommentResponse> response = List.of(client.getForObject(
                uri,
                CommentResponse[].class
        ));
        CommentResponse last = response.getLast();
        List<CommentResponse> second = List.of(client.getForObject(
                String.format(
                        "/v2/comments/infinite-scroll?articleId=%d&pageSize=%d&lastPath=%s",
                        articleId, pageSize, last.getPath()
                ),
                CommentResponse[].class
        ));
        System.out.println("========================= FIRST PAGE =========================");
        for(CommentResponse comment : response) {
            System.out.println("commentId = " + comment.getCommentId());
        }
        System.out.println("========================= SECOND PAGE =========================");
        for(CommentResponse comment : second) {
            System.out.println("commentId = " + comment.getCommentId());
        }
    }

    @Test
    void count() {
        Long articleId = 2L;
        var response = client.getForObject(
                "/v2/comments/articles/" + articleId + "/count",
                Long.class
        );
        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(0L);
    }

    @Test
    void countIncrease() {
        Long articleId = 1L;
        var before = client.getForObject(
                "/v2/comments/articles/{articleId}/count",
                Long.class,
                articleId
        );
        createComment(new CommentApiV2Test.CommentCreateRequestV2(1L, "my content1", null, 1L));
        var after = client.getForObject(
                "/v2/comments/articles/{articleId}/count",
                Long.class,
                articleId
        );
        assertThat(after.longValue()).isEqualTo(before.longValue() + 1L);
    }

    @Test
    void countDecrease() {
        Long articleId = 1L;
        CommentResponse response = createComment(new CommentApiV2Test.CommentCreateRequestV2(1L, "my content1", null, 1L));
        var before = client.getForObject(
                "/v2/comments/articles/{articleId}/count",
                Long.class,
                articleId
        );
        client.delete(
                "/v2/comments/{commentId}",
                response.getCommentId()
        );
        var after = client.getForObject(
                "/v2/comments/articles/{articleId}/count",
                Long.class,
                articleId
        );
        assertThat(after.longValue()).isEqualTo(before.longValue() - 1L);
    }

    @Getter
    @AllArgsConstructor
    public static class CommentCreateRequestV2 {
        Long articleId;
        String content;
        String parentPath;
        Long writerId;
    }
}
