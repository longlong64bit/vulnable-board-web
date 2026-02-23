import client from './client'

export const boardApi = {
  list(params = {}) {
    return client.get('/api/board/list', { params })
  },
  get(id) {
    return client.get(`/api/board/${id}`)
  },
  create(data) {
    return client.post('/api/board', data)
  },
  update(id, data) {
    return client.put(`/api/board/${id}`, data)
  },
  delete(id) {
    return client.delete(`/api/board/${id}`)
  },
}
