# 콘서트 예약 서비스 API

---


## 공통 인증/인가 명세

본 API는 JWT 기반 인증을 사용합니다.<br/>
인증이 필요한 API 호출 시 HTTP Header에 아래와 같이 토큰을 포함해야 합니다.
`Authorization: Bearer {Access_Token}`

|코드| HTTP Stattus | 설명 |
| :---| :---: | :--- |
| UNAUTHORIZED | 401 | 토큰이 누락되었거나 만료됨 |
|FORBIDDEN | 403 | 토큰은 유효하지만 해당 리소스에 대한 권한이 없음 |


---

## 공통 응답 구조

모든 API 응답은 아래의 공통 구조를 따릅니다.

### 성공 응답
| 필드 | 타입 | 필수 | 설명                 |
|------|------|----|--------------------|
|success|boolean| O  | API호출 성공 여부:`true` |
|data|object| O  | API별 응답 데이터        |

```json
{
  "success": true,
  "data": {
    // API별 응답 데이터
  }
}
```

### 실패 응답

|필드|타입|필수| 설명                  |
|----|----|----|---------------------|
|success|boolean|O| API호출 성공 여부:`false` |
| error | object |O |에러 정보|
|error.code | string | O | 에러 코드|
|error.message|string|O|에러 상세 메시지|

```json
{
  "success": false,
  "error" : {
    "code" : "ERROR_CODE",
    "message" : "에러 상세 메시지"
  }
}
```

---

## 공통 에러 코드

### HTTP 상태 코드별 에러

| 코드                    | HTTP Status | 설명                 |
|-----------------------|:-----------:|--------------------|
| INVALID_PARAMETER     |     400     | 잘못된 파라미터           |
| MISSING_PARAMETER     |     400     | 필수 파라미터 누락         |
| UNAUTHORIZED          |     401     | 인증 실패 (로그인 필요)     |
| FORBIDDEN             |     403     | 해당 리소스에 권한 없음      |
| NOT_FOUND             |     404     | 리소스를 찾을 수 없음       |
| CONFLICT              |     409     | 리소스 충돌             |
| GONE                  |     410     | 리소스가 있었으나 사라짐 (만료) |
| INTERNAL_SERVER_ERROR |     500     | 서버 내부 오류           |

---
## 1️⃣ 예약 가능 날짜 조회

* **Method:** GET

* **URL:** `/concerts/{concertId}/schedules`

* **Path Parameters:**

  | 이름 | 타입 | 필수 | 설명 |
  |------|------|:--:|------|
  | concertId | string | Ｏ  | 콘서트 고유 ID |

* **Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "schedules": [
      { "schedule_id": 101, "date": "2025-12-01" },
      { "schedule_id": 102, "date": "2025-12-02" }
    ]
  }
}
```

* **Error Response (ex. 404 Not Found):**

```json
{
  "success": false,
  "error": {
    "code": "CONCERT_NOT_FOUND",
    "message": "해당 콘서트를 찾을 수 없습니다."
  }
}
```

---

## 2️⃣ 특정 날짜 좌석 조회

* **Method:** GET

* **URL:** `/concerts/{scheduleId}/seats`

* **Path Parameters:**

  | 이름 | 타입 | 필수 | 설명           |
  |------|------|:--:|--------------|
  | scheduleId | string | O  | 콘서트 일정 고유 ID |


* **Response (ex. 200 OK):**
* **status enum:** `available`, `held`, `sold`
```json
{
  "success": true,
  "data": {
    "date": "2025-12-01",
    "seats": [
      {"seat_number": 1, "status": "available"},
      {"seat_number": 2, "status": "held"},
      {"seat_number": 3, "status": "sold"},
      ...
    ]
  }
}
```
* **Error Response (ex. 404 Not Found):**

```json
{
  "success": false,
  "error": {
    "code": "CONCERT_NOT_FOUND",
    "message": "해당 콘서트를 찾을 수 없습니다."
  }
}
```

---

## 3️⃣ 좌석 임시 배정 요청  (인증 필요 🔒)

* **Method:** POST

* **URL:** `/concerts/{scheduleId}/reservations`

* **Headers:**
  * `Authorization: Bearer {Access_Token}`

* **Path Parameters:**

  | 이름 | 타입 | 필수 | 설명 |
  |------|------|:--:|------|
  | scheduleId | string | O  | 콘서트 고유 ID |

* **Request Body:**

```json
{
  "user_id": "user_123",
  "seat_number": 10
}
```

* **Response (ex. 201 Created):**

```json
{
  "success": true,
  "data": {
    "reservation_id": "reservation_7654",
    "status": "held",
    "date": "2025-12-22",
    "seat_number": 10,
    "hold_expire_time": "2025-12-01T09:05:00Z"
  }
}
```

* **Error Response (409 Conflict):**

```json
{
  "success": false,
  "error": {
    "code": "SEAT_ALREADY_HELD",
    "message": "해당 좌석은 이미 임시 배정되었습니다."
  }
}
```

* **Error Response (400 Bad Request):**

```json
{
  "success": false,
  "error": {
    "code": "SEAT_NOT_AVAILABLE",
    "message": "해당 좌석은 예약할 수 없습니다."
  }
}
```

---

## 4️⃣ 사용자 포인트 관리

### 4-1. 포인트 충전

* **Method:** PATCH

* **URL:** `/users/{userId}/points`

* **Path Parameters:**

  | 이름 | 타입 | 필수 | 설명 |
  |------|------|:--:|------|
  | userId | string | O  | 사용자 ID |

* **Request Body:**

```json
{
  "amount": 100000
}
```

* **Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "charged_amount": 100000,
    "total_points": 130000
  }
}
```

* **Error Response (ex. 400 Bad Request):**

```json
{
  "success": false,
  "error": {
    "code": "INVALID_AMOUNT",
    "message": "충전 금액은 0보다 커야 합니다."
  }
}
```

### 4-2. 포인트 조회

* **Method:** GET

* **URL:** `/users/{userId}/points`

* **Path Parameters:**

  | 이름 | 타입 | 필수 | 설명 |
  |------|------|:--:|------|
  | userId | string | O  | 사용자 ID |

* **Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "points": 130000
  }
}
```

* **Error Response (ex. 404 Not Found):**

```json
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "해당 사용자를 찾을 수 없습니다."
  }
}
```

---

## 5️⃣ 결제 요청

* **Method:** POST

* **URL:** `/reservations/{reservationId}/payments`

* **Path Parameters:**

  | 이름 | 타입 | 필수 | 설명 |
  |------|------|:--:|------|
  | reservationId | string | O  | 예약 ID |

* **Response (201 Created):**

```json
{
  "success": true,
  "data": {
    "payment_id": "payment_1234",
    "amount": 90000,
    "status": "completed"
  }
}
```

* **Error Response (400 Bad Request):**

```json
{
  "success": false,
  "error": {
    "code": "INSUFFICIENT_POINTS",
    "message": "포인트가 부족합니다. 현재 잔액: 50000"
  }
}
```

* **Error Response (410 Gone):**

```json
{
  "success": false,
  "error": {
    "code": "RESERVATION_EXPIRED",
    "message": "임시 배정 시간이 만료되었습니다."
  }
}
```

---

## 6️⃣ 유저 대기열 토큰

### 6-1. 대기열 토큰 발급

* **Method:** POST

* **URL:** `/queues`

* **Request Body:**

```json
{
  "user_id": "user_123"
}
```

* **Response (201 Created):**

```json
{
  "success": true,
  "data": {
    "queue_token": "jwt_token_string",
    "status": "WAIT",
    "waiting_order": 1234
  }
}
```

* **Error Response (409 Conflict):**

```json
{
  "success": false,
  "error": {
    "code": "ALREADY_IN_QUEUE",
    "message": "이미 대기열에 등록된 사용자입니다."
  }
}
```

### 6-2. 대기번호 조회

* **Method:** GET

* **URL:** `/queues/{userId}`

* **Headers:**
    * `Authorization: Bearer {Access_Token}`

* **Path Parameters:**

  | 이름 | 타입 | 필수 | 설명 |
  |------|------|:----:|------|
  | userId | string | ✅ | 사용자 ID |

* **Response (200 OK): 아직 대기 중일 때**
```json
{ "success": true, 
  "data": 
  { 
    "status": "WAIT", 
    "waiting_order": 142
  }
}
```
* **Response (200 OK): 활성화 상태로 진입했을 때 (이제 예약 가능)**
```
{ 
    "success": true, 
    "data": 
    { 
        "status": "ACTIVE",
        "waiting_order": 0 
    } 
}
```

* **Error Response (404 Not Found):**

```json
{
  "success": false,
  "error": {
    "code": "USER_NOT_IN_QUEUE",
    "message": "대기열에 등록되지 않은 사용자입니다."
  }
}
```

---

## ✅ Notes

* 모든 좌석은 1~50번으로 관리
* 좌석 임시 배정 시간: 5분 (`status: held`)
* 포인트 충전/결제 연동
* RESTful 설계: Path는 리소스 중심, 동사는 HTTP 메소드로 표현
* 민감 데이터(포인트, 결제)는 Request Body로 전달
* 모든 응답은 공통 응답 구조(`success`, `data` 또는 `error`)를 따름
