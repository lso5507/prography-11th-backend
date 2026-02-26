# prography-11th-backend

Prography 11기 출결관리 백엔드 과제 구현 프로젝트입니다.

## 기술 스택

- Java 17
- Spring Boot 3.4.x
- Spring Web / Spring Data JPA / Validation
- H2 Database (in-memory)
- JUnit5 + MockMvc (단위/통합/시나리오 테스트)

## 실행 방법

### 1) 로컬 실행

```bash
mvn spring-boot:run
```

기본 Base URL:

```text
http://localhost:8080/api/v1
```

### 2) 패키징

```bash
mvn package
```

### 3) 테스트 실행

```bash
mvn test
```

## API 테스트 가이드

### 1) `.http` 파일 기반 API 테스트 (IntelliJ / VSCode)

- 파일: `http/api-scenarios.http`
- 목적: 명세 기준 25개 API를 HTTP 요청으로 순차 검증
- 실행 (IntelliJ): `.http` 파일 열고 요청별 실행 또는 `Run All Requests in File`
- 실행 (VSCode): `REST Client` 확장 설치 후 `.http` 파일에서 `Send Request` 또는 `Run All`
- 포함 변수: `cohortId`, `partId`, `teamId`, `memberId`, `sessionId`, `qrCodeId`, `qrHash`, `attendanceId`, `cohortMemberId`

참고:

- `15 QR 생성`은 일정 생성 시 이미 활성 QR이 있으면 `409(QR_ALREADY_ACTIVE)`가 발생할 수 있습니다. 정책상 정상 동작입니다.

### 2) JUnit 테스트

- 실행 명령: `mvn test`
- 구성
  - 시나리오 테스트: `src/test/java/com/prography/backend/ApiScenarioTest.java` (25개 API)
  - 단위 테스트
    - `src/test/java/com/prography/backend/unit/auth/AuthServiceTest.java`
    - `src/test/java/com/prography/backend/unit/member/MemberServiceTest.java`
    - `src/test/java/com/prography/backend/unit/session/SessionServiceTest.java`
    - `src/test/java/com/prography/backend/unit/attendance/AttendanceServiceTest.java`
    - `src/test/java/com/prography/backend/unit/deposit/DepositServiceTest.java`
  - 통합 테스트
    - `src/test/java/com/prography/backend/integration/auth/AuthControllerIntegrationTest.java`
    - `src/test/java/com/prography/backend/integration/member/MemberControllerIntegrationTest.java`
    - `src/test/java/com/prography/backend/integration/cohort/CohortControllerIntegrationTest.java`
    - `src/test/java/com/prography/backend/integration/session/SessionControllerIntegrationTest.java`
    - `src/test/java/com/prography/backend/integration/attendance/AttendanceControllerIntegrationTest.java`
    - `src/test/java/com/prography/backend/integration/deposit/DepositControllerIntegrationTest.java`

## 시드 데이터

애플리케이션 시작 시 자동으로 아래 데이터가 초기화됩니다.

- 기수: 10기, 11기(현재 운영 기수)
- 파트: 기수별 SERVER, WEB, iOS, ANDROID, DESIGN
- 팀: 11기 Team A, Team B, Team C
- 관리자 계정: `admin` / `admin1234`
- 관리자 초기 보증금: 100,000원

## 주요 정책 반영 사항

- 회원 탈퇴는 soft-delete(`WITHDRAWN`) 처리
- 일정 삭제는 soft-delete(`CANCELLED`) 처리
- 일정 생성 시 QR 자동 생성
- QR 갱신 시 기존 QR 만료 + 신규 QR 생성
- QR 출석 체크 시 검증 순서/에러코드 반영
- 패널티 자동 계산/차감 및 DepositHistory 기록
- 출결 수정 시 패널티 차액만큼 보증금 자동 조정
- 공결(EXCUSED) 기수당 최대 3회 제한

## API 구현 범위

- 필수 API 16개 구현
- 가산점 API 9개 구현
- 총 25개 API 구현

## 테스트 커버리지 요약

- API 시나리오(25개): 로그인, 회원, 기수, 일정, QR, 출결, 보증금 이력
- 단위 테스트: Auth/Member/Session/Attendance/Deposit 도메인 서비스 핵심 로직
- 통합 테스트: Auth/Member/Cohort/Session/Attendance/Deposit 컨트롤러 엔드투엔드 흐름

## 문서

- ERD: `docs/ERD.md`
- 시스템 아키텍처: `docs/System-Architecture.md`
- AI 사용 사례: `docs/AI-Usage.md`
