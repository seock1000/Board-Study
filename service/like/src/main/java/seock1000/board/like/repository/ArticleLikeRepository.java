package seock1000.board.like.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import seock1000.board.like.entity.ArticleLike;

import java.util.Optional;

public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {
    Optional<ArticleLike> findByArticleIdAndUserId(Long articleId, Long userId);
}
