export function Root() {
  return (
    <div style={{ padding: 20, fontFamily: 'sans-serif' }}>
      <h1>RSC CVE-2025-55182 Test Server</h1>
      <p>React 19.2.0 + @vitejs/plugin-rsc 0.5.2 (vulnerable)</p>
      <p>Append <code>.rsc</code> to the URL to get the RSC stream.</p>
    </div>
  )
}
