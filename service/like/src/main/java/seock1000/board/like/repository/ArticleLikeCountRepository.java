package seock1000.board.like.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seock1000.board.like.entity.ArticleLikeCount;

import java.util.Optional;

public interface ArticleLikeCountRepository extends JpaRepository<ArticleLikeCount, Long> {

    // 비관적 락 ... for update
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ArticleLikeCount> findLockedByArticleId(Long articleId);

    // 비관적 락 방식 증가 쿼리
    @Query(
            value = "UPDATE article_like_count SET like_count = like_count + 1 WHERE article_id = :articleId",
            nativeQuery = true
    )
    @Modifying // update 쿼리에서 필요
    int increase(@Param("articleId") Long articleId);

    // 비관적 락 방식 감소 쿼리
    @Query(
            value = "UPDATE article_like_count SET like_count = like_count - 1 WHERE article_id = :articleId",
            nativeQuery = true
    )
    @Modifying // update 쿼리에서 필요
    int decrease(@Param("articleId") Long articleId);
}
