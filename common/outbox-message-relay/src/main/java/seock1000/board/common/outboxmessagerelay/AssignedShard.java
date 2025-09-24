package seock1000.board.common.outboxmessagerelay;

import lombok.Getter;

import java.util.List;
import java.util.stream.LongStream;

/**
 * 특정 어플리케이션이 관리하도록 할당된 shard 번호들
 */
@Getter
public class AssignedShard {
    private List<Long> shards;

    /**
     * @param appId : 현재 실행된 어플리케이션 아이디
     * @param appIds : coordinator가 관리하는 전체 어플리케이션 아이디 목록
     * @param shardCount : 전체 shard 개수
     * @return
     */
    public static AssignedShard of(String appId, List<String> appIds, long shardCount) {
        AssignedShard assignedShard = new AssignedShard();
        assignedShard.shards = assign(appId, appIds, shardCount);
        return assignedShard;
    }

    /**
     * 어플리케이션에 shard 할당
     */
    private static  List<Long> assign(String appId, List<String> appIds, long shardCount) {
        int appIndex = findAppIndex(appId, appIds);
        if(appIndex == -1) {
            // 할당할 shard 없음
            return List.of();
        }
        // app이 할당된 shard의 범위
        long start = appIndex * shardCount / appIds.size();
        long end = (appIndex + 1) * shardCount / appIds.size() - 1;

        return LongStream.rangeClosed(start, end).boxed().toList();
    }

    /**
     * 정렬된 실행된 appId 목록에서 현재 appId의 index 찾기
     */
    private static int findAppIndex(String appId, List<String> appIds) {
        for(int i = 0; i < appIds.size(); i++) {
            if(appIds.get(i).equals(appId)) {
                return i;
            }
        }
        return -1;
    }
}
