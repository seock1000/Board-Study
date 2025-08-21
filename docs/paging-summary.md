## 페이징(offset 방식 조회)
- 쿼리 및 결과

    ```bash
    mysql> select * from article where board_id = 1 order by created_at desc limit 30 offset 90;
    30 rows in set (23.22 sec)
    ```

  → 1200만 건 대상 23.22s 소요

- 실행 계획

    ```bash
    mysql> explain select * from article where board_id = 1 order by created_at desc limit 30 offset 90;
    +----+-------------+---------+------------+------+---------------+------+---------+------+---------+----------+-----------------------------+
    | id | select_type | table   | partitions | type | possible_keys | key  | key_len | ref  | rows    | filtered | Extra                       |
    +----+-------------+---------+------------+------+---------------+------+---------+------+---------+----------+-----------------------------+
    |  1 | SIMPLE      | article | NULL       | ALL  | NULL          | NULL | NULL    | NULL | 2713855 |    10.00 | Using where; Using filesort |
    +----+-------------+---------+------------+------+---------------+------+---------+------+---------+----------+-----------------------------+
    1 row in set, 1 warning (0.03 sec)
    ```

    - type ALL: 테이블 풀스캔
    - Using filesort: 데이터가 많기 때문에 메모리 상이 아닌 file system에서 sorting(성능 저하)
- 해결방법 - 인덱스 사용
    - 인덱스

        ```bash
        mysql> create index idx_board_id_article_id on article(board_id asc, article_id desc);
        ```

        - board_id 오름차순, article_id 내림차순 정렬 (순서중요)
        - article_id를 인덱스로 선정하는 이유 → 대규모 서비스는 동시에 생성되어 동일 시간(created_at)을 갖는 여러 데이터가 존재하므로 snowflake 알고리즘으로 충돌가능성이 작은 article_id를 정렬조건으로 사용
    - 인덱싱 이후 쿼리

        ```bash
        mysql> select * from article where board_id = 1 order by article_id desc limit 30 offset 90;
        30 rows in set (0.01 sec)
        ```

    - 실행계획

        ```bash
        mysql> explain select * from article where board_id = 1 order by article_id desc limit 30 offset 90;
        +----+-------------+---------+------------+------+-------------------------+-------------------------+---------+-------+---------+----------+-------+
        | id | select_type | table   | partitions | type | possible_keys           | key                     | key_len | ref   | rows    | filtered | Extra |
        +----+-------------+---------+------------+------+-------------------------+-------------------------+---------+-------+---------+----------+-------+
        |  1 | SIMPLE      | article | NULL       | ref  | idx_board_id_article_id | idx_board_id_article_id | 8       | const | 6471862 |   100.00 | NULL  |
        +----+-------------+---------+------------+------+-------------------------+-------------------------+---------+-------+---------+----------+-------+
        1 row in set, 1 warning (0.01 sec)
        ```

        - 쿼리에 인덱스 사용
- 한계점 - 인덱스 사용
    - 뒤쪽의 데이터일수록 성능 저하 발생

        ```bash
        mysql> select * from article where board_id = 1 order by article_id desc limit 30 offset 1499970;
        30 rows in set (17.02 sec)
        ```

    - why?
        - InnoDB : MySQL의 기본 스토리지 엔진(데이터 저장 및 관리 장치)
        - InnoDB는 테이블마다 PK를 기준(정확한 규칙이 존재하나 일반적으로 PK)으로 Clustered Index를 자동 생성
            - Clustered Index는 leaf node로 행 데이터를 가짐
            - 따라서, PK를 통한 조회는 자동 생성된 Clustered Index를 통해 수행
        - 대량의 데이터에서도 임의의 PK로 조회시에는 빠른 성능

            ```bash
            mysql> select * from article where article_id = 216888757156524034;
            +--------------------+--------+----------+----------+-----------+---------------------+---------------------+
            | article_id         | title  | content  | board_id | writer_id | created_at          | modified_at         |
            +--------------------+--------+----------+----------+-----------+---------------------+---------------------+
            | 216888757156524034 | title2 | content2 |        1 |         1 | 2025-08-21 20:58:29 | 2025-08-21 20:58:29 |
            +--------------------+--------+----------+----------+-----------+---------------------+---------------------+
            1 row in set (0.00 sec)
            ```

            ```bash
            mysql> explain select * from article where article_id = 216888757156524034;
            +----+-------------+---------+------------+-------+---------------+---------+---------+-------+------+----------+-------+
            | id | select_type | table   | partitions | type  | possible_keys | key     | key_len | ref   | rows | filtered | Extra |
            +----+-------------+---------+------------+-------+---------------+---------+---------+-------+------+----------+-------+
            |  1 | SIMPLE      | article | NULL       | const | PRIMARY       | PRIMARY | 8       | const |    1 |   100.00 | NULL  |
            +----+-------------+---------+------------+-------+---------------+---------+---------+-------+------+----------+-------+
            1 row in set, 1 warning (0.01 sec)
            ```

            - why?
                - key PRIMARY : InnoDB가 자동으로 생성한 Clustered Index를 사용했다는 뜻
        - Secondary Index(보조 인덱스) : Clustered Index 외의 생성한 인덱스
            - Non-clustered Index
            - index의 leaf node로 실제 데이터는 없고, 인덱스 컬럼 데이터와 데이터 접근을 위한 포인터(PK)를 보유
                - 실제 데이터는 Clustered Index로 접근
        - Clustered Index vs. Secondary Index
             ```markdown
             | 구분   | Clustered Index (클러스터드 인덱스)      | Secondary Index (보조 인덱스)           |
             |--------|-----------------------------------------|-----------------------------------------|
             | 생성   | 테이블 PK로 자동생성                    | 테이블의 컬럼으로 직접 생성              |
             | 데이터 | 행 데이터                               | 데이터 접근 포인터, 인덱스 컬럼 데이터   |
             | 개수   | 테이블 당 1개                           | 필요에 따라 여러 개 생성 가능            |
             ```
        - Secondary Index를 통한 조회 과정
          1. Secondary Index를 통해 인덱스 컬럼 데이터와 포인터(PK)를 조회
          2. 포인터(PK)를 통해 Clustered Index를 조회하여 실제 행 데이터 조회
          3. 결과 반환 
        - Secondary Index를 통한 조회는 2번의 조회 과정이 필요하므로 성능 저하 발생
        - offset 1499970 조회 과정
          - (board_id, article_id) 인덱스를 통한 Secondary Index 조회하여 article_id 조회
          - Clustered Index를 통해 article_id에 해당하는 행 데이터 조회
          - offset 1499970에 해당하는 행 데이터까지 반복하며 skip
          - limit 30개 추출
- 해결방법 - board_id와, article_id만 추출하여 해당하는 데이터만 clustered index를 통해 조회
    - 쿼리

        ```bash
        mysql> select board_id, article_id from article where board_id = 1 order by article_id desc limit 30 offset 1499970;
        30 rows in set (0.68 sec)
        ```
      - 실행계획

          ```bash
          mysql> explain select board_id, article_id from article where board_id = 1 order by article_id desc limit 30 offset 1499970;
          +----+-------------+---------+------------+------+-------------------------+-------------------------+---------+-------+---------+----------+-------------+
          | id | select_type | table   | partitions | type | possible_keys           | key                     | key_len | ref   | rows    | filtered | Extra       |
          +----+-------------+---------+------------+------+-------------------------+-------------------------+---------+-------+---------+----------+-------------+
          |  1 | SIMPLE      | article | NULL       | ref  | idx_board_id_article_id | idx_board_id_article_id | 8       | const | 6471862 |   100.00 | Using index |
          +----+-------------+---------+------------+------+-------------------------+-------------------------+---------+-------+---------+----------+-------------+
          1 row in set, 1 warning (0.01 sec)
          ```
          - Using index: 인덱스만 사용하여 조회
            - Covering Index: 인덱스 컬럼만으로 조회가 가능하여 실제 행 데이터 조회 없이 인덱스(Secondary Index)만으로 결과 반환
          ```bash
            mysql> select * from (select article_id from article where board_id = 1 order by article_id desc limit 30 offset 1499970) t left join article on t.article_id = article.article_id;
            30 rows in set (0.74 sec)
          ```
        - 무의미한 데이터 접근 생략 -> 성능 향상
        - 완전한 해결은 아님 -> secondary index만 탄다고 하더라도 offset이 커질수록 성능 저하 발생
        ```bash
        mysql> select * from (select article_id from article where board_id = 1 order by article_id desc limit 30 offset 8999970) t left join article on t.article_id = article.article_id;
        30 rows in set (10.19 sec)
        ```
        - 해결방법
          1. 테이블 분리
             - 데이터를 한번 더 분리(예를 들어, 1년 단위로 분리 등)
             - 1년 동안 작성된 게시글 단위로 즉시 skip 가능
             - 어플리케이션 레벨에서 추가적인 처리 필요
          2. 정책적으로 풀어내기
             - 과연 일반적인 사용자가 10000 페이지 이상을 조회할까? - 어뷰저 아님? -> 페이지 조회 제한
             - 시간 범위, 텍스트 검색 기능 등 페이징 집합의 크기를 줄이는 방법
          3. 무한 스크롤 : 커서 기반
             - 아무리 뒷페이지로 가더라도 균등한 조회 속도
        