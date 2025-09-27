package seock1000.board.articleread.service.event.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import seock1000.board.articleread.repository.ArticleQueryModelRepository;
import seock1000.board.common.event.Event;
import seock1000.board.common.event.EventType;
import seock1000.board.common.event.payload.ArticleLikedEventPayload;
import seock1000.board.common.event.payload.CommentCreatedEventPayload;

@Component
@RequiredArgsConstructor
public class ArticleLikedEventHandler implements EventHandler<ArticleLikedEventPayload>  {
    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<ArticleLikedEventPayload> event) {
        ArticleLikedEventPayload articleLikedEventPayload = event.getPayload();
        articleQueryModelRepository.read(articleLikedEventPayload.getArticleId())
                .ifPresent(articleQueryModel -> {
                    articleQueryModel.updateBy(articleLikedEventPayload);
                    articleQueryModelRepository.update(articleQueryModel);
                });
    }

    @Override
    public boolean supports(Event<ArticleLikedEventPayload> event) {
        return EventType.ARTICLE_LIKED == event.getType();
    }
}
