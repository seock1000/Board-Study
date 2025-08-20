package seock1000.board.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import seock1000.board.article.entity.Article;

public interface ArticleRepository extends JpaRepository<Article, Long> {
}
