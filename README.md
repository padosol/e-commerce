# e-commerce
1. 아키텍처 및 레이어별 책임
   - 레이어드 아키텍처
   - controller layer, business layer, persistence layer
   - controller layer: API 스펙 담당, 클라이언트와 요청 및 응답을 맡고 있으며, 요청을 business layer 에게 위임한다.
   - business layer: 비즈니스 로직을 담당하는 레이어이다. 영속성 레이어와 직접 소통이 가능하다.
   - persistence layer: 데이터를 저장 또는 읽기 작업을 하는 레이어. 복잡한 비즈니스 로직이 존재하지 않는다. 

2. 모듈화

3. 테스트 코드

4. 배포 자동화

5. 성능 테스트

6. 로깅 모니터링

7. 메트릭 모니터링

8. 고가용성 및 재해 복구
