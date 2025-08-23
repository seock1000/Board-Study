### 무한 스크롤

- 무한 스크롤에서는 페이징 방식의 쿼리 사용 X
  - 같은 쿼리(offset 기반) 사용 시 데이터 중복/누락 발생 가능성 : 조회 중 새로운 데이터 삽입/삭제 시
  
- 커서 기반 쿼리 사용
  - 마지막으로 불러온 데이터를 기준점으로 사용
    - 조회 중 삽입/삭제가 발생해도 기준점 이후의 데이터는 변하지 않음
    - DB에서 인덱스를 통해 로그시간에 기준점으로 접근이 가능하여 아무리 뒷페이지로 가더라도 균등한 속도 유지 가능(offset 만큼 scan 하는 과정 X)
- 쿼리
  - 첫페이지 : 기준점이 없으므로 기준점 조건 없이 limit 만 사용, 커버링 인덱스 활용됨
    ```bash
      mysql> select * from article where board_id = 1 order by article_id limit 30;
      30 rows in set (0.05 sec)
    ```
    ```
    mysql> explain select * from article where board_id = 1 order by article_id desc limit 30;
    +----+-------------+---------+------------+-------+-------------------------+---------+---------+------+------+----------+-------------+
    | id | select_type | table   | partitions | type  | possible_keys           | key     | key_len | ref  | rows | filtered | Extra       |
    +----+-------------+---------+------------+-------+-------------------------+---------+---------+------+------+----------+-------------+
    |  1 | SIMPLE      | article | NULL       | index | idx_board_id_article_id | PRIMARY | 8       | NULL |   60 |    10.00 | Using where |
    +----+-------------+---------+------------+-------+-------------------------+---------+---------+------+------+----------+-------------+
    1 row in set, 1 warning (0.01 sec)
    ```
  - 이후 페이지 : 마지막으로 불러온 데이터의 article_id 를 기준점으로 사용, 기준점 기반 균등하게 빠른 속도 유지 가능
    ```bash
    mysql> select * from article where board_id = 1 and article_id < 216890211397218358 order by article_id desc limit 30;
    30 rows in set (0.00 sec)
    ```