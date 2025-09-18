package seock1000.board.hotarticle.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimeCalculatorUtilsTest {

    @Test
    void calculateDurationToMidnight() {
        //when
        var duration = TimeCalculatorUtils.calculateDurationToMidnight();

        //then
        assertNotNull(duration);
        assertTrue(duration.toHours() <= 24);
        assertTrue(duration.toHours() >= 0);
    }

}