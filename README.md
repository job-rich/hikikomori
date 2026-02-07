# 히키코모리 (Hikikomori)

> 방에서 나가지 않아도 세상과 싸울 수 있다.

매일 자정, 모든 것이 사라집니다.
게시글도, 댓글도, 당신의 흑역사도 전부.
하지만 걱정 마세요. 내일이면 또 새로운 전장이 펼쳐지니까요.

## 이게 뭔데?

**히키코모리**는 매일매일 초기화되는 익명 커뮤니티입니다.

로그인 따위 필요 없습니다. 들어오는 순간 당신은 랜덤 닉네임을 부여받은 한 명의 **투사**가 됩니다.
오늘 하루, 당신의 논리를 마음껏 설파하세요. 내일이면 어차피 다 사라지니까.

## 왜 써야 하는데?

- **매일 리셋** — 자정이 지나면 깨끗한 백지 상태. 어제의 망언은 우주의 먼지가 됩니다.
- **노 로그인** — 회원가입? 이메일 인증? 그런 거 없습니다. 그냥 들어와서 쓰면 됩니다.
- **랜덤 닉네임** — 접속할 때마다 새로운 정체성. 오늘의 당신은 "분노한 두부"일 수도, "철학하는 고등어"일 수도 있습니다.
- **댓글 전쟁** — 게시글에 댓글, 댓글에 대댓글. 2단계 중첩으로 논쟁의 깊이를 더하세요.
- **기록 보존 (선택)** — 오늘의 명언을 영원히 간직하고 싶다면? 리셋 전에 저장할 수 있습니다.

## 기술 스택

| 영역 | 기술 |
|------|------|
| **Backend** | Spring Boot 4.1, Java 25, Gradle |
| **Frontend** | Next.js 16, React 19, TypeScript |
| **Database** | PostgreSQL |
| **Styling** | Tailwind CSS v4 |
| **Infra** | Docker Compose |

## 시작하기

### 원큐 실행 (Docker Compose)

```bash
docker-compose up
```

끝입니다. PostgreSQL, Backend, Frontend가 알아서 올라갑니다.
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080

### 로컬 개발

**Backend**

```bash
# 빌드 & 테스트
./backend/gradlew -p backend build

# 실행 (PostgreSQL 자동 시작)
./backend/gradlew -p backend bootRun
```

**Frontend**

```bash
cd frontend && npm install
npm run dev
```

## 프로젝트 구조

```
hikikomori/
├── backend/                 # Spring Boot API 서버
│   └── src/main/java/org/hikikomori/community/
│       ├── controller/      # REST API 엔드포인트
│       ├── domain/          # JPA 엔티티 (Post, Comment)
│       ├── repository/      # 데이터 접근 계층
│       └── service/         # 비즈니스 로직
├── frontend/                # Next.js 클라이언트
│   ├── app/                 # App Router 페이지
│   ├── Components/          # UI 컴포넌트
│   └── lib/                 # 유틸리티, 타입, 데이터
└── docker-compose.yml       # 원큐 실행 설정
```

## 라이선스

[MIT](LICENSE)

---

*"오늘의 논쟁은 내일의 거름이 된다."* — 어느 히키코모리
