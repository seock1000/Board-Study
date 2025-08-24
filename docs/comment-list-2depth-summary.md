### 최대 2depth 댓글 목록 조회

- 댓글의 구조 예시
  ```aiignore
    Comment1
        ├─ Comment4
        ├─ Comment5
        └─ Comment7
    Comment2
        ├─ Comment3
        └─ Comment6
  ```
  - 단순한 댓글의 생성 순서로는 정렬순서가 명확하지 않다
    - 대댓글이라면 부모 댓글 바로 아래에 위치함
    - 따라서, 단순히 생성일 순서로 정렬할 수 없음
  - parentId, commentId를 정렬 조건으로 사용
    - Id는 snowflake로 생성
    - order by article_id asc, parent_comment_id asc, comment_id asc
      - article_id : 댓글이 속한 글(shard key)
      - parent_comment_id : 부모 댓글 (대댓글인 경우)
      - comment_id : 댓글 자신
  - index 생성
    ```sql
    CREATE INDEX idx_article_parent_comment ON comment(article_id, parent_comment_id, comment_id);
    ```
- paging query
  - n번 페이지에서 m개의 댓글 조회
  - 커버링 인덱스를 사용해서 빠르게 comment_id 목록 조회
    ```sql
    SELECT * FROM (
        select comment_id from comment
            where article_id = ?
            order by parent_comment_id asc, comment_id asc
            limit ? offset ?
    ) t left join comment on t.comment_id = comment.comment_id;
    ```
  - 댓글 개수 조회
    ```sql
    select count(*) from (
        select comment_id from comment where article_id = ? limit ?
    ) t;
    ```
    
- infinite scroll query
  - 마지막으로 불러온 댓글 이후의 m개의 댓글 조회
  - 첫 번째 페이지 : 기준점이 없으므로 기준점 조건 없이 limit 만 사용, 커버링 인덱스 활용됨
    ```sql
    SELECT * FROM comment
        WHERE article_id = ?
        ORDER BY parent_comment_id asc, comment_id asc
        limit ?;
    ```
  - 두 번째 페이지 이상 : 마지막으로 불러온 댓글의 (parent_comment_id, comment_id) 를 기준점으로 사용
    ```sql
    SELECT * FROM comment
        WHERE article_id = ? AND (
            parent_comment_id > ? OR
            (parent_comment_id = ? AND comment_id > ?)
        )
        ORDER BY parent_comment_id asc, comment_id asc
        limit ?;
    ```