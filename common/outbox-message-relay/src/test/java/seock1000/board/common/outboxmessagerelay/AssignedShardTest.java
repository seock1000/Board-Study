package seock1000.board.common.outboxmessagerelay;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AssignedShardTest {

    @Test
    void ofTest() {
        //given
        // 총 샤드 개수
        int shardCount = 64;
        // 각 application Id
        List<String> appIds = List.of("app1", "app2", "app3", "app4", "app5");

        //when
        AssignedShard assignedShard1 = AssignedShard.of("app1", appIds, shardCount);
        AssignedShard assignedShard2 = AssignedShard.of("app2", appIds, shardCount);
        AssignedShard assignedShard3 = AssignedShard.of("app3", appIds, shardCount);
        AssignedShard assignedShard4 = AssignedShard.of("app4", appIds, shardCount);
        AssignedShard assignedShard5 = AssignedShard.of("app5", appIds, shardCount);
        AssignedShard assignedShardX = AssignedShard.of("appInvalid", appIds, shardCount);

        //then
        List<Long> result = Stream.of(
                        assignedShard1.getShards(),
                        assignedShard2.getShards(),
                        assignedShard3.getShards(),
                        assignedShard4.getShards(),
                        assignedShard5.getShards()
                )
                .flatMap(List::stream)
                .toList();
        // 모든 샤드가 중복 없이 할당되었는지 확인
        assertThat(result).hasSize(shardCount).doesNotHaveDuplicates();

        for(int i = 0; i < shardCount; i++) {
            assertThat(result.get(i)).isEqualTo(i);
        }
        assertThat(assignedShardX.getShards()).isEmpty();
    }
}