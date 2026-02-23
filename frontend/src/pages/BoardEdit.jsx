import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { boardApi } from '../api/board'

export default function BoardEdit() {
  const { id } = useParams()
  const navigate = useNavigate()
  const boardId = Number(id)
  const [board, setBoard] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  async function load() {
    setLoading(true)
    try {
      const res = await boardApi.get(boardId)
      setBoard(res.data)
    } catch (e) {
      if (e.response?.status === 404) setBoard(null)
      else if (e.response?.status === 401) navigate('/login', { replace: true })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [boardId])

  async function submit(e) {
    e.preventDefault()
    setError('')
    setSaving(true)
    try {
      await boardApi.update(boardId, { title: board.title, content: board.content })
      navigate(`/board/${boardId}`)
    } catch (e) {
      if (e.response?.status === 401) navigate('/login', { replace: true })
      else setError(e.response?.data?.message || '저장에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div>로딩 중...</div>
  if (!board) return <div>글이 없습니다.</div>

  return (
    <div className="card">
      <h2 style={{ marginTop: 0 }}>글 수정</h2>
      {error && <div className="alert alert-error">{error}</div>}
      <form onSubmit={submit}>
        <div className="form-group">
          <label>제목</label>
          <input value={board.title} onChange={(e) => setBoard({ ...board, title: e.target.value })} type="text" required />
        </div>
        <div className="form-group">
          <label>내용</label>
          <textarea value={board.content} onChange={(e) => setBoard({ ...board, content: e.target.value })} />
        </div>
        <button type="submit" className="btn btn-primary" disabled={saving}>저장</button>
        <Link to={`/board/${board.id}`} className="btn btn-secondary" style={{ marginLeft: 8 }}>취소</Link>
      </form>
    </div>
  )
}
