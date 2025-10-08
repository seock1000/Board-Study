package seock1000.board.articleread.cache;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class OptimizedCacheTTLTest {

    @Test
    void ofTest() {
        //given
        long ttlSeconds = 10L;

        //when
        OptimizedCacheTTL cacheTTL = OptimizedCacheTTL.of(ttlSeconds);

        //then
        assertThat(cacheTTL.getLogicalTTL()).isEqualTo(Duration.ofSeconds(ttlSeconds));
        assertThat(cacheTTL.getPhysicalTTL()).isBetween(Duration.ofSeconds(ttlSeconds), Duration.ofSeconds(ttlSeconds).plusSeconds(OptimizedCacheTTL.PHYSICAL_TTL_DELAY_SECONDS));
    }

}