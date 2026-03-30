<div align="center">

<img width="866" height="696" alt="Image" src="https://github.com/user-attachments/assets/8377836b-fe79-44f3-bde6-a9baf019bdac" />

# 🚀 파일 변환 서비스
**이미지(PNG, JPG, BMP)를 PDF로 간편하게 변환하세요.**

---

### 📊 실시간 서비스 현황
| 누적 변환 건수 | 누적 변환 용량 |
| :---: | :---: |
| **7건** | **37.1 KB** |

</div>

## 📖 서비스 소개
- 회원가입없이 사용자의 이미지 파일을 PDF 문서로 변환하여 실시간으로 신속하고 안전하게 전달합니다.
- 이미지 파일 변환 목적만을 가진 사용자를 위한 별도의 설정없는 직관적인 UI 를 통해 누구나 쉽게 이용 가능합니다.
- 변환 건에 대한 내역이 실시간으로 통계에 누적반영되어 서비스의 신뢰성을 보장합니다.
---

## 1.개발 환경
[Backend]
- Language: Java 17
- Framework: Spring Boot
- Database: MySQL, Redis
- Event Data: Debezium (CDC)
- Message Broker: RabbitMQ, Redis
- Real-time: SSE (Server-Sent Events)
- Container: Docker
- Monitoring: Prometheus, Grafana
- Test: Artillery (Load Test)
- Alarm: Slack API


[Front]
- View Engine: Thymeleaf 
- Styling: Bootstrap 5 (CDN), Custom CSS (Dark Theme) 
- Scripting: Vanilla JavaScript (ES6+) 
- Real-time: Server-Sent Events (SSE)

---

## 2.개발 기술의 채택 이유와 사용사례

- #### RabbitMQ
  - 파일 변환은 CPU 와 시간이 많이 소요되므로 사용자가 브라우저를 붙잡고 있지않도록 비동기적으로 처리했습니다.
  - API Server,RabbitMQ,CDC 를 개별적으로 분리하여 한 곳에서 문제가 생겨도 데이터가 소실되는 경우를 방지했습니다.
  - 만약 워커 서버에서 변환에 실패하는 경우(변환 불가 포맷,네트워크 지연 등등) 3번까지 재시도하며 DeadLetterQueue 로 이관되며 설정된 Slack Webhook 으로 알림을 받을 수 있습니다.

- #### Redis, SSE
  - 파일이 변환되는 과정에서 진행률, 변환 상태를 사용자가 직접 조회를 하지않고 실시간으로 신속하게 확인해야하므로 Redis 의 메세지 브로커를 통해 상태를 전송하고 구독된 유저 식별자를 대상으로 파일 이름을 매칭하여 실시간 상태 업데이트를 했습니다.
  - SSE 사용자가 페이지로 진입할 때 유저 식별자를 SSE 로 넘겨 정해진 시간동안 실시간으로 최신 데이터를 수신받을 수 있습니다.

- #### Debezium & Docker
  - 신규 변환 요청을 파악하기 위한 스케줄러 기반 방식은 주기적인 Polling으로 인해 데이터베이스에 지속적인 부하를 주고 실시간성이 떨어지는 단점이 있는 반면 Debezium은 DB의 트랜잭션 로그를 직접 읽어 변경 사항을 감지하므로 서비스 로직과 DB 간의 결합도를 낮추고 성능 저하 없이 이벤트를 발행할 수 있습니다.
  - 사용자가 변환 요청을 하면 별도의 log 감지용 전용 테이블에 요청이 생성되고 파싱된 데이터를 RabbitMQ 에 전송합니다.
  - Docker 를 통해 독립적으로 서버를 구성하여 장애 격리 및 유지보수 편의성 증대시켰습니다.

- #### Monitoring + Data Visualization (Prometheus + Grafana)
   - 변환 서비스의 CPU 집약적 작업은 서버 자원 사용량에 대한 모니터링이 필수적이므로 Prometheus의 Pull 방식의 수집 구조를 통해 추후 확장하더라도 워커 서버들의 통합 관리가 가능하며 범용적인 데이터 소스를 지원하는 그라파나로 시각화 대시보드를 구성했습니다.
   - 워커 서버가 RabbitMQ의 메세지를 변환하는 과정에서의 CPU 점유율 및 메모리 점유율을 확인하여 특정 임계치를 넘어서는 시점을 파악하고 인프라 확장의 근거 데이터로 활용했습니다.
 
- #### Artillery
   - 실제 운영환경에서 여러 사용자가 동시에 파일을 업로드했을 때 API Server 가 무너지지않고 성공적으로 비동기 처리를 할 수 있는지 검증하기 위해 도입했습니다.
   - 임의로 총 4단계의 가상의 시나리오를 작성하여 동시 요청수를 높여가며 API Server 로 요청을 보내 Debezium 이 정상적으로 메세지를 Publishing(Trigger) 하는지 확인해볼 수 있었습니다.

---

## 3.시퀀스 다이어그램(UML)

<div align="center">
<img width="1121" height="522" alt="file-conversion-uml drawio" src="https://github.com/user-attachments/assets/e383735f-df02-4038-9e9c-4ab19cee81a9" />

---






