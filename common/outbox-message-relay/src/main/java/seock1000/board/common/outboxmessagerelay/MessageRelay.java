package seock1000.board.common.outboxmessagerelay;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRelay {
    private final OutboxRepository outboxRepository;
    private final MessageRelayCoordinator messageRelayCoordinator;
    private final KafkaTemplate<String, String> messageRelayKafkaTemplate;

    // 트랜잭션 커밋 직전에 outbox 테이블에 저장 -> 비즈니스 로직과 동일한 트랜잭션으로 처리
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void createOutbox(OutboxEvent outboxEvent) {
        log.info("[MessageRelay.createOutbox] outboxEvent={}", outboxEvent);
        outboxRepository.save(outboxEvent.getOutbox());
    }

    // 트랜잭션 커밋 후 kafka에 비동기로 이벤트 발행
    @Async("messageRelayPublishEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishEvent(OutboxEvent outboxEvent) {
        publishEvent(outboxEvent.getOutbox());
    }

    // shardKey에 따라 동일한 파티션으로 전달되어 같은 샤드에 대해 동일한 순서 보장
    private void publishEvent(Outbox outbox) {
        // CompletableFuture를 반환하므로 get을 통해 동기화한 뒤 성공하는 경우 Outbox 삭제, 실패하는 경우만 재전송하기 위해
        try {
            messageRelayKafkaTemplate.send(
                    outbox.getEventType().getTopic(),
                    String.valueOf(outbox.getShardKey()),
                    outbox.getPayload()
            ).get(1, TimeUnit.SECONDS); // 최대 1초 대기

            outboxRepository.delete(outbox);
        } catch (Exception e) {
            log.error("[MessageRelay.publishEvent] outbox={}", outbox, e);
        }
    }

    // polling 방식으로 주기적으로 outbox 재전송 실행
    // fixedDelay: 이전 작업이 끝난 후 10초 후에 다시 실행
    // initialDelay: 애플리케이션 시작 후 5초 후에 처음 실행
    @Scheduled(
            fixedDelay = 10,
            initialDelay = 5,
            timeUnit = TimeUnit.SECONDS,
            scheduler = "messageRelayPublishPendingEventExecutor"
    )
    public void publishPendingEvents() {
        AssignedShard assignedShard = messageRelayCoordinator.assignedShard();
        log.info("[MessageRelay.publishPendingEvents] assignedShard size={}", assignedShard.getShards().size());
        //할당된 샤드에 대해 주기적으로 polling
        for (Long shard : assignedShard.getShards()) {
            // 해당 샤드에 대해 아직 생성된지 10초가 지났으며 전송되지 않은 outbox 이벤트들을 최대 100개까지 조회
            List<Outbox> outboxes = outboxRepository.findAllByShardKeyAndCreatedAtLessThanEqualOrderByCreatedAtAsc(
                    shard,
                    LocalDateTime.now().minusSeconds(10),
                    Pageable.ofSize(100)
            );
            // 조회된 outbox 이벤트들을 순차적으로 kafka에 발행
            for( Outbox outbox : outboxes) {
                publishEvent(outbox);
            }
        }
        log.info("[MessageRelay.publishPendingEvents] end");
    }
}
