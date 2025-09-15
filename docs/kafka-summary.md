### Kafka 요약

- 대규모 데이터의 실시간 처리를 위한 분산 이벤트 스트리밍 플랫폼
- 고성능, 확정성, 내구성, 가용성
- 주요 구성 요소:
  - **Producer**: 데이터를 Kafka로 전송하는 클라이언트 애플리케이션
  - **Consumer**: Kafka에서 데이터를 읽는 클라이언트 애플리케이션
  - **Consumer Group**: 여러 Consumer가 협력하여 메시지를 처리하는 그룹
  - **Broker**: Kafka 서버, 메시지를 저장하고 관리
  - **Cluster**: 여러 Broker의 집합, 확장성과 내결함성 제공
  - **Topic**: 메시지가 카테고리별로 분류되는 논리적 채널
  - **Partition**: Topic 내에서 메시지를 분산 저장하는 단위, 병렬 처리 가능
  - **Offset**: Partition 내에서 메시지의 고유 위치를 나타내는 번호, Consumer Group 별로 관리, 동일한 데이터를 병렬로 처리 가능
  - **Zookeeper**: Kafka 클러스터의 메타데이터 관리 및 분산 동기화 도구
  - **KRaft**: Kafka의 자체 메타데이터 관리 시스템, Zookeeper 의존성 제거 및 대체, 더욱 간단한 구성과 관리 제공
- 활용 예정 사항
  - Producer: Article, Comment, Like, View
  - event: 게시글 생성, 댓글 생성, 좋아요, 좋아요 취소, 조회수 증가 등
  - Consumer: Hot Article, Article Read 

#### Kafka 환경 구축 - Docker 사용 1대 KRaft 모드
```bash
docker run -d --name seock1000-board-kafka -p 9092:9092 apache/kafka:3.8.0
docker exec --workdir /opt/kafka/bin -it seock1000-board-kafka sh
./kafka-topics.sh --bootstrap-server localhost:9092 --create --topic seock1000-board-article --replication-factor 1 --partitions 3
./kafka-topics.sh --bootstrap-server localhost:9092 --create --topic seock1000-board-comment --replication-factor 1 --partitions 3
./kafka-topics.sh --bootstrap-server localhost:9092 --create --topic seock1000-board-like --replication-factor 1 --partitions 3
./kafka-topics.sh --bootstrap-server localhost:9092 --create --topic seock1000-board-view --replication-factor 1 --partitions 3
```