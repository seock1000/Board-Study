package seock1000.board.hotarticle.eventhandler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import seock1000.board.common.event.Event;
import seock1000.board.common.event.EventType;
import seock1000.board.common.event.payload.ArticleViewedEventPayload;
import seock1000.board.hotarticle.repository.ArticleViewCountRepository;
import seock1000.board.hotarticle.utils.TimeCalculatorUtils;

@Component
@RequiredArgsConstructor
public class ArticleViewEventHandler implements EventHandler<ArticleViewedEventPayload> {
    private final ArticleViewCountRepository articleViewCountRepository;

    @Override
    public void handle(Event<ArticleViewedEventPayload> event) {
        ArticleViewedEventPayload payload = event.getPayload();
        articleViewCountRepository.createOrUpdate(
                payload.getArticleId(),
                payload.getArticleViewCount(),
                TimeCalculatorUtils.calculateDurationToMidnight()
        );
    }

    @Override
    public boolean supports(Event<ArticleViewedEventPayload> event) {
        return EventType.ARTICLE_VIEWED == event.getType();
    }

    @Override
    public Long findArticleId(Event<ArticleViewedEventPayload> event) {
        return event.getPayload().getArticleId();
    }
}
