# RSC CVE-2025-55182 (React2Shell) 테스트용 앱

- **React**: 19.2.0 (취약)
- **@vitejs/plugin-rsc**: 0.5.2 (취약)

## Docker로 실행

프로젝트 루트에서:

```bash
docker compose up -d
```

RSC 서버: **http://localhost:5174**

- 페이지: `http://localhost:5174/`
- RSC 스트림: `http://localhost:5174/?rsc` 또는 `http://localhost:5174/.rsc`

## 인증 없이 HTTP로 역직렬화 테스트 (CVE-2025-55182)

**POST** 요청으로 RSC 스트림을 서버에서 역직렬화하는 엔드포인트가 열려 있습니다.  
인증 없이 호출 가능하며, 악성 RSC 페이로드로 RCE 테스트 시 사용할 수 있습니다.  
메인 앱(8888)에서는 **정상 API**처럼 **`/api/rsc/render`** 로 노출됩니다.

- **URL**: `POST http://localhost:8888/api/rsc/render` (메인 앱) 또는 `POST http://localhost:5174/api/rsc/render` (RSC 서버 직접)
- **Content-Type**: `text/x-component` (또는 생략 가능)
- **Body**: RSC(Flight) 페이로드 원시 바이트

### curl 예시

```bash
# 정상 페이로드로 역직렬화 호출 (먼저 .rsc에서 스트림 저장)
curl -s "http://localhost:5174/?rsc" -o payload.bin
curl -X POST "http://localhost:8888/api/rsc/render" \
  -H "Content-Type: text/x-component" \
  --data-binary @payload.bin

# PoC 페이로드가 있으면
curl -X POST "http://localhost:8888/api/rsc/render" \
  -H "Content-Type: text/x-component" \
  --data-binary @poc.bin
```

- 성공 시: `{"ok":true,"message":"deserialized"}` (200)
- 역직렬화 오류 시: `{"ok":false,"error":"..."}` (500)

## RSC 재생 API (RCE 시연용)

**POST `/api/rsc/replay`** 는 수신한 RSC 스트림에서 실행 가능 청크(`_prefix`)를 찾아 서버에서 실행합니다.  
점검·교육 시 CVE-2025-55182 RCE를 이 프로젝트 안에서 재현할 때 사용합니다.  
메인 앱(8888)에서 **`/api/rsc/replay`** 로 동일하게 노출됩니다.

- **URL**: `POST http://localhost:8888/api/rsc/replay` (또는 `POST http://localhost:5174/api/rsc/replay`)
- **Body**: RSC 스트림(주입된 RCE 청크 포함). PoC: `py cve-2025-55182-poc.py --rce-inject --replay`
- **성공(실행 후 throw)**: 500, `{"ok":false,"error":"<명령 출력>"}` → PoC가 해당 내용을 RCE 출력으로 표시
- **실행 후 예외 없음**: 200, `{"ok":true,"message":"executed (no throw)"}`
- **실행 가능 청크 없음**: 400, `{"ok":false,"error":"No executable chunk (_prefix) found in stream"}`

```bash
npm install
npm run dev
```

## CVE 테스트

이 앱은 RSC 엔드포인트와 **인증 없는 역직렬화 테스트 API**를 노출하므로, 공개된 PoC로 CVE-2025-55182 재현 테스트가 가능합니다.  
자세한 내용은 `docs/CVE-2025-55182-React2Shell.md` 를 참고하세요.
