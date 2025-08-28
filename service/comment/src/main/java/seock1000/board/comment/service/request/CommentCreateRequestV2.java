package seock1000.board.comment.service.request;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CommentCreateRequestV2 {
    Long articleId;
    String content;
    String parentPath;
    Long writerId;
}
