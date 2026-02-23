import client from './client'

const base = client.defaults.baseURL || ''

export const fileApi = {
  upload(boardId, file) {
    const form = new FormData()
    form.append('boardId', boardId)
    form.append('file', file)
    return client.post('/file/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  downloadUrl(id) {
    return base + '/file/download/' + id
  },
}
