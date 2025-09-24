package seock1000.board.like;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = "seock1000.board")
@EnableJpaRepositories(basePackages = "seock1000.board")
@SpringBootApplication
public class LikeApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(LikeApplication.class, args);
    }
}
