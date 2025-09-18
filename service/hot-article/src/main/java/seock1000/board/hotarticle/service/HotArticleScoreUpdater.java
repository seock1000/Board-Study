package seock1000.board.hotarticle.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import seock1000.board.common.event.Event;
import seock1000.board.common.event.EventPayload;
import seock1000.board.hotarticle.eventhandler.EventHandler;
import seock1000.board.hotarticle.repository.ArticleCreatedTimeRepository;
import seock1000.board.hotarticle.repository.HotArticleListRepository;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class HotArticleScoreUpdater {
    private final HotArticleListRepository hotArticleListRepository;
    private final ArticleCreatedTimeRepository articleCreatedTimeRepository;

    private final HotArticleScoreCalculator hotArticleScoreCalculator;

    private static final long HOT_ARTICLE_COUNT = 10;
    private static final Duration HOT_ARTICLE_TTL = Duration.ofDays(10); // 10일(7일만 넘게 저장하면 application logic에서 처리 가능)

    public void update(Event<EventPayload> event, EventHandler<EventPayload> handler) {
        Long articleId = handler.findArticleId(event);
        LocalDateTime createdTime = articleCreatedTimeRepository.getCreatedTime(articleId);

        if(!isArticleCreatedToday(createdTime)) {
            // 오늘 생성된 게시글이 아니면 점수 업데이트하지 않음
            return;
        }

        // event handler에서 전달받은 값을 redis에 업데이트
        handler.handle(event);

        long score = hotArticleScoreCalculator.calculate(articleId);
        hotArticleListRepository.add(
                articleId,
                createdTime,
                score,
                HOT_ARTICLE_COUNT,
                HOT_ARTICLE_TTL
        );
    }

    private boolean isArticleCreatedToday(LocalDateTime createdTime) {
        return createdTime != null && createdTime.toLocalDate().isEqual(LocalDateTime.now().toLocalDate());
    }
}
