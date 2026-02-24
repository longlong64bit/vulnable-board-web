# 취약점 점검용 게시판 (Vulnerable Board)

취약점 점검 등 보안 점검 시 **의도적으로 취약한** 웹 애플리케이션입니다.  
Spring Boot + MySQL 구성이며, Docker로 한 번에 실행할 수 있습니다.

## 기능

1. **회원가입 / 로그인**
2. **게시판** (목록, 조회, 작성, 수정, 삭제, 검색)
3. **댓글** (AJAX로 등록·목록 조회)
4. **파일 업로드 / 다운로드**

---

## 프로젝트 파일 구조

```
vulnerable-board-web/
├── docker-compose.yml          # Docker Compose 정의 (MySQL + 앱)
├── Dockerfile                  # 앱 이미지 빌드 스크립트
├── docker-entrypoint.sh        # 컨테이너 기동 시 MySQL 대기 후 앱 실행
├── pom.xml                     # Maven 빌드 설정 및 의존성
├── read_excel.py               # (참고) 엑셀 시트 추출용 유틸
├── README.md                   # 본 문서
├── docs/
│   └── VULNERABILITY_TEST_GUIDE.md   # 웹 취약점 테스트 가이드
├── frontend/                   # Vue 3 SPA (Vite)
│   ├── package.json
│   ├── vite.config.js
│   └── src/                    # 로그인·회원가입·게시판·댓글·파일 화면 및 API 연동
└── src/
    └── main/
        ├── java/com/vuln/board/
        │   ├── VulnBoardApplication.java    # Spring Boot 메인 클래스
        │   ├── config/
        │   │   ├── WebConfig.java           # 정적 리소스 매핑 + 디렉토리 리스팅 허용(취약 설정)
        │   │   ├── DirectoryListingResourceResolver.java
        │   │   └── DirectoryListingResource.java
        │   ├── controller/
        │   │   ├── AuthController.java      # 로그인/회원가입/로그아웃
        │   │   ├── BoardController.java     # 게시판 CRUD
        │   │   ├── CommentController.java  # 댓글 API (AJAX)
        │   │   ├── FileController.java     # 파일 업로드/다운로드
        │   │   └── VulnExtraController.java # 추가 취약 엔드포인트
        │   ├── entity/
        │   │   ├── Attachment.java          # 첨부파일 엔티티
        │   │   ├── Board.java               # 게시글 엔티티
        │   │   ├── Comment.java             # 댓글 엔티티
        │   │   └── User.java                # 사용자 엔티티
        │   └── repository/
        │       ├── AttachmentRepository.java # 첨부파일 DB 접근
        │       ├── BoardRepository.java      # 게시판 DB 접근 (SQL Injection 취약)
        │       ├── CommentRepository.java   # 댓글 DB 접근
        │       └── UserRepository.java       # 사용자 DB 접근 (SQL Injection 취약)
        └── resources/
            ├── application.properties       # 서버/DB/파일업로드 설정
            ├── schema.sql                   # DB 테이블 생성 DDL
            └── templates/                  # Thymeleaf HTML 뷰
                ├── login.html               # 로그인 화면
                ├── join.html                # 회원가입 화면
                ├── admin.html               # 관리자 페이지 (인증 없음)
                ├── ssti.html                # SSTI 테스트용 뷰
                └── board/
                    ├── list.html            # 게시판 목록
                    ├── view.html            # 게시글 상세 + 댓글/첨부
                    ├── write.html           # 글쓰기
                    └── edit.html            # 글 수정
```

---

## 파일별 용도 및 기능

### 루트 및 Docker 설정

| 파일 | 용도 및 기능 |
|------|----------------|
| **docker-compose.yml** | MySQL 서비스와 앱 서비스를 정의. MySQL은 healthcheck 후 앱이 기동되도록 `depends_on` 설정. 앱은 8888→8080 포트 매핑, 업로드 디렉터리 볼륨 마운트. MySQL 포트는 호스트에 노출하지 않음(내부 전용). |
| **Dockerfile** | 1단계: Maven으로 `pom.xml`·`src` 기준 JAR 빌드. 2단계: JRE Alpine 이미지에 JAR·엔트리포인트 복사, `netcat-openbsd`·`bash` 설치. 엔트리포인트에서 MySQL 대기 후 `java -jar` 실행. |
| **docker-entrypoint.sh** | MySQL 호스트/포트에 대해 `nc -z`로 연결 가능할 때까지 대기한 뒤, 인자로 받은 명령(`java -jar app.jar`) 실행. Windows CRLF 대응은 Dockerfile에서 `sed`로 처리. |

### 빌드 및 문서

| 파일 | 용도 및 기능 |
|------|----------------|
| **pom.xml** | Spring Boot 2.7.18, Java 17. 의존성: spring-boot-starter-web, jdbc, thymeleaf, mysql-connector-j, lombok. JAR 패키징으로 `vuln-board-*.jar` 생성. |
| **README.md** | 프로젝트 소개, 실행 방법, 취약점 목록, 파일 구조, 파일별 설명(본 문서). |
| **read_excel.py** | 엑셀 시트(웹/모바일/HTS 등) 내용을 텍스트로 추출하는 참고용 스크립트. openpyxl 사용. |

### docs

| 파일 | 용도 및 기능 |
|------|----------------|
| **docs/VULNERABILITY_TEST_GUIDE.md** | 웹 취약점 평가 항목과 본 앱의 테스트 위치·방법 매핑. SQL Injection, XSS, 파일 업로드/다운로드, SSRF, OS명령, XXE, SSTI, 관리자 노출 등 시나리오 정리. |

### Java 소스 — 메인·설정

| 파일 | 용도 및 기능 |
|------|----------------|
| **VulnBoardApplication.java** | `@SpringBootApplication` 진입점. `main`에서 `SpringApplication.run` 호출. |
| **config/WebConfig.java** | `WebMvcConfigurer` 구현. `file.upload-dir` 경로를 `/uploads/**` URL로 노출해 업로드된 파일을 브라우저에서 직접 접근 가능하게 함. |
| **config/CorsConfig.java** | CORS 허용. Vue 등 프론트 개발 서버(5173, 3000)에서 API 호출 시 credentials 포함 허용. |

### Java 소스 — 컨트롤러

| 파일 | 용도 및 기능 |
|------|----------------|
| **AuthController.java** | `/`, `/login`, `/logout`, `/join` 처리. 로그인 시 `UserRepository.findByUserId(userId)` 로 조회(문자열 연결 쿼리 → SQL Injection 취약). 비밀번호 평문 비교·세션에 userId 저장. 회원가입 시 비밀번호 평문 저장. CSRF 토큰 미사용. |
| **controller/api/ApiAuthController.java** | REST: `POST /api/auth/login`, `POST /api/auth/join`, `POST /api/auth/logout`, `GET /api/auth/me` (JSON). |
| **controller/api/ApiBoardController.java** | REST: `GET /api/board/list`, `GET /api/board/{id}`, `POST /api/board`, `PUT /api/board/{id}`, `DELETE /api/board/{id}` (JSON). |
| **BoardController.java** | `/board/list`, `/view/{id}`, `/write`, `/edit/{id}`, `/delete/{id}`. 목록에서 `keyword`, `orderBy`를 쿼리 문자열에 연결해 SQL Injection 취약. 제목·내용을 뷰에서 `th:utext`로 출력해 Stored XSS 가능. 작성자만 수정/삭제 가능하나 첨부파일 다운로드는 별도 권한 검증 없음. |
| **CommentController.java** | `/api/comment/list`, `/api/comment/add`, `/api/comment/delete/{id}`. JSON으로 댓글 목록·등록·삭제. 댓글 내용 검증/이스케이프 없이 저장·반환. 뷰에서 `innerHTML`로 넣어 Stored XSS 가능. |
| **FileController.java** | `/file/upload`: 확장자·MIME·크기 제한 없이 업로드, `upload-dir`에 저장. `/file/download/{id}`: DB에서 첨부 조회 후 저장된 파일명으로 파일 전송(IDOR 가능). `/file/get?name=...`: `name`을 경로에 연결해 Path Traversal 취약. |
| **VulnExtraController.java** | 추가 취약 엔드포인트. `/admin`, `/redirect?url=`, `/fetch?url=`, `/cmd?exec=`, `/dir`, `/dir?path=`, `/debug`, `/ssti?name=`, `POST /xml` 등. |
| **WebConfig.java** | `/uploads/**` 를 업로드 디렉터리로 매핑. **디렉토리 리스팅 허용** 설정(취약): `DirectoryListingResourceResolver` 등록으로 인덱스 파일 없을 때 `/uploads/` 접근 시 목록 HTML 출력. |
| **DirectoryListingResourceResolver.java** | 정적 리소스가 디렉터리일 때 목록을 반환하는 리졸버. 웹 서버 설정 오류(리스팅 허용) 시뮬레이션. |
| **DirectoryListingResource.java** | 디렉터리 목록 HTML을 생성하는 Resource 구현체. |

### Java 소스 — 엔티티

| 파일 | 용도 및 기능 |
|------|----------------|
| **entity/User.java** | 사용자: id, userId, password, name, createdAt. 로그인·회원가입 시 사용. |
| **entity/Board.java** | 게시글: id, title, content, writerId, createdAt. 목록·상세·쓰기·수정 시 사용. comments·attachments 리스트 포함. |
| **entity/Comment.java** | 댓글: id, boardId, writerId, content, createdAt. 댓글 API에서 사용. |
| **entity/Attachment.java** | 첨부파일: id, boardId, originalName, storedName, createdAt. 업로드·다운로드 시 사용. |

### Java 소스 — 리포지토리

| 파일 | 용도 및 기능 |
|------|----------------|
| **UserRepository.java** | `findByUserId(userId)`: `"SELECT * FROM users WHERE user_id = '" + userId + "'"` (SQL Injection). `findByCondition(condition)`: 조건 문자열 직접 연결. `insert`: 회원가입 시 사용. |
| **BoardRepository.java** | `findAll(orderBy, keyword)`: `keyword`·`orderBy`를 쿼리 문자열에 연결(SQL Injection). `findById`, `insert`, `update`, `deleteById` 등. |
| **CommentRepository.java** | `findByBoardId`, `insert`, `deleteById`. PreparedStatement 사용. |
| **AttachmentRepository.java** | `findByBoardId`, `findById`, `insert`, `deleteByBoardId`. PreparedStatement 사용. |

### 리소스 — 설정·DB

| 파일 | 용도 및 기능 |
|------|----------------|
| **application.properties** | server.port=8080, MySQL URL/계정(환경변수 대응), multipart 크기·업로드 디렉터리, Thymeleaf 캐시 끄기, `schema.sql` 실행 설정. |
| **schema.sql** | `users`, `board`, `comment`, `attachment` 테이블 생성 DDL. Spring Boot 기동 시 `spring.sql.init`으로 실행. |

### 리소스 — 템플릿(뷰)

| 파일 | 용도 및 기능 |
|------|----------------|
| **templates/login.html** | 로그인 폼. 아이디·비밀번호 POST `/login`. 에러/성공 메시지 표시, 회원가입 링크. |
| **templates/join.html** | 회원가입 폼. userId, password, name POST `/join`. |
| **templates/admin.html** | 관리자 페이지. `/admin`에서 접근. 서버 정보 등 노출(인증 없음). |
| **templates/ssti.html** | `name` 파라미터를 `th:utext`로 출력. SSTI 테스트용. |
| **templates/board/list.html** | 게시판 목록. 검색 폼(keyword, orderBy). 목록에서 제목·작성자·날짜. `keyword`를 `th:utext`로 출력해 Reflected XSS. |
| **templates/board/view.html** | 게시글 상세. 제목·내용 `th:utext`(Stored XSS). 첨부 목록·다운로드 링크. 본인 글일 때 파일 업로드 폼(AJAX `/file/upload`), 수정/삭제. 댓글 영역: `fetch`로 목록 조회·등록, `innerHTML`로 렌더링(Stored XSS). |
| **templates/board/write.html** | 글쓰기 폼. title, content POST `/board/write`. |
| **templates/board/edit.html** | 글 수정 폼. title, content POST `/board/edit/{id}`. |

---

## 실행 방법 (Docker)

많은 환경(Cursor, 일부 Windows 등)에서 `docker compose up -d --build` 실행 시 **gRPC 관련 오류**가 발생합니다.  
이 경우 **`build.bat`** 으로 빌드한 뒤 컨테이너를 띄우세요.

```bash
# 프로젝트 루트에서 (권장)
.\build.bat
# 또는 CMD: build

# 로그 확인
docker compose logs -f app
```

**gRPC 오류 나지 않는 환경**에서는 아래처럼 바로 실행해도 됩니다.

```bash
docker compose up -d --build
docker compose logs -f app
```

- 웹 접속: **http://localhost:8888** — Spring Boot가 서빙하는 **React SPA** (빌드된 정적 파일).
- 프론트 개발 서버: **http://localhost:5173** — React 게시판 앱 (Vite, API는 8888로 프록시).
- **RSC(React Server Components) API**: 메인 서버 **http://localhost:8888** 의 **/api/rsc/** 경로로 RSC 렌더/스트림 API가 제공됩니다. **로컬에서 Docker로 요청할 때는 8888 포트로 HTTP 요청**하면 됩니다. 내부적으로 RSC 전용 서버(5174)로 프록시됩니다.  
  예: `POST http://localhost:8888/api/rsc/render` (RSC 페이로드 전송).  
  RSC 서버를 직접 쓰려면 **http://localhost:5174** (Docker에서 5174 포트 노출 시).  
  **CVE-2025-55182 PoC** 스크립트는 **`poc/`** 폴더에 있음 (`poc/README.md` 참고).  
  **Docker 안에서 PoC 실행**: `docker compose --profile poc run --rm poc` (같은 네트워크에서 app:8080 호출).
- MySQL: **도커 내부 전용** (호스트에 포트 미노출, app 컨테이너만 `mysql:3306` 접근)

첫 실행 시 MySQL 초기화로 30초~1분, 이미지 빌드 시 Node로 Vue 빌드가 포함되어 조금 더 걸릴 수 있습니다.

### 코드 변경 후 적용 방법 (Docker)

| 변경한 부분 | 적용 방법 |
|-------------|-----------|
| **프론트만** (frontend/, frontend-rsc/ 소스) | 볼륨 마운트라 **재빌드 불필요**. 저장 후 브라우저 새로고침. 필요 시 `docker compose restart frontend` 또는 `docker compose restart frontend-rsc` |
| **Spring만** (src/, templates/, pom.xml 등) | **app 이미지 재빌드** 후 재기동. CMD: `build app` 또는 PowerShell: `.\build.bat app` (gRPC 오류 나면 `.\build.bat app` 사용) |
| **전체** 또는 처음 띄우기 | `.\build.bat` 또는 `docker compose up -d --build` (gRPC 오류 시 `.\build.bat`) |

로컬에서 Spring만 실행 중이면(IDE 또는 `mvn spring-boot:run`): **앱 한 번 종료 후 다시 실행**하면 됩니다.

### React(프론트)만 로컬에서 개발할 때

백엔드를 로컬 또는 Docker(8888)로 띄운 뒤, 프론트만 별도 실행하려면:

```bash
cd frontend
npm install
npm run dev
```

- 브라우저: **http://localhost:5173** (CORS·세션은 8888과 공유)

## 포함된 취약점 (의도적)

| 구분 | 취약점 | 위치/방법 |
|------|--------|-----------|
| **SQL Injection** | 로그인 우회 | 로그인 시 `userId`에 `' OR '1'='1` 등 |
| **SQL Injection** | 게시판 검색/정렬 | 목록에서 `keyword`, `orderBy` 파라미터 조작 (예: `orderBy=id; DROP TABLE board;--`) |
| **Stored XSS** | 게시글 제목/내용 | 글쓰기에서 `<script>alert(1)</script>` 등 입력 후 목록/상세에서 비이스케이프 출력 |
| **Reflected XSS** | 검색어 | 게시판 목록에서 검색 시 `keyword`를 그대로 출력 |
| **Stored XSS** | 댓글 | 댓글 내용을 AJAX로 저장 후 화면에 `innerHTML`로 출력 |
| **파일 업로드** | 확장자/타입 미검증 | jsp, jspx, exe 등 업로드 가능, 웹쉘 업로드 시도 가능 |
| **Path Traversal** | 파일 다운로드 | `/file/get?name=../../../etc/passwd` 등으로 서버 내 파일 접근 시도 |
| **IDOR** | 첨부파일 | 다른 게시글의 첨부파일 `id`만 알면 `/file/download/{id}` 로 다운로드 가능 |
| **기타** | 비밀번호 평문 저장, CSRF 미적용, 세션 고정 등 | 회원가입/로그인/폼 전송 |
| **Open Redirect** | 리다이렉트 피싱 | `/redirect?url=https://evil.com` |
| **SSRF** | 서버 사이드 요청 위조 | `/fetch?url=...` (로그인 필요) |
| **OS 명령실행** | 사용자 입력을 exec()에 전달 | `/cmd?exec=id` (로그인 필요) |
| **XXE** | XML 외부객체 파싱 | `POST /xml` (로그인 필요) |
| **SSTI** | 서버 사이드 템플릿 인젝션 | `/ssti?name=...` (아래 "SSTI 테스트 방법" 참고) |
| **관리자 노출** | 인증 없이 관리자 페이지 접근 | `/admin` |
| **디렉토리 목록** | 업로드 디렉토리 목록 노출 | `/dir`, `/dir?path=...` (로그인 필요) |
| **인덱스 없음/디렉토리 리스팅** | 웹 서버 설정 오류(인덱스 없음 + 리스팅 허용)로 하위 파일·디렉터리 목록 출력 | `/uploads/` 접근 시 인덱스 파일 없으면 디렉터리 목록 HTML 출력(설정으로 리스팅 허용) |
| **시스템정보 노출** | 디버그 정보 노출 | `/debug` (로그인 필요) |
| **RSC 역직렬화 (CVE-2025-55182)** | React Server Components 스트림 역직렬화(React2Shell) | `POST /api/rsc/render` (RSC 페이로드 전송) |

#### SSTI 테스트 방법 (Java / Spring / Thymeleaf)

Python(Jinja2 등)과 달리 **Thymeleaf**는 **전처리(preprocessing)** `__${...}__` 구문으로 사용자 입력이 표현식으로 평가될 때 SSTI가 발생합니다. 본 앱은 `ssti.html`에서 `th:utext="${__${name}__}"`를 사용해 `name` 파라미터 값을 Thymeleaf 표현식으로 평가합니다.

| 테스트 목적 | 요청 예시 | 기대 결과 |
|-------------|-----------|-----------|
| 표현식 실행 여부 | `GET /ssti?name=7*7` | 화면에 **49** 출력 |
| 문자열 조작 | `GET /ssti?name='hello'.toUpperCase()` | **HELLO** 출력 |
| 시스템 프로퍼티 | `GET /ssti?name=T(java.lang.System).getProperty('user.dir')` | 서버 작업 디렉터리 경로 출력 |

- **URL 인코딩**: `name`에 `+`, 공백, `'` 등이 있으면 `%2B`, `%20`, `%27` 등으로 인코딩해서 요청하세요.
- **Python과의 차이**: Jinja2의 `{{ ... }}`와 달리 Thymeleaf는 `${...}`(일반 표현식)와 `__${...}__`(전처리로 한 번 더 평가)를 구분합니다. 본 앱은 `name`을 전처리 표현식에 넣어 SSTI가 성립하도록 구성했습니다.

## 웹 취약점 테스트 가이드

웹 취약점 항목별 테스트 위치·방법은 아래 문서를 참고하세요.

- **[docs/VULNERABILITY_TEST_GUIDE.md](docs/VULNERABILITY_TEST_GUIDE.md)** — 항목별 테스트 위치·방법 매핑 및 시나리오

## 로컬에서만 실행 (Docker 없이)

- MySQL 8.x를 로컬에 설치 후 `vulndb` DB 생성
- `application.properties`에서 DB 접속 정보 확인
- `./mvnw spring-boot:run` 또는 `mvn spring-boot:run`

## 주의

- **실제 서비스/운영 환경에 사용하지 마세요.**
- 내부 보안 점검·교육·PoC 목적으로만 사용하세요.
