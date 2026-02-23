import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { boardApi } from '../api/board'
import { fileApi } from '../api/file'

export default function BoardWrite() {
  const navigate = useNavigate()
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [file, setFile] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function onFileChange(e) {
    setFile(e.target.files?.[0] ?? null)
  }

  async function submit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await boardApi.create({ title, content })
      const id = res.data.id
      if (file) {
        try {
          await fileApi.upload(id, file)
        } catch {}
      }
      navigate('/board/' + id)
    } catch (e) {
      if (e.response?.status === 401) navigate('/login', { replace: true })
      else setError(e.response?.data?.message || '등록에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="card">
      <h2 style={{ marginTop: 0 }}>글쓰기</h2>
      {error && <div className="alert alert-error">{error}</div>}
      <form onSubmit={submit}>
        <div className="form-group">
          <label>제목</label>
          <input value={title} onChange={(e) => setTitle(e.target.value)} type="text" required />
        </div>
        <div className="form-group">
          <label>내용</label>
          <textarea value={content} onChange={(e) => setContent(e.target.value)} />
        </div>
        <div className="form-group">
          <label>첨부파일 (선택)</label>
          <input type="file" onChange={onFileChange} />
        </div>
        <button type="submit" className="btn btn-primary" disabled={loading}>등록</button>
        <Link to="/board" className="btn btn-secondary" style={{ marginLeft: 8 }}>취소</Link>
      </form>
    </div>
  )
}
