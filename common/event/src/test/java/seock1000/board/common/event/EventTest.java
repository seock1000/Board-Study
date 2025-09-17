package seock1000.board.common.event;

import org.junit.jupiter.api.Test;
import seock1000.board.common.event.payload.ArticleCreatedEventPayload;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    void serialize() {
        //given
        LocalDateTime now = LocalDateTime.now();
        EventPayload payload = ArticleCreatedEventPayload.builder()
                .articleId(1L)
                .title("title")
                .content("content")
                .boardId(1L)
                .writerId(1L)
                .createdAt(now)
                .modifiedAt(now)
                .boardArticleCount(1L)
                .build();
        Event<EventPayload> event = Event.of(
                1L,
                EventType.ARTICLE_CREATED,
                payload
        );
        String expectedJson = """
                {
                    "eventId": 1,
                    "type": "ARTICLE_CREATED",
                    "payload": {
                        "articleId": 1,
                        "title": "title",
                        "content": "content",
                        "boardId": 1,
                        "writerId": 1,
                        "createdAt": "%s",
                        "modifiedAt": "%s",
                        "boardArticleCount": 1
                    }
                }
                """.formatted(now.toString(), now.toString());

        //when
        String actual = event.toJson();

        //then
        assertNotNull(actual);
        assertThat(actual).isEqualTo(expectedJson.replace(" ", "").replace("\n", ""));
    }

    @Test
    void deserialize() {
        //given
        ArticleCreatedEventPayload payload = ArticleCreatedEventPayload.builder()
                .articleId(1L)
                .title("title")
                .content("content")
                .boardId(1L)
                .writerId(1L)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .boardArticleCount(1L)
                .build();
        Event<EventPayload> expected = Event.of(
                1L,
                EventType.ARTICLE_CREATED,
                payload
        );
        String json = expected.toJson();

        //when
        Event<EventPayload> actual = Event.fromJson(json);

        //then
        assertThat(actual).isNotNull();
        assertThat(actual.getEventId()).isEqualTo(expected.getEventId());
        assertThat(actual.getType()).isEqualTo(expected.getType());
        assertThat(actual.getPayload()).isInstanceOf(ArticleCreatedEventPayload.class);
        ArticleCreatedEventPayload actualPayload = (ArticleCreatedEventPayload) actual.getPayload();
        assertThat(actualPayload.getArticleId()).isEqualTo(payload.getArticleId());
        assertThat(actualPayload.getTitle()).isEqualTo(payload.getTitle());
        assertThat(actualPayload.getContent()).isEqualTo(payload.getContent());
        assertThat(actualPayload.getBoardId()).isEqualTo(payload.getBoardId());
        assertThat(actualPayload.getWriterId()).isEqualTo(payload.getWriterId());
        assertThat(actualPayload.getCreatedAt()).isEqualTo(payload.getCreatedAt());
        assertThat(actualPayload.getModifiedAt()).isEqualTo(payload.getModifiedAt());
        assertThat(actualPayload.getBoardArticleCount()).isEqualTo(payload.getBoardArticleCount());
    }

}