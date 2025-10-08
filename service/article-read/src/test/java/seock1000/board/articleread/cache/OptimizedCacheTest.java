package seock1000.board.articleread.cache;

import lombok.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class OptimizedCacheTest {

    @Test
    void parseDataTest() {
        parseDataTest("data", 10);
        parseDataTest(3, 10);
        parseDataTest(3L, 10);
        parseDataTest(new TestClass("data"), 10);
    }

    void parseDataTest(Object data, long ttlSeconds) {
        //given
        OptimizedCache cache = OptimizedCache.of(data, Duration.ofSeconds(ttlSeconds));
        System.out.println("optimizedCache = " + cache);

        //when
        Object resolvedData = cache.parseData(data.getClass());

        //then
        System.out.println("resolvedData = " + resolvedData);
        assertThat(data).isEqualTo(resolvedData);
    }

    @Test
    void isExpiredTest() {
        assertThat(OptimizedCache.of(new TestClass("data"), Duration.ofDays(-30)).isExpired()).isTrue(); // 만료
        assertThat(OptimizedCache.of(new TestClass("data"), Duration.ofDays(+30)).isExpired()).isFalse(); // 만료 안됨
    }

    @Getter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestClass {
        String testData;
    }
}