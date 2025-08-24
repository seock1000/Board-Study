package seock1000.board.comment.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seock1000.board.comment.entity.Comment;
import seock1000.board.comment.repository.CommentRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @InjectMocks
    private CommentService commentService;
    @Mock
    private CommentRepository commentRepository;

    @Test
    @DisplayName("삭제할 댓글에 자식이 있으면 삭제표시만 한다")
    void delete_withChildren() {
        //given
        Long articleId = 1L;
        Long commentId = 2L;
        Comment comment = createComment(articleId, commentId);
        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));
        given(commentRepository.countBy(articleId, commentId, 2L)).willReturn(2L); // 자식이 있는 경우

        //when
        commentService.delete(commentId);

        //then
        verify(comment).delete();
    }

    @Test
    @DisplayName("하위 댓글이 삭제되고 삭제되지 않은 부모이면 하위 댓글만 삭제한다")
    void delete_children_parentNotDeleted() {
        //given
        Long articleId = 1L;
        Long commentId = 2L;
        Long parentCommentId = 1L;

        Comment comment = createComment(articleId, commentId, parentCommentId);
        given(comment.isRoot()).willReturn(false);

        Comment parentComment = mock(Comment.class);
        given(parentComment.getDeleted()).willReturn(false);

        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));
        given(commentRepository.countBy(articleId, commentId, 2L)).willReturn(1L); // 자식이 없는 경우

        given(commentRepository.findById(parentCommentId))
                .willReturn(Optional.of(parentComment));

        //when
        commentService.delete(commentId);

        //then
        verify(commentRepository).delete(comment);
        verify(parentComment, never()).delete();
    }

    @Test
    @DisplayName("하위 댓글이 삭제되고 삭제된 부모이면 부모까지 삭제한다")
    void delete_children_parentDeleted() {
        //given
        Long articleId = 1L;
        Long commentId = 2L;
        Long parentCommentId = 1L;

        Comment comment = createComment(articleId, commentId, parentCommentId);
        given(comment.isRoot()).willReturn(false);

        Comment parentComment = createComment(articleId, parentCommentId);
        given(parentComment.isRoot()).willReturn(true);
        given(parentComment.getDeleted()).willReturn(true);

        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));
        given(commentRepository.countBy(articleId, commentId, 2L)).willReturn(1L); // 자식이 없는 경우

        given(commentRepository.findById(parentCommentId))
                .willReturn(Optional.of(parentComment));
        given(commentRepository.countBy(articleId, parentCommentId, 2L)).willReturn(1L); // 자식이 없는 경우

        //when
        commentService.delete(commentId);

        //then
        verify(commentRepository).delete(comment);
        verify(commentRepository).delete(parentComment);
    }

    private Comment createComment(Long articleId, Long commentId) {
        Comment comment = mock(Comment.class);
        given(comment.getArticleId()).willReturn(articleId);
        given(comment.getCommentId()).willReturn(commentId);
        return comment;
    }

    private Comment createComment(Long articleId, Long commentId, Long parentCommentId) {
        Comment comment = mock(Comment.class);
        given(comment.getArticleId()).willReturn(articleId);
        given(comment.getCommentId()).willReturn(commentId);
        given(comment.getParentCommentId()).willReturn(parentCommentId);
        return comment;
    }
}