import { useState, useEffect } from 'react'
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { authApi } from './api/auth'

export default function App() {
  const location = useLocation()
  const navigate = useNavigate()
  const [userName, setUserName] = useState('')

  const noHeader = ['/login', '/join']
  const showHeader = !noHeader.includes(location.pathname)

  async function loadUser() {
    try {
      const res = await authApi.me()
      setUserName(res.data.success ? res.data.userName : '')
    } catch {
      setUserName('')
    }
  }

  async function logout(e) {
    e?.preventDefault()
    await authApi.logout()
    setUserName('')
    navigate('/login')
  }

  useEffect(() => { loadUser() }, [])
  useEffect(() => {
    const unsub = () => loadUser()
    return unsub
  }, [location.pathname])

  return (
    <div id="app">
      {showHeader && (
        <header className="header">
          <div className="container">
            <Link to="/board">{userName ? `${userName}님 게시판` : '게시판'}</Link>
            <nav>
              {userName ? (
                <>
                  <Link to="/board">목록</Link>
                  <Link to="/board/write">글쓰기</Link>
                  <a href="/rsc">RSC API</a>
                  <a href="#" onClick={logout}>로그아웃</a>
                </>
              ) : (
                <>
                  <Link to="/login">로그인</Link>
                  <Link to="/join">회원가입</Link>
                  <a href="/rsc">RSC API</a>
                </>
              )}
            </nav>
          </div>
        </header>
      )}
      <main className="container">
        <Outlet />
      </main>
    </div>
  )
}
