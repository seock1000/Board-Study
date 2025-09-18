package seock1000.board.hotarticle.eventhandler;

import seock1000.board.common.event.Event;
import seock1000.board.common.event.EventPayload;

public interface EventHandler<T extends EventPayload> {
    void handle(Event<T> event);
    boolean supports(Event<T> event);
    Long findArticleId(Event<T> event);
}
