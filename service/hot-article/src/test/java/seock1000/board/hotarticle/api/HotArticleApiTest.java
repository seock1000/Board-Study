package seock1000.board.hotarticle.api;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import seock1000.board.hotarticle.response.HotArticleResponse;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HotArticleApiTest {
    RestClient restClient = RestClient.create("http://localhost:9004");

    @Test
    void readAllTest() {
        List<HotArticleResponse> responses = restClient.get()
                .uri("/v1/hot-articles/articles/date/{dateStr}", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                .retrieve()
                .body(new ParameterizedTypeReference<List<HotArticleResponse>>(){});

        for(HotArticleResponse article : responses) {
            System.out.println("article = " + article);
        }
    }
}
