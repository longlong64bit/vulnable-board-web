# Vue 빌드 결과물 설명 (초보자용)

`http://127.0.0.1:8888/board` 에 접속했을 때 보이는 HTML과 `assets` 폴더 안의 JS/CSS 파일이 무엇인지, 소스 코드와 어떻게 연결되는지 정리한 문서입니다.

---

## 1. 화면에 보이는 HTML이 뭔가요?

브라우저에서 **"페이지 소스 보기"** 또는 개발자 도구로 보면 대략 아래와 같습니다.

```html
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/vite.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>취약점 평가용 게시판</title>
    <script type="module" crossorigin src="/assets/index-BN0Abl-D.js"></script>
    <link rel="stylesheet" crossorigin href="/assets/index-Cp88lDxk.css">
  </head>
  <body>
    <div id="app"></div>
  </body>
</html>
```

### 각 부분이 하는 일

| 부분 | 설명 |
|------|------|
| `<div id="app"></div>` | Vue 앱이 **붙을 자리**입니다. `main.js`에서 `app.mount('#app')`으로 여기에 로그인·게시판 등이 그려집니다. |
| `<script src="/assets/index-xxx.js">` | **실제 앱 코드 전체**가 들어 있는 자바스크립트 파일입니다. 이 한 파일(또는 여러 청크)이 Vue, 라우터, API 호출, 화면 컴포넌트를 모두 포함합니다. |
| `<link href="/assets/index-xxx.css">` | **전체 스타일**이 들어 있는 CSS 파일입니다. `base.css`와 각 `.vue` 파일의 스타일이 합쳐져 있습니다. |

즉, **빈 HTML + JS/CSS 조합**으로 동작하는 **SPA(Single Page Application)** 구조입니다.  
서버는 항상 이 `index.html`을 주고, 나머지 화면 전환은 JS가 처리합니다.

---

## 1.1 왜 모든 URL에서 "소스 보기"하면 똑같은 HTML만 나올까?

`/`, `/login`, `/board`, `/board/1` … **어떤 주소로 들어가도** "페이지 소스 보기"를 하면 위와 똑같은 HTML만 보입니다.

### 원리

- **서버(Spring) 입장**: `/`, `/login`, `/board`, `/board/xxx` 같은 요청을 **페이지마다 다른 HTML로 구분하지 않습니다.**  
  `SpaController`가 이 경로들을 모두 받아서 **같은 응답**을 내려줍니다.  
  → **"`/index.html` 내용 그대로 보내라(forward)"** 한 가지 규칙만 있습니다.
- **그래서**: 브라우저가 받는 **최초 문서**는 항상 동일한 `index.html` 한 장입니다.  
  그래서 "소스 보기" = "서버가 준 그 문서" = **어디로 들어가도 같은 HTML**이 보입니다.

즉, **"모든 페이지에 같은 소스가 나온다"가 아니라**,  
**"모든 URL에 대해 서버가 같은 HTML 한 장만 보내기 때문에, 소스 보기에도 그 한 장만 보인다"**가 맞습니다.

### 흐름(Flow) 요약

```
[사용자] http://127.0.0.1:8888/board 입력
    │
    ▼
[브라우저] 서버에 GET /board 요청
    │
    ▼
[Spring]
  - /api/board/list 같은 API가 아니므로 → SpaController 처리
  - "forward:/index.html" → index.html 내용을 그대로 응답
    │
    ▼
[브라우저] 동일한 index.html 수신 (div#app만 있는 빈 껍데기)
    │
    ├─► <script src="/assets/index-xxx.js"> 로 JS 요청·실행
    │       │
    │       └─► Vue 앱 기동 → Vue Router가 "지금 URL이 /board다" 확인
    │               │
    │               └─► BoardList.vue에 해당하는 JS 청크 로드 후
    │                     <div id="app"> 안에 게시판 목록 DOM을 **JS로 추가**
    │
    └─► 화면에는 "게시판 목록"이 보이지만,
        서버가 보낸 원본 HTML에는 그 내용이 없음 → 소스 보기에는 안 보임
```

정리하면:

1. **서버**는 URL이 `/`, `/login`, `/board`, `/board/1` … 무엇이든 **같은 `index.html` 한 장**만 보냅니다.
2. **브라우저**는 그 HTML을 받고, 그 안의 `<script>`로 **JS(Vue 앱)** 를 불러와 실행합니다.
3. **Vue Router**가 **현재 주소창 URL**을 보고, 그에 맞는 화면(로그인, 목록, 상세 등)을 **JS로 만들어서** `<div id="app">` 안에 넣습니다.
4. 따라서 **실제 보이는 내용(로그인 폼, 게시판 목록, 글 내용 등)은 전부 "JS가 만든 DOM"** 이고,  
   **"페이지 소스 보기"는 "서버가 보낸 최초 HTML"만 보여 주기 때문에** 항상 빈 껍데기만 보입니다.

### 소스 보기 vs 요소 검사(Inspect)

| 보는 것 | 의미 |
|--------|------|
| **페이지 소스 보기 (View Source)** | **서버가 처음 보낸 HTML 그대로**. 그래서 모든 URL에서 동일한 `index.html`만 보입니다. |
| **요소 검사 (Inspect Element / 개발자도구)** | **지금 시점의 화면 DOM**. JS가 그린 로그인 폼, 게시판 목록, 글 내용 등이 모두 포함됩니다. |

SPA에서는 "화면에 보이는 내용"이 대부분 JS가 그린 것이므로, **원리와 흐름**을 이해할 때는 위 두 가지를 구분해 두는 것이 좋습니다.

---

## 2. "빌드"가 뭔가요?

- **개발할 때**: `frontend/src/` 아래에 `main.js`, `App.vue`, `Login.vue`, `BoardList.vue` 등 **많은 파일**로 나눠서 작성합니다.
- **배포할 때**: 이 파일들을 **한 번에 묶고(번들링)** 압축해서, **적은 수의 JS/CSS 파일**로 만듭니다. 이 과정을 **빌드(build)** 라고 합니다.

Vite가 빌드하면 `frontend/dist/` 폴더에 대략 다음이 생깁니다.

- `index.html` — 위에서 본 그 HTML (경로만 빌드 결과에 맞게 바뀜)
- `assets/index-xxxxxx.js` — 메인 진입점 + 공통 코드
- `assets/index-xxxxxx.css` — 합쳐진 스타일
- `assets/로그인-Bxxxxxx.js`, `assets/BoardList-Bxxxxxx.js` 등 — **페이지별로 나눈 JS** (코드 스플리팅)

파일명에 붙는 **`BN0Abl-D`, `Cp88lDxk` 같은 문자열**은 **해시**입니다.  
내용이 바뀔 때마다 이름이 바뀌어서, 브라우저가 **예전 캐시 대신 새 파일**을 받도록 합니다.

---

## 3. 빌드 결과 파일 ↔ 소스 코드 매핑

아래는 **빌드 후 생성되는 파일**과 **그 안에 들어 있는 소스**를 대응한 표입니다.  
실제 파일명의 해시(`BN0Abl-D` 등)는 빌드할 때마다 달라지고, 이름 패턴만 같습니다.

### 3.1 항상 나오는 파일

| 빌드 결과 파일 (예시) | 내용(포함되는 소스) |
|----------------------|----------------------|
| **index.html** | `frontend/index.html`을 기반으로, script/link 경로만 `assets/` 쪽으로 바뀐 것. |
| **assets/index-[해시].js** | Vue 앱의 **진입점 + 공통 코드**. `main.js`, `App.vue`, `router/index.js`, `api/client.js`, `api/auth.js`, `api/board.js`, `api/comment.js`, `api/file.js`, Vue·Vue Router·axios 라이브러리 등. |
| **assets/index-[해시].css** | `assets/base.css` + 각 `.vue` 파일의 `<style>` 이 합쳐진 **전체 CSS**. |

### 3.2 페이지별로 나뉜 JS (코드 스플리팅)

라우터에서 `import('../views/Login.vue')` 처럼 **동적 import**를 쓰기 때문에,  
해당 페이지는 **별도 JS 파일**로 쪼개져서, 그 페이지로 갈 때만 로드됩니다.

| 빌드 결과 파일 (예시) | 해당 화면 | 대응 소스 파일 |
|----------------------|-----------|-----------------|
| **assets/Login-[해시].js** | 로그인 화면 | `src/views/Login.vue` |
| **assets/Join-[해시].js** | 회원가입 화면 | `src/views/Join.vue` |
| **assets/BoardList-[해시].js** | 게시판 목록 | `src/views/BoardList.vue` |
| **assets/BoardWrite-[해시].js** | 글쓰기 | `src/views/BoardWrite.vue` |
| **assets/BoardView-[해시].js** | 게시글 상세 + 댓글 | `src/views/BoardView.vue` |
| **assets/BoardEdit-[해시].js** | 글 수정 | `src/views/BoardEdit.vue` |

(실제 파일명은 Vite 버전에 따라 `Login-xxx.js` 형태일 수도 있고, 해시만 붙은 형태일 수도 있습니다.)

---

## 4. 흐름 한 줄 요약

1. 브라우저가 `http://127.0.0.1:8888/board` 를 요청하면  
   → Spring이 **항상 같은 `index.html`** 을 내려줍니다.
2. 그 HTML이 **`/assets/index-xxx.js`** 와 **`/assets/index-xxx.css`** 를 로드합니다.
3. JS가 실행되면서 **경로가 `/board`** 이므로,  
   → 라우터가 **게시판 목록용 청크**(예: `BoardList-xxx.js`)를 추가로 요청해  
   → 그 안에 있는 **BoardList.vue** 내용으로 `<div id="app">` 안을 그립니다.

그래서 `/board` 를 열었을 때 보이는 HTML은 단순해도,  
**assets 폴더의 여러 JS**가 각각 “메인 앱”, “로그인”, “목록”, “상세” 등 **역할별로 나뉘어** 있는 것입니다.

---

## 5. 소스 폴더 구조 참고

빌드 전 **소스**는 대략 아래 구조입니다. 위 표의 “대응 소스”와 연결해서 보면 됩니다.

```
frontend/
├── index.html              → 빌드 후 그대로 dist/index.html (script/link 경로만 변경)
├── src/
│   ├── main.js             → assets/index-xxx.js 안에 포함
│   ├── App.vue             → assets/index-xxx.js 안에 포함
│   ├── router/index.js     → assets/index-xxx.js 안에 포함
│   ├── api/
│   │   ├── client.js       → assets/index-xxx.js 안에 포함
│   │   ├── auth.js
│   │   ├── board.js
│   │   ├── comment.js
│   │   └── file.js
│   ├── assets/
│   │   └── base.css        → assets/index-xxx.css 안에 포함
│   └── views/
│       ├── Login.vue       → assets/Login-xxx.js (또는 비슷한 이름)
│       ├── Join.vue        → assets/Join-xxx.js
│       ├── BoardList.vue   → assets/BoardList-xxx.js
│       ├── BoardView.vue   → assets/BoardView-xxx.js
│       ├── BoardWrite.vue  → assets/BoardWrite-xxx.js
│       └── BoardEdit.vue   → assets/BoardEdit-xxx.js
```

이 문서는 초보자를 위한 설명이므로, “화면에 보이는 HTML”과 “assets 밑 다양한 JS”가 위와 같이 매핑된다고 이해하면 됩니다.
