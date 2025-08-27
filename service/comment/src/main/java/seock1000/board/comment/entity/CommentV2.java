package seock1000.board.comment.entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Table(name = "comment_v2")
@Getter
@Entity
@ToString
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class CommentV2 {
    @Id
    private Long commentId;
    private String content;
    @Embedded
    private CommentPath commentPath;
    private Long articleId; // shardKey
    private Long writerId;
    private Boolean deleted;
    private LocalDateTime createdAt;
}
