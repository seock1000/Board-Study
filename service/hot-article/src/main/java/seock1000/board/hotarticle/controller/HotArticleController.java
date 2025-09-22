package seock1000.board.hotarticle.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import seock1000.board.hotarticle.response.HotArticleResponse;
import seock1000.board.hotarticle.service.HotArticleService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HotArticleController {
    private final HotArticleService hotArticleService;

    @GetMapping("/v1/hot-articles/articles/date/{dateStr}")
    public List<HotArticleResponse> readAll(
            @PathVariable("dateStr") String dateStr
    ) {
        return hotArticleService.readAll(dateStr);
    }
}
