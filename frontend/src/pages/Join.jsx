import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from '../api/auth'

export default function Join() {
  const navigate = useNavigate()
  const [userId, setUserId] = useState('')
  const [password, setPassword] = useState('')
  const [name, setName] = useState('')
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)

  async function submit(e) {
    e.preventDefault()
    setError('')
    setMessage('')
    setLoading(true)
    try {
      const res = await authApi.join(userId, password, name)
      if (res.data.success) {
        setMessage(res.data.message)
        setTimeout(() => navigate('/login'), 1500)
      } else {
        setError(res.data.message || '가입에 실패했습니다.')
      }
    } catch {
      setError('서버 오류가 발생했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="card" style={{ maxWidth: 400, margin: '40px auto' }}>
      <h2 style={{ marginTop: 0 }}>회원가입</h2>
      {error && <div className="alert alert-error">{error}</div>}
      {message && <div className="alert alert-success">{message}</div>}
      <form onSubmit={submit}>
        <div className="form-group">
          <label>아이디</label>
          <input value={userId} onChange={(e) => setUserId(e.target.value)} type="text" required autoComplete="username" />
        </div>
        <div className="form-group">
          <label>비밀번호</label>
          <input value={password} onChange={(e) => setPassword(e.target.value)} type="password" required autoComplete="new-password" />
        </div>
        <div className="form-group">
          <label>이름 (선택)</label>
          <input value={name} onChange={(e) => setName(e.target.value)} type="text" autoComplete="name" />
        </div>
        <button type="submit" className="btn btn-primary" disabled={loading}>가입하기</button>
        <Link to="/login" className="btn btn-secondary" style={{ marginLeft: 8 }}>로그인</Link>
      </form>
    </div>
  )
}
