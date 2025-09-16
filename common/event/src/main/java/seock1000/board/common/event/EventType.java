package seock1000.board.common.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import seock1000.board.common.event.payload.*;

import static seock1000.board.common.event.EventType.Topic.*;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {
    ARTICLE_CREATED(ArticleCreatedEventPayload.class, SEOCK1000_BOARD_ARTICLE),
    ARTICLE_UPDATED(ArticleUpdatedEventPayload.class, SEOCK1000_BOARD_ARTICLE),
    ARTICLE_DELETED(ArticleDeletedEventPayload.class, SEOCK1000_BOARD_ARTICLE),
    COMMENT_CREATED(CommentCreatedEventPayload.class, SEOCK1000_BOARD_COMMENT),
    COMMENT_DELETED(CommentDeletedEventPayload.class, SEOCK1000_BOARD_COMMENT),
    ARTICLE_LIKED(ArticleLikedEventPayload.class, SEOCK1000_BOARD_LIKE),
    ARTICLE_UNLIKED(ArticleUnlikedEventPayload.class, SEOCK1000_BOARD_LIKE),
    ARTICLE_VIEWED(ArticleViewedEventPayload.class, SEOCK1000_BOARD_VIEW),;

    private final Class<? extends EventPayload> payloadClass;
    private final String topic;

    public static EventType from(String type) {
        try {
            return EventType.valueOf(type);
        } catch (IllegalArgumentException e) {
            log.error("[EventType.from] type={}", type, e);
            return null;
        }
    }

    public static class Topic {
        public static final String SEOCK1000_BOARD_ARTICLE = "seock1000-board-article";
        public static final String SEOCK1000_BOARD_COMMENT = "seock1000-board-comment";
        public static final String SEOCK1000_BOARD_LIKE = "seock1000-board-like";
        public static final String SEOCK1000_BOARD_VIEW = "seock1000-board-view";
    }
}
