### 무한 Depth 댓글

- Depth 경로 정보 
  - 문자열 컬럼 1개로 관리
  - path enumeration(경로 열거) 방식
    - 각 depth에서 순서를 문자열로 나타내고 이를 결합하여 경로를 표현
    - xxxxx(1depth)|xxxxx(2depth)|xxxxx(3depth)|...
    - n-depth length : n * 5 
    - 문자열로 모든 상위 댓글에서 각 댓글까지 경로 저장
    - ex) 1depth(00001), 2depth(0000100001), 3depth(000010000100001)
    - 각 depth는 문자열이므로 62^5(0-9, A-Z, a-z로 916132832)개의 댓글을 표현 가능
    - 유의사항
      - DB Collation: 문자열을 정렬하고 비교 방법을 정의하는 설정, DB-테이블-컬럼 별 설정 가능
      - 현재 설정: utf8mb4_0900_ai_ci
        - utf8mb4 : 4바이트 유니코드 문자 지원
        - 0900 : 정력방식 버전
        - ai : 악센트 구분 없이 비교
        - ci : 대소문자 구분 없이 비교(설정 변경 필요) => path varchar(25) character set utf8mb4 collate utf8mb4_bin not null
  - path를 정렬데이터로 사용하기 때문에 index path로 설정, 독립적인 경로를 가지므로 unique index 설정 -> 동시성 문제 해결
    ```aiignore
    create unique index idx_article_id_path on comment_v2(article_id asc, path asc);
    ```
- 댓글 추가시 path 생성 로직
  - children top path 조회
    - children top path : 부모 댓글의 자식 댓글 중 가장 마지막 댓글의 path, 해당하는 path를 빠르게 찾는 방법이 필요
    - 모든 자식 댓글은 동일한 prefix를 가짐
    - descendents top path: prefix를 포함하는 모든 path 중 가장 큰 path
      - children top path와 다를 수 있지만, children top path가 반드시 포함 됨
        - ex) 부모 path: 00001, 자식 path: 0000100001, 0000100002, 0000100003, 000010000300001
        - children top path: 0000100002, descendents top path: 000010000300001
    - descendents top path에서 생성되는 댓글의 depth * 5(depth별 자릿수)만 남기고 잘라내면 children top path 획득 가능
    - 즉, descendents top path를 빠르게 조회하면 children top path도 빠르게 조회 가능
    - 쿼리
    - ```sql
      select path from comment_v2
        where article_id = {article_id} 
            and path > {parent_path}
            and path like concat({parent_path}, '%')
        order by path desc
        limit 1;
      ```
      - path > {parent_path} : 부모 댓글의 path보다 큰 path만 조회, 본인은 미포함
      - path like concat({parent_path}, '%') : 부모 댓글의 path로 시작하는 path만 조회
      - order by path desc limit 1 : 가장 큰 path(=descendents top path) 1개만 조회
        - path는 asc로 index 되어있는데 빠른 조회가 가능한가?
          ```sql
            mysql> explain select path from comment_v2
                where article_id = 1
                    and path > '00a0z'
                    and path like concat('00a0z', '%')
                order by path desc limit 1;
          
              +----+-------------+------------+------------+-------+---------------------+---------------------+---------+------+------+----------+-----------------------------------------------+
              | id | select_type | table      | partitions | type  | possible_keys       | key                 | key_len | ref  | rows | filtered | Extra                                         |
              +----+-------------+------------+------------+-------+---------------------+---------------------+---------+------+------+----------+-----------------------------------------------+
              |  1 | SIMPLE      | comment_v2 | NULL       | index | idx_article_id_path | idx_article_id_path | 110     | NULL |    1 |   100.00 | Using where; Backward index scan; Using index |
              +----+-------------+------------+------------+-------+---------------------+---------------------+---------+------+------+----------+-----------------------------------------------+
          ```
          - 커버렁 인덱스로 동작, index 사용됨
          - Backward index scan : index를 역방향으로 스캔, 인덱스 트리 leaf node간 연결된 양방향 포인터를 이용
          - 즉, index를 역방향으로 스캔하는 것이므로 index가 asc로 되어있어도 desc로 정렬된 것처럼 동작
    - 문자열 기반 덧셈 연산 코드로 처리 필요 
- 목록 조회
  ```aiignore
    select * from (
        select comment_id 
            from comment_v2
            where article_id = ?
            order by path asc
            limit ? offset ?
    ) t left join comment_v2 on t.comment_id = comment_v2.comment_id;
  ```
  - 카운트 조회
  ```aiignore
    select count(*) from (
        select comment_id 
            from comment_v2
            where article_id = ?
            limit ?
    ) t;
  ```
- 무한 스크롤 조회
  - 첫 페이지
    ```aiignore
      select * from comment_v2
          where article_id = ?
          order by path asc limit ?;
    ```
    - 두 번째 페이지 이후
        ```aiignore
        select * from comment_v2
            where article_id = ? and path > ?
            order by path asc limit ?;
        ```