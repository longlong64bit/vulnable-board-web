import axios from 'axios'

// 빈 문자열이면 현재 origin(프론트 서버 5173) 기준으로 요청 → Vite proxy가 /api, /file 을 백엔드로 전달
const baseURL = ''

const client = axios.create({
  baseURL,
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
})

export default client
