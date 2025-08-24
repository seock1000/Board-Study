package seock1000.board.comment.service.request;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CommentCreateRequest {
    Long articleId;
    String content;
    Long parentCommentId;
    Long writerId;
}
