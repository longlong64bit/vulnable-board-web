import client from './client'

export const authApi = {
  login(userId, password) {
    return client.post('/api/auth/login', { userId, password })
  },
  join(userId, password, name) {
    return client.post('/api/auth/join', { userId, password, name })
  },
  logout() {
    return client.post('/api/auth/logout')
  },
  me() {
    return client.get('/api/auth/me')
  },
}
