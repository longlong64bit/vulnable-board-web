import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from '../api/auth'

export default function Login() {
  const navigate = useNavigate()
  const [userId, setUserId] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function submit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await authApi.login(userId, password)
      if (res.data.success) {
        navigate('/board')
      } else {
        setError(res.data.message || '로그인에 실패했습니다.')
      }
    } catch {
      setError('서버 오류가 발생했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="card" style={{ maxWidth: 400, margin: '40px auto' }}>
      <h2 style={{ marginTop: 0 }}>로그인</h2>
      {error && <div className="alert alert-error">{error}</div>}
      <form onSubmit={submit}>
        <div className="form-group">
          <label>아이디</label>
          <input value={userId} onChange={(e) => setUserId(e.target.value)} type="text" required autoComplete="username" />
        </div>
        <div className="form-group">
          <label>비밀번호</label>
          <input value={password} onChange={(e) => setPassword(e.target.value)} type="password" required autoComplete="current-password" />
        </div>
        <button type="submit" className="btn btn-primary" disabled={loading}>로그인</button>
        <Link to="/join" className="btn btn-secondary" style={{ marginLeft: 8 }}>회원가입</Link>
      </form>
    </div>
  )
}
