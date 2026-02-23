import { renderToReadableStream } from '@vitejs/plugin-rsc/rsc'
import React from 'react'
import { createRequire } from 'module'
import { Root } from './Root.jsx'

const require = createRequire(import.meta.url)

export default async function handler(request) {
  const url = new URL(request.url)
  const pathname = url.pathname
  const isRenderPath = pathname === '/api/rsc/render' || pathname === '/render'

  // GET /api/rsc/render 또는 /render → 브라우저 직접 접근 시. POST 전용 API이므로 안내만 반환 (HTML 렌더 시 server.node.js 로드 오류 방지)
  if (request.method === 'GET' && isRenderPath) {
    return new Response(
      JSON.stringify({
        message: 'RSC render API: use POST with Content-Type: text/x-component and RSC stream body.',
        endpoints: { render: 'POST /api/rsc/render', replay: 'POST /api/rsc/replay' },
      }),
      { status: 200, headers: { 'Content-Type': 'application/json' } }
    )
  }

  // RSC 재생/디버그: 스트림 내 실행 가능 청크(_prefix)를 서버에서 실행 (점검·교육용, CVE-2025-55182 RCE 시연)
  // 메인 앱 프록시 시 경로가 /replay 로 전달됨
  if (request.method === 'POST' && (url.pathname === '/api/rsc/replay' || url.pathname === '/replay')) {
    try {
      const raw = await request.arrayBuffer()
      const text = new TextDecoder('utf-8', { fatal: false }).decode(raw)
      const lines = text.split(/\r?\n/)
      for (const line of lines) {
        const m = line.match(/^(\d+):(\{.*\})$/)
        if (!m) continue
        let obj
        try {
          obj = JSON.parse(m[2])
        } catch {
          continue
        }
        if (typeof obj._prefix !== 'string' || !obj._prefix) continue
        const code = obj._prefix.trim().replace(/\/\/\s*$/, '')
        try {
          const fn = new Function('require', code)
          fn(require)
          return new Response(JSON.stringify({ ok: true, message: 'executed (no throw)' }), {
            status: 200,
            headers: { 'Content-Type': 'application/json' },
          })
        } catch (e) {
          return new Response(
            JSON.stringify({ ok: false, error: e?.message ?? String(e) }),
            { status: 500, headers: { 'Content-Type': 'application/json' } }
          )
        }
      }
      return new Response(
        JSON.stringify({ ok: false, error: 'No executable chunk (_prefix) found in stream' }),
        { status: 400, headers: { 'Content-Type': 'application/json' } }
      )
    } catch (e) {
      return new Response(
        JSON.stringify({ ok: false, error: String(e?.message || e) }),
        { status: 500, headers: { 'Content-Type': 'application/json' } }
      )
    }
  }

  // RSC 스트림 역직렬화(서버 렌더 파이프라인). POST body = RSC payload. (메인 앱에서 /api/rsc/render 로 프록시 시 /render 로 수신)
  if (request.method === 'POST' && (url.pathname === '/api/rsc/render' || url.pathname === '/render')) {
    const contentType = request.headers.get('content-type') || ''

    // multipart/form-data (Next.js Server Actions 형식): 이 Vite RSC 빌드에는 react-server-dom-webpack/server.node.js 가 없어 사용 불가 → 501
    if (contentType.includes('multipart/form-data')) {
      return new Response(
        JSON.stringify({ ok: false, error: 'decodeReply not available in this build. Use text/x-component stream.' }),
        { status: 501, headers: { 'Content-Type': 'application/json' } }
      )
    }

    // text/x-component (스트림) → createFromReadableStream
    try {
      const ssrEntry = await import.meta.viteRsc.loadModule('ssr', 'index')
      await ssrEntry.deserializeRscStream(request.body)
      return new Response(JSON.stringify({ ok: true, message: 'deserialized' }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      })
    } catch (e) {
      return new Response(
        JSON.stringify({ ok: false, error: String(e?.message || e) }),
        { status: 500, headers: { 'Content-Type': 'application/json' } }
      )
    }
  }

  const rscPayload = { root: <Root /> }
  const rscStream = renderToReadableStream(rscPayload)

  if (url.pathname.endsWith('.rsc') || url.searchParams.get('rsc') !== null) {
    return new Response(rscStream, {
      headers: { 'Content-type': 'text/x-component;charset=utf-8' },
    })
  }

  const ssrEntry = await import.meta.viteRsc.loadModule('ssr', 'index')
  const htmlStream = await ssrEntry.renderHTML(rscStream)
  return new Response(htmlStream, {
    headers: { 'Content-type': 'text/html' },
  })
}

if (import.meta.hot) {
  import.meta.hot.accept()
}
