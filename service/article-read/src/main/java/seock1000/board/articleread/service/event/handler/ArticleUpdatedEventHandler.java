package seock1000.board.articleread.service.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import seock1000.board.articleread.repository.ArticleQueryModel;
import seock1000.board.articleread.repository.ArticleQueryModelRepository;
import seock1000.board.common.event.Event;
import seock1000.board.common.event.EventType;
import seock1000.board.common.event.payload.ArticleCreatedEventPayload;
import seock1000.board.common.event.payload.ArticleUpdatedEventPayload;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ArticleUpdatedEventHandler implements EventHandler<ArticleUpdatedEventPayload>  {
    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<ArticleUpdatedEventPayload> event) {
        ArticleUpdatedEventPayload articleUpdatedEventPayload = event.getPayload();
        articleQueryModelRepository.read(articleUpdatedEventPayload.getArticleId())
                .ifPresent(articleQueryModel -> {
                    articleQueryModel.updateBy(articleUpdatedEventPayload);
                    articleQueryModelRepository.update(articleQueryModel);
                });
    }

    @Override
    public boolean supports(Event<ArticleUpdatedEventPayload> event) {
        return EventType.ARTICLE_UPDATED == event.getType();
    }
}
