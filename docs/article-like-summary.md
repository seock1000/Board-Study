### 좋아요 설계
- 대규모 시스템에서 count 쿼리는 비용이 많이 든다.
- 좋아요 수를 실시간으로 보여줘야 하는 경우, 매번 count 쿼리를 날리면 성능에 큰 영향을 미친다.
- 따라서, 좋아요 테이블의 게시글별 데이터 개수를 비정규화 하여 별도의 테이블에 저장해두고 생성/삭제시 이를 갱신하는 방식으로 설계한다.

- 좋아요 데이터의 특징
  - 쓰기 트래픽이 크지 않음
  - 데이터의 일관성 중요
  - DB 트랜잭션 활용 -> 생성/삭제와 좋아요 개수 갱신을 같은 트랜잭션에서 처리
- 좋아요 개수 관리 방법
  - 게시글 테이블의 컬럼으로 설정
    - 발생 문제, RecordLock : 레코드에 락이 걸려 다른 트랜잭션(좋아요/게시글)이 대기하게 된다. 이로 인해 성능 저하 및 데드락 발생 가능성이 있다.
      - 게시글과 좋아요의 life cycle이 다 -> 서로 다른 주체에 의해 레코드의 락 발생 가능
  - 좋아요 수를 독립적인 테이블로 분리
    - 좋아요 데이터와 좋아요 개수의 일관성이 중요
    - 분산 환경을 지향하는 시스템에서 좋아요 수는 좋아요 서비스의 관심사로 보는 것이 자연스러움
    - 좋아요 서비스의 단일 DB에서 하나의 트랜잭션으로 처리

- 동시성 문제
  - 좋아요 요청이 동시에 들어올 때, 트랜잭션을 사용하더라도 좋아요 개수의 데이터 일관성이 깨질 수 있음
    - Lost Update 현상
      - 트랜잭션 A와 트랜잭션 B가 동시에 좋아요 개수를 읽고, 각각 좋아요 개수를 1 증가시킨 후 커밋
      - 최종적으로 좋아요 개수가 1만 증가하게 됨
    - 해결 방법
      - 비관적 락
        - 데이터 접근 시 충돌 가능성이 있다고 가정
        - 항상 락을 걸어 데이터를 보호
        - 데이터에 접근하는 모든 트랜잭션이 순차적으로 처리되어야 하므로 성능 저하 발생
        - deadlock 발생 가능성 존재
        - 구현 방법1 : DB 저장 값을 기준으로 update 수행
          ```aiignore
            transaction start;
            insert into article_like (article_like_id, article_id, user_id, created_at) values (?, ?, ?, ?);
            update article_like_count set like_count = like_count + 1 where article_id = ?;
            transaction commit;
          ``` 
          - 락 점유 시간이 상대적으로 짧음
          - 저장된 데이터를 기준으로 증감처리하므로 SQL을 직접 전송
        - 구현 방법2 : 데이터 조회 시 lock을 걸어 처리(for update 구문)
          ```aiignore
            transaction start;
            insert into article_like (article_like_id, article_id, user_id, created_at) values (?, ?, ?, ?);
            select like_count from article_like_count where article_id = ? for update;
            update article_like_count set like_count = like_count + 1 where article_id = ?;
            transaction commit;
          ```
          - for update: 해당 레코드에 락을 걸어 다른 트랜잭션이 접근하지 못하게 함
          - 락 점유 시간이 상대적으로 김
          - 데이터 조회 후 과정을 수행하므로 락 해제 지연 가능성
          - 어플리케이션 레벨에서 비즈니스 로직(좋아요 개수 증감) 관리 가능