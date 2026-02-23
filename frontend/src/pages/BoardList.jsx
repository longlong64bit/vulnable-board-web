import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { boardApi } from '../api/board'

function formatDate(s) {
  if (!s) return ''
  try {
    return new Date(s).toLocaleString('ko-KR')
  } catch {
    return s
  }
}

export default function BoardList() {
  const [list, setList] = useState([])
  const [keyword, setKeyword] = useState('')
  const [orderBy, setOrderBy] = useState('id DESC')

  async function fetchList() {
    try {
      const res = await boardApi.list({ keyword: keyword || undefined, orderBy })
      setList(res.data.list || [])
    } catch (e) {
      if (e.response?.status === 401) return
      setList([])
    }
  }

  useEffect(() => { fetchList() }, [])

  return (
    <div>
      <div className="card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
        <h2 style={{ margin: 0 }}>게시글 목록</h2>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <input value={keyword} onChange={(e) => setKeyword(e.target.value)} type="text" placeholder="검색" style={{ padding: '6px 10px', border: '1px solid #ddd', borderRadius: 4 }} onKeyDown={(e) => e.key === 'Enter' && fetchList()} />
          <select value={orderBy} onChange={(e) => setOrderBy(e.target.value)} style={{ padding: '6px 10px', border: '1px solid #ddd', borderRadius: 4 }}>
            <option value="id DESC">최신순</option>
            <option value="id ASC">오래된순</option>
            <option value="title ASC">제목순</option>
          </select>
          <button className="btn btn-primary" onClick={fetchList}>검색</button>
          <Link to="/board/write" className="btn btn-primary">글쓰기</Link>
        </div>
      </div>
      <div className="card">
        <table className="table">
          <thead>
            <tr>
              <th>번호</th>
              <th>제목</th>
              <th>작성자</th>
              <th>작성일</th>
            </tr>
          </thead>
          <tbody>
            {list.map((item) => (
              <tr key={item.id}>
                <td>{item.id}</td>
                <td><Link to={`/board/${item.id}`}>{item.title}</Link></td>
                <td>{item.writerId}</td>
                <td>{formatDate(item.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {list.length === 0 && <p style={{ textAlign: 'center', color: '#666' }}>글이 없습니다.</p>}
      </div>
    </div>
  )
}
