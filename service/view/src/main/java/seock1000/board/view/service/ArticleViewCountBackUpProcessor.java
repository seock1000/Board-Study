package seock1000.board.view.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seock1000.board.common.event.EventType;
import seock1000.board.common.event.payload.ArticleViewedEventPayload;
import seock1000.board.common.outboxmessagerelay.OutboxEventPublisher;
import seock1000.board.view.entity.ArticleViewCount;
import seock1000.board.view.repository.ArticleViewCountBackUpRepository;

@Component
@RequiredArgsConstructor
public class ArticleViewCountBackUpProcessor {
    private final ArticleViewCountBackUpRepository articleViewCountBackUpRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional
    public void backUp(Long articleId, Long viewCount) {
        int result = articleViewCountBackUpRepository.updateViewCount(articleId, viewCount);
        if(result == 0) {
            articleViewCountBackUpRepository.findById(articleId)
                    .ifPresentOrElse(
                            ignore -> {},
                            () -> articleViewCountBackUpRepository.save(ArticleViewCount.init(articleId, viewCount))
                    );
        }

        // 백업 시점에 이벤트 발행
        outboxEventPublisher.publish(
                EventType.ARTICLE_VIEWED,
                ArticleViewedEventPayload.builder()
                        .articleId(articleId)
                        .articleViewCount(viewCount)
                        .build(),
                articleId // shardKey
        );
    }
}
