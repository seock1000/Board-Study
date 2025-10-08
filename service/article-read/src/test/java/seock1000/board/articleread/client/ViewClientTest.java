package seock1000.board.articleread.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class ViewClientTest {
    @Autowired
    ViewClient viewClient;

    @Test
    void readCacheableTest() throws InterruptedException {
        viewClient.count(1L); // 로그 출력
        viewClient.count(1L); // 로그 미출력
        viewClient.count(1L); // 로그 미출력

        TimeUnit.SECONDS.sleep(1);
        viewClient.count(1L); // 로그 출력
    }

    /**
     * cache 동시성 테스트
     * ===== cache expired =====
     * 2025-10-08T16:18:43.715+09:00  INFO 20774 --- [seock1000-board-article-read-service] [pool-2-thread-4] s.board.articleread.client.ViewClient    : [ViewClient.count] articleId=1
     * 2025-10-08T16:18:43.715+09:00  INFO 20774 --- [seock1000-board-article-read-service] [pool-2-thread-1] s.board.articleread.client.ViewClient    : [ViewClient.count] articleId=1
     * 2025-10-08T16:18:43.715+09:00  INFO 20774 --- [seock1000-board-article-read-service] [pool-2-thread-2] s.board.articleread.client.ViewClient    : [ViewClient.count] articleId=1
     * 2025-10-08T16:18:43.715+09:00  INFO 20774 --- [seock1000-board-article-read-service] [pool-2-thread-5] s.board.articleread.client.ViewClient    : [ViewClient.count] articleId=1
     * 2025-10-08T16:18:43.715+09:00  INFO 20774 --- [seock1000-board-article-read-service] [pool-2-thread-3] s.board.articleread.client.ViewClient    : [ViewClient.count] articleId=1
     * ===== cache expired =====
     * 2025-10-08T16:18:45.730+09:00  INFO 20774 --- [seock1000-board-article-read-service] [pool-2-thread-3] s.board.articleread.client.ViewClient    : [ViewClient.count] articleId=1
     * 2025-10-08T16:18:45.730+09:00  INFO 20774 --- [seock1000-board-article-read-service] [pool-2-thread-5] s.board.articleread.client.ViewClient    : [ViewClient.count] articleId=1
     * 2025-10-08T16:18:45.730+09:00  INFO 20774 --- [seock1000-board-article-read-service] [pool-2-thread-4] s.board.articleread.client.ViewClient    : [ViewClient.count] articleId=1
     * 2025-10-08T16:18:45.730+09:00  INFO 20774 --- [seock1000-board-article-read-service] [pool-2-thread-1] s.board.articleread.client.ViewClient    : [ViewClient.count] articleId=1
     * 2025-10-08T16:18:45.731+09:00  INFO 20774 --- [seock1000-board-article-read-service] [pool-2-thread-2] s.board.articleread.client.ViewClient    : [ViewClient.count] articleId=1
     * ===== cache expired =====
     * 2025-10-08T16:18:47.743+09:00  INFO 20774 --- [seock1000-board-article-read-service] [pool-2-thread-1] s.board.articleread.client.ViewClient    : [ViewClient.count] articleId=1
     * 2025-10-08T16:18:47.743+09:00  INFO 20774 --- [seock1000-board-article-read-service] [pool-2-thread-3] s.board.articleread.client.ViewClient    : [ViewClient.count] articleId=1
     * 2025-10-08T16:18:47.743+09:00  INFO 20774 --- [seock1000-board-article-read-service] [pool-2-thread-4] s.board.articleread.client.ViewClient    : [ViewClient.count] articleId=1
     * 2025-10-08T16:18:47.743+09:00  INFO 20774 --- [seock1000-board-article-read-service] [pool-2-thread-5] s.board.articleread.client.ViewClient    : [ViewClient.count] articleId=1
     * 2025-10-08T16:18:47.743+09:00  INFO 20774 --- [seock1000-board-article-read-service] [pool-2-thread-2] s.board.articleread.client.ViewClient    : [ViewClient.count] articleId=1
     *
     * 동시 요청이 발생할 때, cache put 이전에 cache miss가 발생하여 불필요하게 여러 번 fetch 되는 현상 확인
     *
     * 해결방법
     * 분산락 전략
     * - 캐시 갱신에 lock을 적용
     * - 캐시 갱신이 길어지면 갱신될 때까지 무한히 대기할 가능성 존재
     * Logical TTL & Physical TTL 혼합 전략
     * - 갱신을 위한 만료시각과 실제 만료시간을 다르게 적용
     * - Logical TTL이 만료되면 갱신을 시도하되, Physical TTL이 만료되기 전까지는 기존 캐시를 사용
     * - ex) Logical TTL 5초, Physical TTL 10초
     * - 갱신 처리 전까지 과거 데이터가 일시적으로 노출될 가능성 존재
     *   - 캐시 일관성이 엄격하지 않은 경우에 적용 가능, ex. 조회수
     *   - 원본 데이터 처리가 무거운 경우
     * - 무의미한 중복 요청 트래픽을 줄이는 이점 존재
     * - Request Collapsing : 여러 개의 동일하거나 유사한 요청을 하나의 요청으로 합쳐서 처리하는 기법
     *
     * 캐시 최적화 적용 이후
     * 2025-10-08T17:53:33.947+09:00  INFO 25943 --- [seock1000-board-article-read-service] [    Test worker] s.board.articleread.client.ViewClient    : [ViewClient.count] articleId=1
     * ===== cache expired =====
     * 2025-10-08T17:53:36.423+09:00  INFO 25943 --- [seock1000-board-article-read-service] [pool-2-thread-1] s.board.articleread.client.ViewClient    : [ViewClient.count] articleId=1
     * ===== cache expired =====
     * 2025-10-08T17:53:38.453+09:00  INFO 25943 --- [seock1000-board-article-read-service] [pool-2-thread-4] s.board.articleread.client.ViewClient    : [ViewClient.count] articleId=1
     * ===== cache expired =====
     * 2025-10-08T17:53:40.470+09:00  INFO 25943 --- [seock1000-board-article-read-service] [pool-2-thread-2] s.board.articleread.client.ViewClient    : [ViewClient.count] articleId=1
     * ===== cache expired =====
     * 2025-10-08T17:53:42.489+09:00  INFO 25943 --- [seock1000-board-article-read-service] [pool-2-thread-1] s.board.articleread.client.ViewClient    : [ViewClient.count] articleId=1
     * ===== cache expired =====
     */
    @Test
    void readCacheableMultiThreadTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        viewClient.count(1L); // init cache

        for(int i = 0; i < 5; i++) {
            CountDownLatch latch = new CountDownLatch(5);
            for(int j = 0; j < 5; j++) {
                executorService.submit(() -> {
                    viewClient.count(1L);
                    latch.countDown();
                });
            }
            latch.await();
            TimeUnit.SECONDS.sleep(2);
            System.out.println("===== cache expired =====");
        }

    }


}