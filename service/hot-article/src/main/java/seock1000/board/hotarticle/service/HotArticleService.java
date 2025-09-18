package seock1000.board.hotarticle.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import seock1000.board.common.event.Event;
import seock1000.board.common.event.EventPayload;
import seock1000.board.common.event.EventType;
import seock1000.board.hotarticle.client.ArticleClient;
import seock1000.board.hotarticle.eventhandler.EventHandler;
import seock1000.board.hotarticle.repository.HotArticleListRepository;
import seock1000.board.hotarticle.response.HotArticleResponse;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotArticleService {
    private final ArticleClient articleClient;
    //EventHandler 구현체가 전부 주입됨
    private final List<EventHandler> eventHandlers;
    private final HotArticleScoreUpdater hotArticleScoreUpdater;
    private final HotArticleListRepository hotArticleListRepository;

    public void handleEvent(Event<EventPayload> event) {
        var handler = findEventHandler(event);
        if (handler == null) {
            return;
        }
        // 생성 또는 삭제 이벤트인 경우 점수 업데이트 필요 X
        if(isArticleCreatedOrDeleted(event)) {
            handler.handle(event);
        } else {
            hotArticleScoreUpdater.update(event, handler);
        }
    }

    private EventHandler<EventPayload> findEventHandler(Event<EventPayload> event) {
        return eventHandlers.stream()
                .filter(handler -> handler.supports(event))
                .findAny()
                .orElse(null);
    }

    private boolean isArticleCreatedOrDeleted(Event<EventPayload> event) {
        return event.getType() == EventType.ARTICLE_CREATED || event.getType() == EventType.ARTICLE_DELETED;
    }

    // dateStr: "yyyyMMdd"
    public List<HotArticleResponse> readAll(String dateStr) {
        return hotArticleListRepository.readAll(dateStr).stream()
                .map(articleClient::read)
                .filter(Objects::nonNull)
                .map(HotArticleResponse::from)
                .toList();
    }
}
