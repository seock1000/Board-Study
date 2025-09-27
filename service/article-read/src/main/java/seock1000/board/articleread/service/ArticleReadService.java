package seock1000.board.articleread.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import seock1000.board.articleread.client.ArticleClient;
import seock1000.board.articleread.client.CommentClient;
import seock1000.board.articleread.client.LikeClient;
import seock1000.board.articleread.client.ViewClient;
import seock1000.board.articleread.repository.ArticleQueryModel;
import seock1000.board.articleread.repository.ArticleQueryModelRepository;
import seock1000.board.articleread.service.event.handler.EventHandler;
import seock1000.board.articleread.service.response.ArticleReadResponse;
import seock1000.board.common.event.Event;
import seock1000.board.common.event.EventPayload;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleReadService {
    private final ArticleClient articleClient;
    private final CommentClient commentClient;
    private final LikeClient likeClient;
    private final ViewClient viewClient;
    private final ArticleQueryModelRepository articleQueryModelRepository;

    private final List<EventHandler> eventHandlers;

    // consumer 통해 호출
    public void handleEvent(Event<EventPayload> event) {
        for(EventHandler eventHandler : eventHandlers) {
            if(eventHandler.supports(event)) {
                eventHandler.handle(event);
            }
        }
    }

    public ArticleReadResponse read(Long articleId) {
        ArticleQueryModel articleQueryModel = articleQueryModelRepository.read(articleId) // query model이 redis에 있으면 가져옴
                .or(() -> fetch(articleId)) // 없으면 fetch
                .orElseThrow(); // fetch도 없으면 예외 발생

        return ArticleReadResponse.from(
                articleQueryModel,
                viewClient.count(articleId)
        );
    }

    private Optional<ArticleQueryModel> fetch(Long articleId) {
        Optional<ArticleQueryModel> articleQueryModelOptional = articleClient.read(articleId)
                .map(article ->
                        ArticleQueryModel.create(
                                article,
                                commentClient.count(articleId),
                                likeClient.count(articleId)
                        ));
        // fetch한 데이터로 redis에 캐시 생성(1일 ttl)
        articleQueryModelOptional.ifPresent(articleQueryModel ->
                articleQueryModelRepository.create(articleQueryModel, Duration.ofDays(1))
        );
        log.info("[ArticleReadService.fetch] fetch data. articleId={} ifPresent={}", articleId, articleQueryModelOptional.isPresent());
        return articleQueryModelOptional;
    }
}
