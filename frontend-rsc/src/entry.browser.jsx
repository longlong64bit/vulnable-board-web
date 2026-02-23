import { createFromReadableStream } from '@vitejs/plugin-rsc/browser'
import { hydrateRoot } from 'react-dom/client'

async function main() {
  const rscUrl = window.location.pathname + (window.location.search || '') + (window.location.search ? '&' : '?') + 'rsc'
  const rscResponse = await fetch(rscUrl)
  const payload = await createFromReadableStream(rscResponse.body)
  const rootNode = payload?.root ?? null
  if (rootNode) {
    hydrateRoot(document.getElementById('root'), rootNode)
  }
}

main()
