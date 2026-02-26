# System Design Architecture

## 1) 전체 구조

```mermaid
flowchart TB
    subgraph Clients[Clients]
        C1[Admin Client]
        C2[Member Client]
        C3[.http/MockMvc Tests]
    end

    subgraph App[Spring Boot Application]
        subgraph API[API Layer]
            A1[AuthController]
            A2[MemberController]
            A3[SessionController]
            A4[AttendanceController]
            A5[Admin* Controllers]
        end

        subgraph Service[Service Layer]
            S1[AuthService]
            S2[MemberService]
            S3[SessionService]
            S4[QrCodeService]
            S5[AttendanceService]
            S6[DepositService]
            S7[CohortService]
        end

        subgraph Domain[Domain/Persistence]
            D1[Entities]
            D2[Spring Data JPA Repositories]
            D3[H2 Database]
        end

        subgraph Cross[Cross-Cutting]
            X1[GlobalExceptionHandler]
            X2[ErrorCode]
            X3[ApiResponse success/data/error]
            X4[DataInitializer]
        end
    end

    C1 --> API
    C2 --> API
    C3 --> API

    API --> Service
    Service --> D2
    D2 --> D3
    D1 --> D2

    API --> X3
    Service --> X2
    API --> X1
    X4 --> D3
```

## 2) 핵심 도메인 흐름 (출석 체크)

```mermaid
sequenceDiagram
    participant Client
    participant AttendanceController
    participant AttendanceService
    participant QrCodeService
    participant SessionService
    participant MemberService
    participant DepositService
    participant DB

    Client->>AttendanceController: POST /api/v1/attendances (hashValue, memberId)
    AttendanceController->>AttendanceService: checkIn(request)
    AttendanceService->>QrCodeService: findByHash(hashValue)
    AttendanceService->>SessionService: validate session status(IN_PROGRESS)
    AttendanceService->>MemberService: validate member status
    AttendanceService->>DB: find attendance duplicate(session, member)
    AttendanceService->>AttendanceService: calculate status/penalty
    alt penalty > 0
        AttendanceService->>DepositService: applyPenalty(...)
    end
    AttendanceService->>DB: save Attendance
    AttendanceService-->>AttendanceController: AttendanceResponse
    AttendanceController-->>Client: 201 Created
```

## 3) 설계 원칙

- API 계약은 명세 기반으로 유지하고, 모든 응답을 `ApiResponse(success/data/error)`로 통일합니다.
- 컨트롤러는 입출력/검증에 집중하고, 비즈니스 규칙은 서비스 계층에 모아 테스트 가능성을 높입니다.
- 출결/패널티/보증금 변경은 트랜잭션으로 처리해 정합성을 보장합니다.
- 에러는 `ErrorCode`와 전역 예외 처리로 HTTP 상태코드와 메시지를 일관되게 반환합니다.
