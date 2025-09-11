package seock1000.board.view.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import seock1000.board.view.entity.ArticleViewCount;

public interface ArticleViewCountBackUpRepository extends JpaRepository<ArticleViewCount, Long> {

    // view count < :viewCount: 동시 요청에서 낮은 값으로 덮어쓰는 것 방어 코드
    @Query(
            value = "UPDATE article_view_count SET view_count = :viewCount " +
                    "WHERE article_id = :articleId AND view_count < :viewCount",
            nativeQuery = true
    )
    @Modifying
    int updateViewCount(
            @Param("articleId") Long articleId,
            @Param("viewCount") Long viewCount
    );
}
