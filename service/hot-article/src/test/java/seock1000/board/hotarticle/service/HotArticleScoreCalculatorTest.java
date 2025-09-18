package seock1000.board.hotarticle.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seock1000.board.hotarticle.repository.ArticleCommentCountRepository;
import seock1000.board.hotarticle.repository.ArticleLikeCountRepository;
import seock1000.board.hotarticle.repository.ArticleViewCountRepository;

import java.util.random.RandomGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotArticleScoreCalculatorTest {
    @InjectMocks
    HotArticleScoreCalculator hotArticleScoreCalculator;
    @Mock
    ArticleLikeCountRepository articleLikeCountRepository;
    @Mock
    ArticleViewCountRepository articleViewCountRepository;
    @Mock
    ArticleCommentCountRepository articleCommentCountRepository;

    @Test
    void calculateTest() {
        //given
        Long articleId = 1L;
        Long likeCount = RandomGenerator.getDefault().nextLong(100L); // 100 이하의 랜덤 값
        Long viewCount = RandomGenerator.getDefault().nextLong(100L);
        Long commentCount = RandomGenerator.getDefault().nextLong(100L);

        when(articleLikeCountRepository.read(articleId)).thenReturn(likeCount);
        when(articleViewCountRepository.read(articleId)).thenReturn(viewCount);
        when(articleCommentCountRepository.read(articleId)).thenReturn(commentCount);
        long expectedScore = (viewCount * 1) + (commentCount * 2) + (likeCount * 3);

        //when
        long score = hotArticleScoreCalculator.calculate(articleId);

        //then
        assertThat(score).isEqualTo(expectedScore);
    }

}