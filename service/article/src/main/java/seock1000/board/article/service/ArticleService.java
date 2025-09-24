package seock1000.board.article.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seock1000.board.article.entity.Article;
import seock1000.board.article.entity.BoardArticleCount;
import seock1000.board.article.repository.ArticleRepository;
import seock1000.board.article.repository.BoardArticleCountRepository;
import seock1000.board.article.service.request.ArticleCreateRequest;
import seock1000.board.article.service.request.ArticleUpdateRequest;
import seock1000.board.article.service.response.ArticlePageResponse;
import seock1000.board.article.service.response.ArticleResponse;
import seock1000.board.common.event.EventType;
import seock1000.board.common.event.payload.ArticleCreatedEventPayload;
import seock1000.board.common.event.payload.ArticleDeletedEventPayload;
import seock1000.board.common.event.payload.ArticleUpdatedEventPayload;
import seock1000.board.common.outboxmessagerelay.OutboxEventPublisher;
import seock1000.board.common.snowflake.Snowflake;

import java.util.List;

import static seock1000.board.article.service.PageLimitCalculator.calculatePageLimit;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final Snowflake snowflake = new Snowflake();
    private final ArticleRepository articleRepository;
    private final BoardArticleCountRepository boardArticleCountRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional
    public ArticleResponse create(ArticleCreateRequest request) {
        Article article = articleRepository.save(
                Article.create(snowflake.nextId(), request.getTitle(), request.getContent(), request.getBoardId(), request.getWriterId())
        );
        int result = boardArticleCountRepository.increase(request.getBoardId());
        if(result == 0) {
            boardArticleCountRepository.save(
                    BoardArticleCount.init(request.getBoardId(), 1L)
            );
        }
        outboxEventPublisher.publish(
                EventType.ARTICLE_CREATED,
                ArticleCreatedEventPayload.builder()
                        .articleId(article.getArticleId())
                        .title(article.getTitle())
                        .content(article.getContent())
                        .boardId(article.getBoardId())
                        .writerId(article.getWriterId())
                        .createdAt(article.getCreatedAt())
                        .modifiedAt(article.getModifiedAt())
                        .boardArticleCount(count(article.getBoardId()))
                        .build(),
                article.getBoardId() // shardKey
        );
        return ArticleResponse.from(article);
    }

    @Transactional
    public ArticleResponse update(Long articleId, ArticleUpdateRequest request) {
        Article article = articleRepository.findById(articleId).orElseThrow();
        article.update(request.getTitle(), request.getContent());
        outboxEventPublisher.publish(
                EventType.ARTICLE_UPDATED,
                ArticleUpdatedEventPayload.builder()
                        .articleId(article.getArticleId())
                        .title(article.getTitle())
                        .content(article.getContent())
                        .boardId(article.getBoardId())
                        .writerId(article.getWriterId())
                        .createdAt(article.getCreatedAt())
                        .modifiedAt(article.getModifiedAt())
                        .build(),
                article.getBoardId() // shardKey
        );
        return ArticleResponse.from(article);
    }

    public ArticleResponse read(Long articleId) {
        return ArticleResponse.from(articleRepository.findById(articleId).orElseThrow());
    }

    @Transactional
    public void delete(Long articleId) {
        Article article = articleRepository.findById(articleId).orElseThrow();
        articleRepository.delete(article);
        boardArticleCountRepository.decrease(article.getBoardId());
        outboxEventPublisher.publish(
                EventType.ARTICLE_DELETED,
                ArticleDeletedEventPayload.builder()
                        .articleId(article.getArticleId())
                        .title(article.getTitle())
                        .content(article.getContent())
                        .boardId(article.getBoardId())
                        .writerId(article.getWriterId())
                        .createdAt(article.getCreatedAt())
                        .modifiedAt(article.getModifiedAt())
                        .boardArticleCount(count(article.getBoardId()))
                        .build(),
                article.getBoardId() // shardKey
        );
    }

    public ArticlePageResponse readAll(Long boardId, Long page, Long pageSize) {
        return ArticlePageResponse.of(
                articleRepository.findAll(boardId, (page - 1) * pageSize, pageSize).stream()
                .map(ArticleResponse::from)
                .toList(),
                articleRepository.count(
                        boardId,
                        calculatePageLimit(page, pageSize, 10L)
                )
        );
    }

    public List<ArticleResponse> readAllInfiniteScroll(Long boardId, Long pageSize, Long lastArticleId) {
        List<Article> articles = lastArticleId == null ?
                articleRepository.findAllInfiniteScroll(boardId, pageSize) :
                articleRepository.findAllInfiniteScroll(boardId, pageSize, lastArticleId);

        return articles.stream()
                .map(ArticleResponse::from)
                .toList();
    }

    public Long count(Long boardId) {
        System.out.println("count boardId = " + boardId);
        return boardArticleCountRepository.findById(boardId)
                .map(BoardArticleCount::getArticleCount)
                .orElse(0L);
    }

}
