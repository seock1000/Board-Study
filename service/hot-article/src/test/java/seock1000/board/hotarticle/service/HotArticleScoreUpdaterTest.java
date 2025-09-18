package seock1000.board.hotarticle.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seock1000.board.common.event.Event;
import seock1000.board.hotarticle.eventhandler.EventHandler;
import seock1000.board.hotarticle.repository.ArticleCreatedTimeRepository;
import seock1000.board.hotarticle.repository.HotArticleListRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotArticleScoreUpdaterTest {
    @InjectMocks
    HotArticleScoreUpdater hotArticleScoreUpdater;
    @Mock
    HotArticleScoreCalculator hotArticleScoreCalculator;
    @Mock
    ArticleCreatedTimeRepository articleCreatedTimeRepository;
    @Mock
    HotArticleListRepository hotArticleListRepository;

    @Test
    void 오늘_생성되지_않은_게시글() {
        //given
        Long articleId = 1L;
        Event event = mock(Event.class);
        EventHandler eventHandler = mock(EventHandler.class);

        when(eventHandler.findArticleId(event)).thenReturn(articleId);
        LocalDateTime createdTime = LocalDateTime.now().minusDays(1); // 어제 생성된 게시글
        when(articleCreatedTimeRepository.getCreatedTime(articleId)).thenReturn(createdTime);

        //when
        hotArticleScoreUpdater.update(event, eventHandler);

        //then
        verify(eventHandler, never()).handle(any());
        verify(hotArticleListRepository, never()).add(anyLong(), any(), anyLong(), anyLong(), any());
    }

    @Test
    void 오늘_생성된_게시글() {
        //given
        Long articleId = 1L;
        Event event = mock(Event.class);
        EventHandler eventHandler = mock(EventHandler.class);

        when(eventHandler.findArticleId(event)).thenReturn(articleId);
        LocalDateTime createdTime = LocalDateTime.now(); // 오늘 생성된 게시글
        when(articleCreatedTimeRepository.getCreatedTime(articleId)).thenReturn(createdTime);

        //when
        hotArticleScoreUpdater.update(event, eventHandler);

        //then
        verify(eventHandler, times(1)).handle(any());
        verify(hotArticleListRepository, times(1)).add(anyLong(), any(), anyLong(), anyLong(), any());
    }
}