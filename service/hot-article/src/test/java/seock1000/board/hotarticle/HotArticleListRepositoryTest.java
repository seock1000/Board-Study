package seock1000.board.hotarticle;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HotArticleListRepositoryTest {
    @Autowired
    HotArticleListRepository hotArticleListRepository;

    @Test
    void add() throws InterruptedException {
        //given
        LocalDateTime time = LocalDateTime.now();
        long limit = 3;
        List<Long> expected = List.of(5L, 4L, 3L);

        //when
        hotArticleListRepository.add(1L, time, 100L, limit, Duration.ofSeconds(3));
        hotArticleListRepository.add(2L, time, 200L, limit, Duration.ofSeconds(3));
        hotArticleListRepository.add(3L, time, 300L, limit, Duration.ofSeconds(3));
        hotArticleListRepository.add(4L, time, 400L, limit, Duration.ofSeconds(3));
        hotArticleListRepository.add(5L, time, 500L, limit, Duration.ofSeconds(3));

        //then
        List<Long> hotArticleIds = hotArticleListRepository.readAll(DateTimeFormatter.ofPattern("yyyyMMdd").format(time));
        assertIterableEquals(hotArticleIds, expected);

        TimeUnit.SECONDS.sleep(4);

        assertThat(hotArticleListRepository.readAll(DateTimeFormatter.ofPattern("yyyyMMdd").format(time))).isEmpty();
    }

}