package seock1000.board.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seock1000.board.comment.entity.CommentPath;
import seock1000.board.comment.entity.CommentV2;
import seock1000.board.comment.repository.CommentRepositoryV2;
import seock1000.board.comment.service.request.CommentCreateRequestV2;
import seock1000.board.comment.service.response.CommentPageResponse;
import seock1000.board.comment.service.response.CommentResponse;
import seock1000.board.common.snowflake.Snowflake;

import java.util.List;

import static java.util.function.Predicate.not;
import static seock1000.board.comment.service.PageLimitCalculator.calculatePageLimit;

@Service
@RequiredArgsConstructor
public class CommentServiceV2 {
    private final CommentRepositoryV2 commentRepositoryV2;
    private final Snowflake snowflake = new Snowflake();

    @Transactional
    public CommentResponse create(CommentCreateRequestV2 request) {
        CommentV2 parent = findParent(request);
        CommentPath parentPath = parent == null ? CommentPath.create("") : parent.getCommentPath();
        CommentV2 comment = commentRepositoryV2.save(
                CommentV2.create(
                        snowflake.nextId(),
                        request.getContent(),
                        parentPath.createChildCommentPath(
                                commentRepositoryV2.findDescendantTopPath(request.getArticleId(), parentPath.getPath())
                                        .orElse(null)
                        ),
                        request.getArticleId(),
                        request.getWriterId()
                )
        );
        return CommentResponse.from(comment);
    }

    private CommentV2 findParent(CommentCreateRequestV2 request) {
        String parentPath = request.getParentPath();
        if (parentPath == null) {
            return null;
        }
        return commentRepositoryV2.findByPath(parentPath)
                .filter(not(CommentV2::getDeleted))
                .orElse(null);
    }

    public CommentResponse read(Long commentId) {
        return CommentResponse.from(
                commentRepositoryV2.findById(commentId)
                .orElseThrow()
        );
    }

    @Transactional
    public void delete(Long commentId) {
        commentRepositoryV2.findById(commentId)
                .filter(not(CommentV2::getDeleted))
                .ifPresent(comment -> {
                    if(hasChildren(comment)) {
                        comment.delete();
                    } else {
                        delete(comment);
                    }
                });
    }

    private boolean hasChildren(CommentV2 comment) {
        return commentRepositoryV2.findDescendantTopPath(
                        comment.getArticleId(),
                        comment.getCommentPath().getPath()
                ).isPresent();
    }

    private void delete(CommentV2 comment) {
        commentRepositoryV2.delete(comment);
        if(!comment.isRoot()) {
            commentRepositoryV2.findByPath(comment.getCommentPath().getParentPath())
                    .filter(CommentV2::getDeleted)
                    .filter(not(this::hasChildren))
                    .ifPresent(this::delete);
        }
    }

    public CommentPageResponse readAll(Long articleId, Long page, Long pageSize) {
        return CommentPageResponse.of(
                commentRepositoryV2.findAll(articleId, (page - 1) * pageSize, pageSize).stream()
                        .map(CommentResponse::from)
                        .toList(),
                commentRepositoryV2.count(articleId, calculatePageLimit(page, pageSize, 10L))
        );
    }

    public List<CommentResponse> readAllInfiniteScroll(Long articleId, String lastPath, Long pageSize) {
        List<CommentV2> comments = (lastPath == null)
                ? commentRepositoryV2.findAllInfiniteScroll(articleId, pageSize)
                : commentRepositoryV2.findAllInfiniteScroll(articleId, lastPath, pageSize);

        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }
}
