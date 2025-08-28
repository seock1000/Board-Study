package seock1000.board.comment.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
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

    @Getter
    @AllArgsConstructor
    public static class CommentCreateRequestV2 {
        Long articleId;
        String content;
        String parentPath;
        Long writerId;
    }
}
