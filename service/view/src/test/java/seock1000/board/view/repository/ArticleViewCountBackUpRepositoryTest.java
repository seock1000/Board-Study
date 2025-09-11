package seock1000.board.view.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import seock1000.board.view.ViewApplication;
import seock1000.board.view.entity.ArticleViewCount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest
class ArticleViewCountBackUpRepositoryTest {
    @Autowired
    private ArticleViewCountBackUpRepository articleViewCountBackUpRepository;
    @PersistenceContext
    private EntityManager em;

    @Test
    @Transactional
    void updateViewCountBackUp() {
        //given
        articleViewCountBackUpRepository.save(
                ArticleViewCount.init(1L, 0L)
        );
        em.flush();
        em.clear();

        //when
        int result1 = articleViewCountBackUpRepository.updateViewCount(1L, 10L);
        int result2 = articleViewCountBackUpRepository.updateViewCount(1L, 20L);
        int result3 = articleViewCountBackUpRepository.updateViewCount(1L, 15L);

        //then
        assertThat(result1).isEqualTo(1);
        assertThat(result2).isEqualTo(1);
        assertThat(result3).isEqualTo(0);
        ArticleViewCount articleViewCount = articleViewCountBackUpRepository.findById(1L).orElseThrow();
        assertThat(articleViewCount.getViewCount()).isEqualTo(20L);
    }
}