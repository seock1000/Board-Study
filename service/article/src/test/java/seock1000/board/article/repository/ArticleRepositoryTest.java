package seock1000.board.article.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import seock1000.board.article.entity.Article;

import java.util.List;

@Slf4j
@SpringBootTest
public class ArticleRepositoryTest {
    @Autowired
    ArticleRepository articleRepository;

    @Test
    void findAllTest() {
        List<Article> articles = articleRepository.findAll(1L, 1499970L, 30L);
        log.info("articles.size = {}", articles.size());
        articles.forEach(article -> log.info("article = {}", article));
    }

    @Test
    void countTest() {
        Long count = articleRepository.count(1L, 10001L);
        log.info("count = {}", count);
    }

    @Test
    void findAllInfiniteScrollTest() {
        List<Article> articles = articleRepository.findAllInfiniteScroll(1L, 30L);
        log.info("articles.size = {}", articles.size());
        articles.forEach(article -> log.info("article = {}", article));

        Long lastArticleId = articles.getLast().getArticleId();
        articles = articleRepository.findAllInfiniteScroll(1L, 30L, lastArticleId);
        log.info("articles.size = {}", articles.size());
        articles.forEach(article -> log.info("article = {}", article));
    }
}
