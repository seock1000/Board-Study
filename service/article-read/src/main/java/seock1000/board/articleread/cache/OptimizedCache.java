package seock1000.board.articleread.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.ToString;
import seock1000.board.common.dataserializer.DataSerializer;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@ToString
public class OptimizedCache {
    private String data;
    private LocalDateTime expiredAt; // logical expiration time

    public static OptimizedCache of(Object data, Duration ttl) {
        OptimizedCache cache = new OptimizedCache();
        cache.data = DataSerializer.serialize(data);
        cache.expiredAt = LocalDateTime.now().plus(ttl);
        return cache;
    }

    @JsonIgnore
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    public <T> T parseData(Class<T> dataType) {
        return DataSerializer.deserialize(data, dataType);
    }
}
