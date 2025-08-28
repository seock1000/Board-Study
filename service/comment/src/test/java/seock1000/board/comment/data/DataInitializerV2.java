package seock1000.board.comment.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;
import seock1000.board.comment.entity.Comment;
import seock1000.board.comment.entity.CommentPath;
import seock1000.board.comment.entity.CommentV2;
import seock1000.board.common.snowflake.Snowflake;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

@SpringBootTest
public class DataInitializerV2 {
    @PersistenceContext
    EntityManager em;
    @Autowired
    TransactionTemplate transactionTemplate;
    Snowflake snowflake = new Snowflake();
    CountDownLatch latch = new CountDownLatch(EXECUTE_COUNT);

    static final int BULK_INSERT_SIZE = 2000;
    static final int EXECUTE_COUNT = 6000;

    @Test
    void initialize() throws InterruptedException {
        ExecutorService executorService = java.util.concurrent.Executors.newFixedThreadPool(10);
        for (int i = 0; i < EXECUTE_COUNT; i++) {
            int start = i * BULK_INSERT_SIZE;
            int end = (i + 1) * BULK_INSERT_SIZE;
            executorService.submit(() -> {
                insert(start, end);
                latch.countDown();
                System.out.println("latch.countDown() = " + latch.getCount());
            });
        }
        latch.await();
        executorService.shutdown();
    }

    // 중복이 되지 않는 범위 사용
    void insert(int start, int end) {
        transactionTemplate.executeWithoutResult(status -> {
            for (int i = start; i < end; i++) {
                CommentV2 comment = CommentV2.create(
                        snowflake.nextId(),
                        "content" + i,
                        toPath(i),
                        1L,
                        1L
                );
                em.persist(comment);
            }
        });
    }

    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int DEPTH_CHUNK_SIZE = 5;

    private CommentPath toPath(int value) {
        // 10진수를 62진수로 변환
        String path = "";
        for(int i = 0; i < DEPTH_CHUNK_SIZE; i++) {
            path = CHARSET.charAt(value % CHARSET.length()) + path;
            value /= CHARSET.length();
        }
        return CommentPath.create(path);
    }
}
