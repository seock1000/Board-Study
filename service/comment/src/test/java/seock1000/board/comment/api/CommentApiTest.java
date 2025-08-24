package seock1000.board.comment.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
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

    @Test
    void readAll() {
        Long articleId = 1L;
        Long page = 1L;
        Long pageSize = 30L;
        String uri = String.format(
                "/v1/comments?articleId=%d&page=1&pageSize=%d",
                articleId, pageSize
        );

        CommentPageResponse response = client.getForObject(
                uri,
                CommentPageResponse.class
        );

        for(CommentResponse comment : response.getComments()) {
            if(!comment.getParentCommentId().equals(comment.getCommentId())) {
                System.out.print("\t");
            }
            System.out.println("commentId = " + comment.getCommentId());
        }

        /**
         * commentId = 217869197782437888
         * 	commentId = 217869200223522816
         * 	commentId = 217869200361934848
         * commentId = 217869432349433856
         * commentId = 217869678923526144
         * commentId = 217869880726556672
         * commentId = 217870523979567104
         * 	commentId = 217870524973617152
         * commentId = 217871909263458304
         * 	commentId = 217871909343150089
         * commentId = 217871909263458305
         * 	commentId = 217871909343150081
         * commentId = 217871909263458306
         * 	commentId = 217871909343150082
         * commentId = 217871909263458307
         * 	commentId = 217871909343150086
         * commentId = 217871909263458308
         * 	commentId = 217871909343150080
         * commentId = 217871909263458309
         * 	commentId = 217871909343150085
         * commentId = 217871909263458310
         * 	commentId = 217871909343150084
         * commentId = 217871909263458311
         * 	commentId = 217871909343150088
         * commentId = 217871909263458312
         * 	commentId = 217871909343150083
         * commentId = 217871909263458313
         * 	commentId = 217871909343150087
         * commentId = 217871909343150090
         * 	commentId = 217871909343150101
         */
    }

    @Test
    void infiniteScroll() {
        Long articleId = 1L;
        Long pageSize = 30L;
        String uri = String.format(
                "/v1/comments/infinite-scroll?articleId=%d&pageSize=%d",
                articleId, pageSize
        );
        List<CommentResponse> response = List.of(client.getForObject(
                uri,
                CommentResponse[].class
        ));
        CommentResponse last = response.getLast();
        List<CommentResponse> second = List.of(client.getForObject(
                String.format(
                        "/v1/comments/infinite-scroll?articleId=%d&pageSize=%d&lastParentCommentId=%d&lastCommentId=%d",
                        articleId, pageSize, last.getParentCommentId(), last.getCommentId()
                ),
                CommentResponse[].class
        ));
        System.out.println("========================= FIRST PAGE =========================");
        for(CommentResponse comment : response) {
            if(!comment.getParentCommentId().equals(comment.getCommentId())) {
                System.out.print("\t");
            }
            System.out.println("commentId = " + comment.getCommentId());
        }
        System.out.println("========================= SECOND PAGE =========================");
        for(CommentResponse comment : second) {
            if (!comment.getParentCommentId().equals(comment.getCommentId())) {
                System.out.print("\t");
            }
            System.out.println("commentId = " + comment.getCommentId());
        }
    }

    @Test
    void infiniteScrollWithLastIds() {
        Long articleId = 1L;
        Long pageSize = 30L;
        Long lastParentCommentId = 28L;
        Long lastCommentId = 30L;
        String uri = String.format(
                "/v1/comments/infinite-scroll?articleId=%d&pageSize=%d&lastParentCommentId=%d&lastCommentId=%d",
                articleId, pageSize, lastParentCommentId, lastCommentId
        );
        CommentResponse[] response = client.getForObject(
                uri,
                CommentResponse[].class
        );
        for(CommentResponse comment : response) {
            if(!comment.getParentCommentId().equals(comment.getCommentId())) {
                System.out.print("\t");
            }
            System.out.println("commentId = " + comment.getCommentId());
        }
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
