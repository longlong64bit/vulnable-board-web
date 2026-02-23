import { createFromReadableStream } from '@vitejs/plugin-rsc/ssr'
import React from 'react'
import { renderToReadableStream } from 'react-dom/server'
import { Root } from './Root.jsx'

/** CVE-2025-55182 테스트: 요청 body(RSC 스트림)를 역직렬화. 인증 없음. */
export async function deserializeRscStream(stream) {
  const payload = await createFromReadableStream(stream)
  return payload
}

export async function renderHTML(rscStream) {
  const payload = await createFromReadableStream(rscStream)
  const rootNode = payload?.root ?? <Root />
  const bootstrapScriptContent =
    await import.meta.viteRsc.loadBootstrapScriptContent('index')
  const htmlStream = await renderToReadableStream(rootNode, {
    bootstrapScriptContent,
  })
  return htmlStream
}
