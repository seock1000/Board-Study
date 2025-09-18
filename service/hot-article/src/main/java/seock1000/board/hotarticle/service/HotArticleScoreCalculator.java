package seock1000.board.hotarticle.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import seock1000.board.hotarticle.repository.ArticleCommentCountRepository;
import seock1000.board.hotarticle.repository.ArticleLikeCountRepository;
import seock1000.board.hotarticle.repository.ArticleViewCountRepository;

@Component
@RequiredArgsConstructor
public class HotArticleScoreCalculator {
    private final ArticleViewCountRepository articleViewCountRepository;
    private final ArticleLikeCountRepository articleLikeCountRepository;
    private final ArticleCommentCountRepository articleCommentCountRepository;

    private static final long ARTICLE_LIKE_WEIGHT = 3;
    private static final long ARTICLE_COMMENT_WEIGHT = 2;
    private static final long ARTICLE_VIEW_WEIGHT = 1;

    public long calculate(Long articleId) {
        long viewCount = articleViewCountRepository.read(articleId);
        long likeCount = articleLikeCountRepository.read(articleId);
        long commentCount = articleCommentCountRepository.read(articleId);

        // 가중치: 조회수(1), 좋아요(2), 댓글(3)
        return (viewCount * ARTICLE_VIEW_WEIGHT)
                + (likeCount * ARTICLE_LIKE_WEIGHT)
                + (commentCount * ARTICLE_COMMENT_WEIGHT);
    }
}
