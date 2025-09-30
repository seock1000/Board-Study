package seock1000.board.articleread.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import seock1000.board.articleread.service.ArticleReadService;
import seock1000.board.articleread.service.response.ArticleReadResponse;

@RestController
@RequiredArgsConstructor
public class ArticleReadController {
    private final ArticleReadService articleReadService;

    @GetMapping("/v1/articles/{articleId}")
    public ArticleReadResponse read(
            @PathVariable("articleId") Long articleId
    ) {
        return articleReadService.read(articleId);
    }
}
