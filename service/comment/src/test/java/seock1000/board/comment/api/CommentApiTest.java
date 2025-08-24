package seock1000.board.comment.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import seock1000.board.comment.CommentApplication;
import seock1000.board.comment.service.response.CommentResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(
        classes = CommentApplication.class,
        webEnvironment = RANDOM_PORT
)
public class CommentApiTest {
    @Autowired
    TestRestTemplate client;

    @Test
    void create() {
        CommentResponse response1 = createComment(new CommentCreateRequest(1L, "my content1", null, 1L));
        CommentResponse response2 = createComment(new CommentCreateRequest(1L, "my content2", response1.getCommentId(), 1L));
        CommentResponse response3 = createComment(new CommentCreateRequest(1L, "my content3", response1.getCommentId(), 1L));

        assertThat(response1).isNotNull();
        assertThat(response1.getContent()).isEqualTo("my content1");
        assertThat(response1.getParentCommentId()).isEqualTo(response1.getCommentId());
        assertThat(response1.getArticleId()).isEqualTo(1L);

        assertThat(response2).isNotNull();
        assertThat(response2.getContent()).isEqualTo("my content2");
        assertThat(response2.getParentCommentId()).isEqualTo(response1.getCommentId());
        assertThat(response2.getArticleId()).isEqualTo(1L);

        assertThat(response3).isNotNull();
        assertThat(response3.getContent()).isEqualTo("my content3");
        assertThat(response3.getParentCommentId()).isEqualTo(response1.getCommentId());
        assertThat(response3.getArticleId()).isEqualTo(1L);
    }

    CommentResponse createComment(CommentCreateRequest request) {
        return client.postForObject(
                "/v1/comments",
                request,
                CommentResponse.class
        );
    }

    @Test
    void read() {
        CommentResponse createResponse = createComment(new CommentCreateRequest(1L, "my content", null, 1L));
        Long commentId = createResponse.getCommentId();
        CommentResponse response = client.getForObject(
                "/v1/comments/{commentId}",
                CommentResponse.class,
                commentId
        );
        assertThat(response).isNotNull();
        assertThat(response.getCommentId()).isEqualTo(commentId);
        assertThat(response.getParentCommentId()).isEqualTo(response.getCommentId());
        assertThat(response.getContent()).isEqualTo("my content");
        assertThat(response.getArticleId()).isEqualTo(1L);
    }

    @Test
    void delete() {
        CommentResponse createResponse = createComment(new CommentCreateRequest(1L, "my content", null, 1L));
        Long commentId = createResponse.getCommentId();
        client.delete("/v1/comments/{commentId}", commentId);
    }

    @Getter
    @AllArgsConstructor
    public static class CommentCreateRequest {
        Long articleId;
        String content;
        Long parentCommentId;
        Long writerId;
    }
}
