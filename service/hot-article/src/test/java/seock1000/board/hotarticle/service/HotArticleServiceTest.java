package seock1000.board.hotarticle.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seock1000.board.common.event.Event;
import seock1000.board.common.event.EventType;
import seock1000.board.hotarticle.eventhandler.EventHandler;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HotArticleServiceTest {
    @InjectMocks
    HotArticleService hotArticleService;
    @Mock
    List<EventHandler> eventHandlers;
    @Mock
    HotArticleScoreUpdater hotArticleScoreUpdater;

    @Test
    void 이벤트_핸들러_없음() {
        //given
        Event event = mock(Event.class);
        EventHandler eventHandler = mock(EventHandler.class);
        when(eventHandler.supports(event)).thenReturn(false);
        when(eventHandlers.stream()).thenReturn(Stream.of(eventHandler));

        //when
        hotArticleService.handleEvent(event);

        //then
        verify(eventHandler, never()).handle(any());
        verify(hotArticleScoreUpdater, never()).update(any(), any());
    }

    @Test
    void 이벤트_핸들러_있음_Article_Created() {
        //given
        Event event = mock(Event.class);
        EventHandler eventHandler = mock(EventHandler.class);
        when(eventHandler.supports(event)).thenReturn(true);
        when(event.getType()).thenReturn(EventType.ARTICLE_CREATED);
        when(eventHandlers.stream()).thenReturn(Stream.of(eventHandler));

        //when
        hotArticleService.handleEvent(event);

        //then
        verify(eventHandler, times(1)).handle(any());
        verify(hotArticleScoreUpdater, never()).update(any(), any());
    }

    @Test
    void 이벤트_핸들러_있음_Article_Deleted() {
        //given
        Event event = mock(Event.class);
        EventHandler eventHandler = mock(EventHandler.class);
        when(eventHandler.supports(event)).thenReturn(true);
        when(event.getType()).thenReturn(EventType.ARTICLE_DELETED);
        when(eventHandlers.stream()).thenReturn(Stream.of(eventHandler));

        //when
        hotArticleService.handleEvent(event);

        //then
        verify(eventHandler, times(1)).handle(any());
        verify(hotArticleScoreUpdater, never()).update(any(), any());
    }

    @Test
    void 이벤트_핸들러_있음_UPDATE_SCORE_EVENT() {
        //given
        Event event = mock(Event.class);
        EventHandler eventHandler = mock(EventHandler.class);
        when(eventHandler.supports(event)).thenReturn(true);
        when(event.getType()).thenReturn(EventType.ARTICLE_LIKED);
        when(eventHandlers.stream()).thenReturn(Stream.of(eventHandler));

        //when
        hotArticleService.handleEvent(event);

        //then
        verify(eventHandler, never()).handle(any());
        verify(hotArticleScoreUpdater, times(1)).update(any(), any());
    }
}
