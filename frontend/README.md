# Vue 3 프론트엔드 (취약점 평가용 게시판)

Vite + Vue 3 + Vue Router + Axios 로 구성된 SPA입니다.  
Spring Boot 백엔드(8888)의 REST API(`/api/auth/*`, `/api/board/*`, `/api/comment/*`, `/file/*`)를 사용합니다.

## 요구 사항

- Node.js 18+
- 백엔드 서버가 **http://localhost:8888** 에서 실행 중이어야 함 (Docker 또는 `mvn spring-boot:run`)

## 설치 및 실행

```bash
npm install
npm run dev
```

브라우저에서 **http://localhost:5173** 접속.

- 개발 모드에서는 API 요청이 `http://localhost:8888`로 가며, CORS + 쿠키(세션)가 공유됩니다.

## 빌드

```bash
npm run build
```

결과물은 `dist/`에 생성됩니다.  
Spring에서 SPA를 서빙하려면 `dist/` 내용을 `src/main/resources/static/`에 복사하고, 미매칭 경로를 `index.html`로 보내는 SPA 폴백 설정을 추가하면 됩니다.

## 구조

- `src/main.js` — 앱 진입, 라우터 적용
- `src/App.vue` — 상단 헤더(로그인/로그아웃, 목록/글쓰기 링크)
- `src/router/index.js` — 라우트 정의 및 인증 가드
- `src/api/` — axios 클라이언트 및 auth/board/comment/file API
- `src/views/` — Login, Join, BoardList, BoardView, BoardWrite, BoardEdit
