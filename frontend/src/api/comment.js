import client from './client'

export const commentApi = {
  list(boardId) {
    return client.get('/api/comment/list', { params: { boardId } })
  },
  add(boardId, content) {
    return client.post('/api/comment/add', new URLSearchParams({ boardId, content }), {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    })
  },
  delete(id) {
    return client.post(`/api/comment/delete/${id}`)
  },
}
