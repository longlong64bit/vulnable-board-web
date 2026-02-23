import { lazy, Suspense, useState, useEffect } from 'react'
import { createBrowserRouter, Navigate, RouterProvider, useLocation } from 'react-router-dom'
import client from '../api/client'
import App from '../App'

const Login = lazy(() => import('../pages/Login'))
const Join = lazy(() => import('../pages/Join'))
const BoardList = lazy(() => import('../pages/BoardList'))
const BoardWrite = lazy(() => import('../pages/BoardWrite'))
const BoardView = lazy(() => import('../pages/BoardView'))
const BoardEdit = lazy(() => import('../pages/BoardEdit'))

function RequireAuth({ children }) {
  const [ok, setOk] = useState(null)
  useEffect(() => {
    client.get('/api/auth/me')
      .then((res) => setOk(res.data.success))
      .catch(() => setOk(false))
  }, [])
  if (ok === null) return <div>로딩 중...</div>
  if (!ok) return <Navigate to="/login" replace />
  return children
}

function GuestOnly({ children }) {
  const [ok, setOk] = useState(null)
  const location = useLocation()
  const isLogin = location.pathname === '/login'
  useEffect(() => {
    client.get('/api/auth/me')
      .then((res) => setOk(res.data.success))
      .catch(() => setOk(false))
  }, [])
  if (ok === null) return <div>로딩 중...</div>
  if (ok && isLogin) return <Navigate to="/board" replace />
  return children
}

function SuspenseWrap({ children }) {
  return <Suspense fallback={<div>로딩 중...</div>}>{children}</Suspense>
}

const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
      { index: true, element: <Navigate to="/board" replace /> },
      {
        path: 'login',
        element: (
          <GuestOnly>
            <SuspenseWrap><Login /></SuspenseWrap>
          </GuestOnly>
        ),
      },
      {
        path: 'join',
        element: (
          <GuestOnly>
            <SuspenseWrap><Join /></SuspenseWrap>
          </GuestOnly>
        ),
      },
      {
        path: 'board',
        element: (
          <RequireAuth>
            <SuspenseWrap><BoardList /></SuspenseWrap>
          </RequireAuth>
        ),
      },
      {
        path: 'board/write',
        element: (
          <RequireAuth>
            <SuspenseWrap><BoardWrite /></SuspenseWrap>
          </RequireAuth>
        ),
      },
      {
        path: 'board/:id',
        element: (
          <RequireAuth>
            <SuspenseWrap><BoardView /></SuspenseWrap>
          </RequireAuth>
        ),
      },
      {
        path: 'board/edit/:id',
        element: (
          <RequireAuth>
            <SuspenseWrap><BoardEdit /></SuspenseWrap>
          </RequireAuth>
        ),
      },
    ],
  },
])

export default function Router() {
  return <RouterProvider router={router} />
}
