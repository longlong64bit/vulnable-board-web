import rsc from '@vitejs/plugin-rsc'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [rsc(), react()],
  server: {
    host: '0.0.0.0',
    port: 5174,
    allowedHosts: ['frontend-rsc', 'localhost'],
  },
  environments: {
    rsc: {
      build: {
        rollupOptions: {
          input: { index: './src/entry.rsc.jsx' },
        },
      },
    },
    ssr: {
      build: {
        rollupOptions: {
          input: { index: './src/entry.ssr.jsx' },
        },
      },
    },
    client: {
      build: {
        rollupOptions: {
          input: { index: './src/entry.browser.jsx' },
        },
      },
    },
  },
})
