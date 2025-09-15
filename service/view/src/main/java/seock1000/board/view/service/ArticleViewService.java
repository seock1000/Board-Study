package seock1000.board.view.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seock1000.board.view.repository.ArticleViewCountRepository;
import seock1000.board.view.repository.ArticleViewDistributedLockRepository;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ArticleViewService {
    private final ArticleViewCountRepository articleViewRepository;
    private final ArticleViewCountBackUpProcessor articleViewCountBackUpProcessor;
    private final ArticleViewDistributedLockRepository articleViewDistributedLockRepository;

    private static final int BACKUP_BATCH_SIZE = 100;
    private static final Duration TTL = Duration.ofMinutes(10);

    public Long increase(Long articleId, Long userId) {
        if(!articleViewDistributedLockRepository.lock(articleId, userId, TTL)) {
            return articleViewRepository.read(articleId);
        }

        return articleViewRepository.increase(articleId);
    }

    public Long count(Long articleId) {
        Long count = articleViewRepository.read(articleId);
        if(count % BACKUP_BATCH_SIZE == 0) {
            articleViewCountBackUpProcessor.backUp(articleId, count);
        }
        return count;
    }
}
