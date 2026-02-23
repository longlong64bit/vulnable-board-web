import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { boardApi } from '../api/board'
import { commentApi } from '../api/comment'
import { fileApi } from '../api/file'

function formatDate(s) {
  if (!s) return ''
  try {
    return new Date(s).toLocaleString('ko-KR')
  } catch {
    return s
  }
}

export default function BoardView() {
  const { id } = useParams()
  const navigate = useNavigate()
  const boardId = Number(id)
  const [board, setBoard] = useState(null)
  const [comments, setComments] = useState([])
  const [newComment, setNewComment] = useState('')
  const [loading, setLoading] = useState(true)

  async function fetchBoard() {
    setLoading(true)
    try {
      const res = await boardApi.get(boardId)
      setBoard(res.data)
      setComments(res.data.comments || [])
    } catch (e) {
      if (e.response?.status === 404) setBoard(null)
      else if (e.response?.status === 401) navigate('/login', { replace: true })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchBoard() }, [boardId])

  async function addComment() {
    if (!newComment.trim()) return
    try {
      await commentApi.add(boardId, newComment)
      const listRes = await commentApi.list(boardId)
      setComments(listRes.data)
      setNewComment('')
    } catch (e) {
      if (e.response?.status === 401) navigate('/login', { replace: true })
    }
  }

  async function deleteComment(cid) {
    try {
      await commentApi.delete(cid)
      const listRes = await commentApi.list(boardId)
      setComments(listRes.data)
    } catch (e) {
      if (e.response?.status === 401) navigate('/login', { replace: true })
    }
  }

  async function remove() {
    if (!window.confirm('삭제하시겠습니까?')) return
    try {
      await boardApi.delete(boardId)
      navigate('/board')
    } catch (e) {
      if (e.response?.status === 401) navigate('/login', { replace: true })
    }
  }

  if (loading) return <div>로딩 중...</div>
  if (!board) return <div>글이 없습니다.</div>

  return (
    <>
      <div className="card">
        <h2 style={{ marginTop: 0 }}>{board.title}</h2>
        <p style={{ color: '#666' }}>작성자: {board.writerId} | {formatDate(board.createdAt)}</p>
        <div style={{ whiteSpace: 'pre-wrap' }}>{board.content}</div>
        {board.attachments?.length > 0 && (
          <div style={{ marginTop: 16 }}>
            <strong>첨부파일:</strong>
            <ul style={{ margin: '4px 0' }}>
              {board.attachments.map((a) => (
                <li key={a.id}>
                  <a href={fileApi.downloadUrl(a.id)} target="_blank" rel="noopener noreferrer">{a.originalName}</a>
                </li>
              ))}
            </ul>
          </div>
        )}
        <div style={{ marginTop: 20 }}>
          <Link to={'/board/edit/' + board.id} className="btn btn-secondary">수정</Link>
          <button type="button" className="btn btn-danger" style={{ marginLeft: 8 }} onClick={remove}>삭제</button>
          <Link to="/board" className="btn btn-secondary" style={{ marginLeft: 8 }}>목록</Link>
        </div>
      </div>
      <div className="card">
        <h3>댓글</h3>
        <div className="form-group">
          <textarea value={newComment} onChange={(e) => setNewComment(e.target.value)} placeholder="댓글 입력" rows={2} />
          <button type="button" className="btn btn-primary" style={{ marginTop: 8 }} onClick={addComment} disabled={!newComment.trim()}>등록</button>
        </div>
        <ul style={{ listStyle: 'none', padding: 0 }}>
          {comments.map((c) => (
            <li key={c.id} style={{ padding: '12px 0', borderBottom: '1px solid #eee' }}>
              <strong>{c.writerId}</strong> {formatDate(c.createdAt)}
              <div style={{ marginTop: 4 }}>{c.content}</div>
              <button type="button" className="btn btn-secondary" style={{ marginTop: 4, fontSize: 12 }} onClick={() => deleteComment(c.id)}>삭제</button>
            </li>
          ))}
        </ul>
      </div>
    </>
  )
}
