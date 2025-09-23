package seock1000.board.common.outboxmessagerelay;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@EnableAsync
@Configuration
@ComponentScan(basePackages = "seock1000.board.common.outboxmessagerelay")
@EnableScheduling
public class MessageRelayConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * kafka로 producer application 들이 이벤트를 전달할 때 사용하는 template
     */
    @Bean
    public KafkaTemplate<String, String> messageRelayKafkaTemplate() {
        // KafkaTemplate을 생성하기 위한 설정
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // 역직렬화와 동일하게 String
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // 모든 ISR 멤버로부터 확인 응답을 기다림(유실방지)
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(configProps));
    }

    /**
     * 트랜잭션 종료마다 이벤트를 비동기로 발행하는데 사용하는 쓰레드풀
     * corePoolSize: 기본적으로 유지할 쓰레드 수
     * maxPoolSize: 최대 생성 가능한 쓰레드 수
     * queueCapacity: corePoolSize 초과 시, 작업을 대기시키는 큐의 크기
     * ThreadNamePrefix: 생성되는 쓰레드 이름의 접두사
     */
    @Bean
    public Executor messageRelayPublishEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("my-pub-event-");
        return executor;
    }

    /**
     * 스케줄링으로 미처리 이벤트를 재발행하는데 사용하는 쓰레드풀
     * 싱글 쓰레드로 동작 - 미전송 이벤트가 많지 않을 것으로 예상되어 단일 쓰레드로 처리
     */
    @Bean
    public Executor messageRelayPublishPendingEventExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }
}
