package seock1000.board.article.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import seock1000.board.article.entity.BoardArticleCount;

import java.util.Optional;

public interface BoardArticleCountRepository extends JpaRepository<BoardArticleCount, Long> {

    // 비관적 락 ... for update
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BoardArticleCount> findLockedByBoardId(Long boardId);

    // 비관적 락 방식 증가 쿼리
    @Query(
            value = "UPDATE board_article_count SET article_count = article_count + 1 WHERE board_id = :boardId",
            nativeQuery = true
    )
    @Modifying
    // update 쿼리에서 필요
    int increase(@Param("boardId") Long boardId);

    // 비관적 락 방식 감소 쿼리
    @Query(
            value = "UPDATE board_article_count SET article_count = article_count - 1 WHERE board_id = :boardId",
            nativeQuery = true
    )
    @Modifying // update 쿼리에서 필요
    int decrease(@Param("boardId") Long boardId);
}
