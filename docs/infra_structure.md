![infra_structure.png](images/infra_structure.png)

### ☁️ Infrastructure & Client
* **Amazon EC2 (Elastic Compute Cloud)**
    * 전체 애플리케이션 인프라를 호스팅하는 클라우드 가상 가상 서버입니다.
    * 요구사항 및 트래픽 변화에 따라 유연하게 스펙을 확장할 수 있는 컴퓨팅 환경을 제공합니다.
* **Users**
    * 웹 브라우저나 모바일 앱을 통해 서비스에 접근하는 최종 클라이언트 레이어입니다.

### 🌐 Routing & Containerization
* **NGINX (Reverse Proxy & Load Balancer)**
    * **라우팅 및 부하 분산:** 외부 요청을 안전하게 수신하여 내부에서 구동 중인 3개의 Spring Boot 인스턴스로 트래픽을 고르게 분산(Load Balancing)합니다.
    * **보안 및 성능 최적화:** HTTPS 암호화를 처리하는 SSL/TLS 종료 역할을 수행하며, HTML/CSS/JS 등 정적 리소스를 앞단에서 빠르게 서빙하여 백엔드 서버의 부하를 줄입니다.
* **Docker**
    * Spring Boot 애플리케이션 및 각 인프라 요소를 개별 컨테이너로 패키징하여, 호스트 OS 및 다른 프로세스로부터 분리된 독립적이고 일관된 실행 환경(격리성)을 보장합니다.

### ⚙️ Application & Storage
* **Spring Boot (Application Server)**
    * 서비스의 핵심 비즈니스 로직을 실행하고, 클라이언트의 다양한 비즈니스 요청을 처리하는 REST API 서버 역할을 담당합니다.
* **Redis (In-Memory Cache & Queue)**
    * **분산 세션 관리:** 다중 Spring Boot 인스턴스 환경에서 사용자가 어떤 서버로 접속하더라도 상태를 유지할 수 있도록 세션을 공유·관리합니다.
    * **고속 데이터 처리:** 자주 조회되는 데이터를 메모리에 위치시켜 조회 성능을 높이고, 콘서트 예약 등의 대량 트래픽 상황에서 동시성 제어를 위한 **대기열 토큰** 및 **좌석 임시 선점(Held Seats) 정보**를 실시간으로 관리합니다.
* **MySQL (Persistent Storage)**
    * 시스템의 메인 데이터베이스로서 데이터의 유실이 없어야 하는 영구 저장소 역할을 합니다.
    * 유저 정보, 예약 내역, 결제 정보 등 영속성이 보장되어야 하는 구조화된 핵심 데이터를 관계형 데이터 모델(RDB)로 안전하게 관리합니다.
* **Apache Kafka (Event Streaming)**
    * 실시간 데이터 전송 및 비동기 이벤트 처리를 위한 분산 메시징 플랫폼입니다.
    * 예약 완료, 결제 처리 등 즉각적인 응답이 필요 없는 무거운 비즈니스 로직을 비동기 이벤트로 발행하여 서버 간의 결합도를 낮추고 후행 프로세스를 안정적으로 처리합니다.