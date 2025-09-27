package seock1000.board.articleread.service.event.handler;

import seock1000.board.common.event.Event;
import seock1000.board.common.event.EventPayload;

public interface EventHandler<T extends EventPayload> {
    void handle(Event<T> event);
    boolean supports(Event<T> event);
}
