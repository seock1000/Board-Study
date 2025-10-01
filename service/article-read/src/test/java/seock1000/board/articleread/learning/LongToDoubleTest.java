package seock1000.board.articleread.learning;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class LongToDoubleTest {

    @Test
    void longToDoubleTest() {
        // long 64비트 정수값 표현, double 64비트 부동소수점 표현
        // 표현 방식이 다르기 때문에 데이터 유실 발생
        long longValue = 111_111_111_111_111_111L;
        System.out.println("longValue = " + longValue);

        double doubleValue = longValue;
        System.out.println("doubleValue = " + new BigDecimal(doubleValue).toString());

        long longValue2 = (long) doubleValue;
        System.out.println("longValue2 = " + new BigDecimal(doubleValue).toString());
    }
}
