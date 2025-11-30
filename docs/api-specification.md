# 콘서트 예약 서비스 API

##### Version: 1.0.0

---

## 1️⃣ 예약 가능 날짜 조회

* **Method:** GET

* **URL:** `/concerts/{concertId}/dates`

* **Path Parameters:**

  | 이름        | 타입     | 필수 | 설명        |
    | --------- | ------ | -- | --------- |
  | concertId | string | ✅  | 콘서트 고유 ID |

* **Response (200 OK):**

```json
{
  "dates": ["2025-12-01", "2025-12-02", "2025-12-03"]
}
```

---

## 2️⃣ 특정 날짜 좌석 조회

* **Method:** GET

* **URL:** `/concerts/{concertId}/dates/{date}/seats`

* **Path Parameters:**

  | 이름        | 타입                  | 필수 | 설명        |
    | --------- | ------------------- | -- | --------- |
  | concertId | string              | ✅  | 콘서트 고유 ID |
  | date      | string (YYYY-MM-DD) | ✅  | 조회할 날짜    |

* **Response (200 OK):**

```json
{
  "date": "2025-12-01",
  "seats": [
    {"seat_number": 1, "status": "available"},
    {"seat_number": 25, "status": "held"},
    {"seat_number": 50, "status": "sold"}
  ]
}
```

* **status enum:** `available`, `held`, `sold`

---

## 3️⃣ 좌석 임시 배정 요청

* **Method:** POST

* **URL:** `/concerts/{concertId}/reservations`

* **Path Parameters:**

  | 이름        | 타입     | 필수 | 설명        |
    | --------- | ------ | -- | --------- |
  | concertId | string | ✅  | 콘서트 고유 ID |

* **Request Body:**

```json
{
  "user_id": "user_123",
  "date": "2025-12-22",
  "seat_number": 10
}
```

* **Response (201 Created):**

```json
{
  "reservation_id": "reservation_7654",
  "status": "held",
  "date": "2025-12-22",
  "seat_number": 10,
  "hold_expire_time": "2025-12-01T09:05:00Z"
}
```

---

## 4️⃣ 사용자 포인트 관리

### 4-1. 포인트 충전

* **Method:** PATCH

* **URL:** `/users/{userId}/points`

* **Path Parameters:**

  | 이름     | 타입     | 필수 | 설명     |
    | ------ | ------ | -- | ------ |
  | userId | string | ✅  | 사용자 ID |

* **Request Body:**

```json
{
  "amount": 100000
}
```

* **Response (200 OK):**

```json
{
  "charged_point": 100000,
  "points": 130000
}
```

### 4-2. 포인트 조회

* **Method:** GET
* **URL:** `/users/{userId}/points`
* **Response (200 OK):**

```json
{
  "points": 130000
}
```

---

## 5️⃣ 결제 요청

* **Method:** POST

* **URL:** `/reservations/{reservation_id}/payments`

* **Path Parameters:**

  | 이름             | 타입     | 필수 | 설명    |
    | -------------- | ------ | -- | ----- |
  | reservation_id | string | ✅  | 예약 ID |

* **Request Body:**

```json
{
  "amount": 90000
}
```

* **Response (201 Created):**

```json
{
  "payment_id": "payment_1234"
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
  "user_id": "user_123",
  "waiting_order": 33
}
```

### 6-2. 대기번호 조회

* **Method:** GET

* **URL:** `/queues/{userId}`

* **Path Parameters:**

  | 이름     | 타입     | 필수 | 설명     |
    | ------ | ------ | -- | ------ |
  | userId | string | ✅  | 사용자 ID |

* **Response (200 OK):**

```json
{
  "user_id": "user_123",
  "waiting_order": 33
}
```

---

### ✅ Notes

* 모든 좌석은 1~50번으로 관리
* 좌석 임시 배정 시간: 5분 (`status: held`)
* 포인트 충전/결제 연동
* RESTful 설계: Path는 리소스 중심, 동사는 HTTP 메소드로 표현
* 민감 데이터(포인트, 결제)는 Request Body로 전달


