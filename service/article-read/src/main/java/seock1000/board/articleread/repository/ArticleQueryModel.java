package seock1000.board.articleread.repository;

import lombok.Getter;
import seock1000.board.articleread.client.ArticleClient;
import seock1000.board.common.event.payload.*;

import java.time.LocalDateTime;

@Getter
public class ArticleQueryModel {
    private Long articleId;
    private String title;
    private String content;
    private Long boardId;
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long articleCommentCount;
    private Long articleLikeCount;

    // 게시글 생성 이벤트로부터 쿼리 모델 생성
    public static ArticleQueryModel create(ArticleCreatedEventPayload payload) {
        ArticleQueryModel model = new ArticleQueryModel();
        model.articleId = payload.getArticleId();
        model.title = payload.getTitle();
        model.content = payload.getContent();
        model.boardId = payload.getBoardId();
        model.writerId = payload.getWriterId();
        model.createdAt = payload.getCreatedAt();
        model.updatedAt = payload.getModifiedAt();
        model.articleCommentCount = 0L; // 초기 댓글 수는 0
        model.articleLikeCount = 0L; // 초기 좋아요 수는 0
        return model;
    }

    // 캐시 미스로 Article 서비스에서 직접 조회한 데이터로부터 쿼리 모델 생성
    public static ArticleQueryModel create(ArticleClient.ArticleResponse article, Long commentCount, Long likeCount) {
        ArticleQueryModel model = new ArticleQueryModel();
        model.articleId = article.getArticleId();
        model.title = article.getTitle();
        model.content = article.getContent();
        model.boardId = article.getBoardId();
        model.writerId = article.getWriterId();
        model.createdAt = article.getCreatedAt();
        model.updatedAt = article.getUpdatedAt();
        model.articleCommentCount = commentCount;
        model.articleLikeCount = likeCount;
        return model;
    }

    public void updateBy(CommentCreatedEventPayload payload) {
        this.articleCommentCount = payload.getArticleCommentCount();
    }

    public void updateBy(CommentDeletedEventPayload payload) {
        this.articleCommentCount = payload.getArticleCommentCount();
    }

    public void updateBy(ArticleLikedEventPayload payload) {
        this.articleLikeCount = payload.getArticleLikeCount();
    }

    public void updateBy(ArticleUnlikedEventPayload payload) {
        this.articleLikeCount = payload.getArticleLikeCount();
    }

    public void updateBy(ArticleUpdatedEventPayload payload) {
        this.title = payload.getTitle();
        this.content = payload.getContent();
        this.boardId = payload.getBoardId();
        this.writerId = payload.getWriterId();
        this.createdAt = payload.getCreatedAt();
        this.updatedAt = payload.getModifiedAt();
    }
}
