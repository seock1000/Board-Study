package seock1000.board.hotarticle.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import seock1000.board.common.event.Event;
import seock1000.board.common.event.EventPayload;
import seock1000.board.common.event.EventType;
import seock1000.board.hotarticle.service.HotArticleService;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotArticleEventConsumer {
    private final HotArticleService hotArticleService;

    // 처리가 성공하면 Acknowledgment를 통해 커밋을 수행
    @KafkaListener(topics = {
            EventType.Topic.SEOCK1000_BOARD_ARTICLE,
            EventType.Topic.SEOCK1000_BOARD_COMMENT,
            EventType.Topic.SEOCK1000_BOARD_LIKE,
            EventType.Topic.SEOCK1000_BOARD_VIEW
    })
    public void listen(String message, Acknowledgment ack) {
        log.info("[HotArticleEventConsumer.listen] received message={}", message);
        Event<EventPayload> event = Event.fromJson(message);
        if(event != null) {
            // 이벤트 변환에 성공했으면 handle
            hotArticleService.handleEvent(event);
        }
        // 메시지 처리가 끝났음을 KAFKA에 알림
        ack.acknowledge();
    }


}
