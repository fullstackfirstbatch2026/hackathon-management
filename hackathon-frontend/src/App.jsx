import { useCallback, useEffect, useMemo, useState } from 'react'
import './App.css'

const sections = [
  { id: 'overview', label: 'Overview', icon: '◫' },
  { id: 'projects', label: 'Projects', icon: '▧' },
  { id: 'teams', label: 'Teams', icon: '♧' },
  { id: 'participants', label: 'Participants', icon: '♙' },
  { id: 'judges', label: 'Judges', icon: '✳' },
  { id: 'evaluations', label: 'Evaluations', icon: '▤' },
]

const resources = ['projects', 'teams', 'participants', 'judges', 'evaluations']
const singularNames = { projects: 'project', teams: 'team', participants: 'participant', judges: 'judge', evaluations: 'evaluation' }
const blankData = () => Object.fromEntries(resources.map((name) => [name, []]))
const fields = {
  projects: [
    { name: 'projectName', label: 'Project name', required: true, maxLength: 160, placeholder: 'e.g. Campus food exchange' },
    { name: 'teamId', label: 'Team', type: 'team', required: true },
    { name: 'technology', label: 'Technology', placeholder: 'e.g. React, Spring Boot' },
    { name: 'description', label: 'Description', type: 'textarea', placeholder: 'What is the team building?' },
  ],
  teams: [
    { name: 'teamName', label: 'Team name', required: true, placeholder: 'e.g. Code Warriors' },
    { name: 'description', label: 'Description', type: 'textarea', placeholder: 'A short description of the team' },
  ],
  participants: [
    { name: 'name', label: 'Full name', required: true },
    { name: 'email', label: 'Email', type: 'email', required: true },
    { name: 'college', label: 'College' },
    { name: 'department', label: 'Department' },
    { name: 'teamId', label: 'Team', type: 'team', required: true },
  ],
  judges: [
    { name: 'name', label: 'Full name', required: true },
    { name: 'email', label: 'Email', type: 'email', required: true },
    { name: 'expertise', label: 'Expertise', placeholder: 'e.g. Artificial Intelligence' },
  ],
  evaluations: [
    { name: 'projectId', label: 'Project', type: 'project', required: true },
    { name: 'judgeId', label: 'Judge', type: 'judge', required: true },
    { name: 'score', label: 'Score', type: 'number', min: 0, max: 100, step: 0.5, required: true },
    { name: 'comments', label: 'Comments', type: 'textarea', placeholder: 'Share constructive feedback' },
  ],
}

const columns = {
  projects: [['projectName', 'Project'], ['team.teamName', 'Team'], ['technology', 'Technology'], ['status', 'Status'], ['averageScore', 'Average']],
  teams: [['teamName', 'Team'], ['description', 'About']],
  participants: [['name', 'Name'], ['email', 'Email'], ['college', 'College'], ['department', 'Department'], ['team.teamName', 'Team']],
  judges: [['name', 'Name'], ['email', 'Email'], ['expertise', 'Expertise']],
  evaluations: [['project.projectName', 'Project'], ['judge.name', 'Judge'], ['score', 'Score'], ['comments', 'Feedback']],
}

const descriptions = {
  projects: 'Manage project submissions, details, and judging progress.',
  teams: 'Create teams and keep their details up to date.',
  participants: 'Manage participant details and team assignments.',
  judges: 'Manage judges and their areas of expertise.',
  evaluations: 'Review and manage project scores and judge feedback.',
}

function apiBase() {
  return (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/$/, '')
}

async function apiRequest(path, options) {
  const response = await fetch(`${apiBase()}/${path}`, options)
  const text = await response.text()
  if (!response.ok) {
    let message = text || `${response.status} ${response.statusText}`
    try {
      const parsed = JSON.parse(text)
      message = parsed.message || parsed.error || message
    } catch { /* The API sometimes returns plain-text error messages. */ }
    throw new Error(message)
  }
  if (!text) return null
  try { return JSON.parse(text) } catch { return text }
}

function atPath(object, path) {
  if (path === 'averageScore') return object.averageScore
  return path.split('.').reduce((value, key) => value?.[key], object)
}

function projectName(resource, record) {
  if (!record) return `New ${singularNames[resource]}`
  return record.projectName || record.teamName || record.name || record.id
}

function App() {
  const [active, setActive] = useState('overview')
  const [data, setData] = useState(blankData)
  const [details, setDetails] = useState([])
  const [aboveAverage, setAboveAverage] = useState([])
  const [averages, setAverages] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [insightError, setInsightError] = useState('')
  const [query, setQuery] = useState('')
  const [lastUpdated, setLastUpdated] = useState(null)
  const [editor, setEditor] = useState(null)
  const [projectView, setProjectView] = useState('list')
  const [busy, setBusy] = useState('')
  const [notice, setNotice] = useState(null)
  const [openMenu, setOpenMenu] = useState('')
  const [currentDate] = useState(() => new Date())

  const loadData = useCallback(async () => {
    setLoading(true)
    setError('')
    const listResults = await Promise.allSettled(resources.map((resource) => apiRequest(resource)))
    const nextData = blankData()
    const failures = []
    listResults.forEach((result, index) => {
      const resource = resources[index]
      if (result.status === 'fulfilled' && Array.isArray(result.value)) nextData[resource] = result.value
      else failures.push(result.status === 'rejected' ? `${resource}: ${result.reason.message}` : `${resource}: invalid list response`)
    })
    setData(nextData)
    setError(failures.join(' · '))

    const advanced = await Promise.allSettled([
      apiRequest('projects/details'),
      apiRequest('projects/above-average'),
      ...nextData.projects.map((project) => apiRequest(`projects/${project.id}/average-score`)),
    ])
    const detailResult = advanced[0]
    const aboveResult = advanced[1]
    const nextAverages = {}
    advanced.slice(2).forEach((result, index) => {
      if (result.status === 'fulfilled') nextAverages[nextData.projects[index].id] = result.value
    })
    setDetails(detailResult.status === 'fulfilled' && Array.isArray(detailResult.value) ? detailResult.value : [])
    setAboveAverage(aboveResult.status === 'fulfilled' && Array.isArray(aboveResult.value) ? aboveResult.value : aboveResult.status === 'fulfilled' && aboveResult.value ? [aboveResult.value] : [])
    setAverages(nextAverages)
    const extraFailures = [detailResult, aboveResult, ...advanced.slice(2)].filter((result) => result.status === 'rejected')
    setInsightError(extraFailures.length ? 'One or more database insight endpoints could not be loaded.' : '')
    setLastUpdated(new Date())
    setLoading(false)
  }, [])

  useEffect(() => {
    const timer = window.setTimeout(loadData, 0)
    return () => window.clearTimeout(timer)
  }, [loadData])

  const statusCounts = useMemo(() => {
    const counts = { DRAFT: 0, SUBMITTED: 0, EVALUATED: 0, OTHER: 0 }
    data.projects.forEach(({ status = '' }) => {
      const key = String(status).toUpperCase()
      if (Object.hasOwn(counts, key) && key !== 'OTHER') counts[key] += 1
      else counts.OTHER += 1
    })
    return counts
  }, [data.projects])

  const title = active === 'overview' ? 'Overview' : active[0].toUpperCase() + active.slice(1)
  const rows = useMemo(() => active === 'overview' ? data.projects : data[active] || [], [active, data])
  const filteredRows = useMemo(() => {
    const list = rows.map((row) => active === 'projects' || active === 'overview' ? { ...row, averageScore: averages[row.id] } : row)
    if (!query.trim()) return list
    const term = query.toLowerCase()
    return list.filter((row) => JSON.stringify(row).toLowerCase().includes(term))
  }, [rows, averages, query, active])
  const averageOverall = data.evaluations.length
    ? (data.evaluations.reduce((total, item) => total + Number(item.score || 0), 0) / data.evaluations.length).toFixed(1)
    : '—'

  async function saveRecord(event) {
    event.preventDefault()
    const { resource, record } = editor
    const values = new FormData(event.currentTarget)
    const payload = Object.fromEntries(fields[resource].filter((field) => !['teamId', 'projectId', 'judgeId'].includes(field.name)).map((field) => [field.name, values.get(field.name) || null]))
    if (resource === 'projects') {
      payload.status = record?.status || 'DRAFT'
      payload.team = { id: Number(values.get('teamId')) }
    }
    if (resource === 'participants') payload.team = { id: Number(values.get('teamId')) }
    if (resource === 'evaluations') {
      payload.score = Number(values.get('score'))
      payload.project = { id: Number(values.get('projectId')) }
      payload.judge = { id: Number(values.get('judgeId')) }
    }
    setBusy(`save-${resource}`)
    try {
      await apiRequest(`${resource}${record ? `/${record.id}` : ''}`, {
        method: record ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })
      setEditor(null)
      setNotice({ kind: 'success', text: `${singularNames[resource]} ${record ? 'updated' : 'created'} successfully.` })
      await loadData()
    } catch (cause) {
      setEditor((current) => ({ ...current, error: cause.message }))
    } finally {
      setBusy('')
    }
  }

  async function deleteRecord(resource, record) {
    if (!window.confirm(`Delete ${projectName(resource, record)}? This cannot be undone.`)) return
    setBusy(`delete-${resource}-${record.id}`)
    try {
      await apiRequest(`${resource}/${record.id}`, { method: 'DELETE' })
      setNotice({ kind: 'success', text: `${singularNames[resource]} deleted.` })
      await loadData()
    } catch (cause) {
      setNotice({ kind: 'error', text: `Could not delete ${projectName(resource, record)}: ${cause.message}` })
    } finally {
      setBusy('')
    }
  }

  async function submitProject(project) {
    if (String(project.status).toUpperCase() !== 'DRAFT') return
    setBusy(`submit-${project.id}`)
    try {
      await apiRequest(`projects/${project.id}/submit`, { method: 'POST' })
      setNotice({ kind: 'success', text: `${project.projectName} submitted for judging.` })
      await loadData()
    } catch (cause) {
      setNotice({ kind: 'error', text: `Could not submit project: ${cause.message}` })
    } finally {
      setBusy('')
    }
  }

  const openNew = (resource) => { setEditor({ resource, record: null, error: '' }); setNotice(null) }
  const openEdit = (resource, record) => { setEditor({ resource, record, error: '' }); setNotice(null) }
  const newLabel = active === 'overview' ? 'New project' : `New ${singularNames[active]}`
  const newResource = active === 'overview' ? 'projects' : active

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <a className="brand" href="#overview" onClick={(event) => { event.preventDefault(); setActive('overview') }}><span className="brand-mark">H</span><span><strong>huddle</strong><small>HACKATHON CONSOLE</small></span></a>
        <button className="event-switcher" aria-expanded={openMenu === 'workspace'} onClick={() => setOpenMenu(openMenu === 'workspace' ? '' : 'workspace')}><span className="event-glyph">✦</span><span className="event-copy"><b>Hackathon workspace</b><small>Event overview</small></span><span className="chevron">⌄</span></button>
        {openMenu === 'workspace' && <div className="side-popover"><span className="popover-label">CURRENT WORKSPACE</span><button onClick={() => { setActive('overview'); setOpenMenu('') }}>✦ &nbsp;Hackathon management</button><button onClick={() => { setActive('projects'); setProjectView('list'); setOpenMenu('') }}>▧ &nbsp;Browse projects</button></div>}
        <div className="nav-label">WORKSPACE</div>
        <nav aria-label="Main navigation">{sections.map((section) => <button key={section.id} className={`nav-item ${active === section.id ? 'active' : ''}`} onClick={() => { setActive(section.id); setQuery(''); setOpenMenu('') }}><span className="nav-icon" aria-hidden="true">{section.icon}</span><span>{section.label}</span>{section.id === 'projects' && data.projects.length > 0 && <span className="nav-count">{data.projects.length}</span>}</button>)}</nav>
        <div className="sidebar-bottom"><button className="help-card" onClick={() => setNotice({ kind: 'info', text: 'Use the workspace navigation to manage records. API errors are shown at the top of the page.' })}><span className="help-icon">?</span><span><b>Need a hand?</b><small>Organizer quick help</small></span><span className="arrow">↗</span></button><button className="profile" aria-expanded={openMenu === 'profile'} onClick={() => setOpenMenu(openMenu === 'profile' ? '' : 'profile')}><span className="avatar">OR</span><span><b>Organizer</b><small>Workspace admin</small></span><span className="more">···</span></button>{openMenu === 'profile' && <div className="side-popover profile-popover"><span className="popover-label">ORGANIZER</span><p>Local workspace session</p><button onClick={() => { setOpenMenu(''); loadData() }}>↻ &nbsp;Refresh API data</button></div>}</div>
      </aside>

      <main className="main-content">
        <header className="topbar"><div className="breadcrumbs"><span>Hackathon workspace</span><span className="crumb-sep">/</span><b>{title}</b></div><div className="top-actions"><span className={`connection ${error ? 'offline' : loading ? 'loading' : 'online'}`}><i />{error ? 'Connection issue' : loading ? 'Connecting' : 'Live data'}</span><button className="icon-button" aria-label="Notifications" onClick={() => setNotice({ kind: 'info', text: 'You are all caught up.' })}>♧</button><span className="top-avatar">OR</span></div></header>

        <div className="page-wrap">
          {error && <div className="notice" role="status"><span className="notice-mark">!</span><div><b>Some records could not be loaded</b><small>{error}</small></div><button onClick={loadData}>Try again</button></div>}
          {notice && <div className={`notice ${notice.kind === 'success' ? 'notice-success' : notice.kind === 'error' ? 'notice-error' : ''}`} role="status"><span className="notice-mark">{notice.kind === 'success' ? '✓' : notice.kind === 'error' ? '!' : 'i'}</span><div><b>{notice.kind === 'success' ? 'Done' : notice.kind === 'error' ? 'Action failed' : 'Workspace'}</b><small>{notice.text}</small></div><button aria-label="Dismiss notification" onClick={() => setNotice(null)}>×</button></div>}
          <div className="page-heading"><div><div className="eyebrow">{currentDate.toLocaleDateString(undefined, { weekday: 'long', month: 'long', day: '2-digit', year: 'numeric' }).toUpperCase()}</div><h1>{active === 'overview' ? 'Hackathon overview' : title}</h1><p>{active === 'overview' ? 'A live view of projects, teams, and judging progress.' : descriptions[active]}</p></div><div className="heading-actions"><button className="secondary-button" onClick={loadData} disabled={loading}><span className={loading ? 'spin' : ''}>↻</span> Refresh</button><button className="primary-button" onClick={() => openNew(newResource)}>＋ <span>{newLabel}</span></button></div></div>

          {active === 'overview' ? <Overview data={data} averages={averages} loading={loading} statusCounts={statusCounts} averageOverall={averageOverall} aboveAverage={aboveAverage} insightError={insightError} details={details} onNavigate={(page, subView) => { setActive(page); if (subView) setProjectView(subView); setQuery('') }} rows={filteredRows.slice(0, 5)} onEdit={openEdit} onDelete={deleteRecord} onSubmit={submitProject} busy={busy} query={query} setQuery={setQuery} /> : active === 'projects' ? <ProjectsPage data={data} rows={filteredRows} averages={averages} details={details} aboveAverage={aboveAverage} insightError={insightError} loading={loading} view={projectView} setView={setProjectView} onEdit={openEdit} onDelete={deleteRecord} onSubmit={submitProject} busy={busy} query={query} setQuery={setQuery} /> : <ResourcePage resource={active} rows={filteredRows} loading={loading} query={query} setQuery={setQuery} onEdit={openEdit} onDelete={deleteRecord} busy={busy} />}
          <footer className="page-footer"><span>Huddle · Hackathon operations</span><span>{lastUpdated ? `Last synced ${lastUpdated.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' })}` : 'Waiting for first sync'}</span></footer>
        </div>
        {editor && <RecordModal editor={editor} data={data} busy={busy} onClose={() => setEditor(null)} onSubmit={saveRecord} />}
      </main>
    </div>
  )
}

function Overview({ data, averages, loading, statusCounts, averageOverall, aboveAverage, insightError, details, onNavigate, rows, onEdit, onDelete, onSubmit, busy, query, setQuery }) {
  const total = data.projects.length || 1
  const evaluatedShare = (statusCounts.SUBMITTED + statusCounts.EVALUATED) / total * 100
  const participantCount = new Set(details.map((row) => row.participantId)).size
  return <>
    <section className="stat-grid" aria-label="Hackathon statistics"><StatCard label="PROJECTS" value={data.projects.length} note="Ideas in the showcase" icon="▧" tone="lavender" loading={loading} /><StatCard label="TEAMS" value={data.teams.length} note="Teams registered" icon="♧" tone="mint" loading={loading} /><StatCard label="PARTICIPANTS" value={data.participants.length} note="Builders on the floor" icon="♙" tone="peach" loading={loading} /><StatCard label="JUDGES" value={data.judges.length} note="Ready to review" icon="✳" tone="blue" loading={loading} /></section>
    <section className="overview-grid">
      <div className="panel progress-panel"><div className="panel-heading"><div><span className="section-kicker">PROJECT LIFECYCLE</span><h2>Submission progress</h2></div><span className="live-badge"><i /> LIVE</span></div><div className="progress-summary"><strong>{Math.round(evaluatedShare)}<small>%</small></strong><span>submitted or evaluated</span></div><div className="progress-track"><span style={{ width: `${evaluatedShare}%` }} /></div><div className="status-counts"><StatusCount status="DRAFT" count={statusCounts.DRAFT} /><StatusCount status="SUBMITTED" count={statusCounts.SUBMITTED} /><StatusCount status="EVALUATED" count={statusCounts.EVALUATED} /></div><div className="progress-foot"><span>Average evaluation score</span><b>{averageOverall}{averageOverall === '—' ? '' : ' / 100'}</b></div></div>
      <div className="panel"><div className="panel-heading"><div><span className="section-kicker">ASSIGNMENT INSIGHTS</span><h2>Above-average projects</h2></div><button className="text-button" onClick={() => onNavigate('projects', 'above')}>Open projects <span>→</span></button></div>{insightError && <div className="mini-error">An insight endpoint could not be reached.</div>}{aboveAverage.length ? <ul className="insight-list">{aboveAverage.slice(0, 3).map((item) => <li key={item.projectId}><span className="insight-trophy">✳</span><span><b>{item.projectName}</b><small>Above overall evaluation average</small></span></li>)}</ul> : <div className="insight-empty">{loading ? 'Loading analysis…' : 'No projects are currently above the overall average.'}</div>}<div className="insight-foot"><span>Join query rows</span><b>{details.length} participants across projects</b><span className="soft-count">{participantCount}</span></div></div>
    </section>
    <section className="panel projects-panel"><div className="panel-heading project-list-heading"><div><span className="section-kicker">THE SHOWCASE</span><h2>Recent projects <span className="soft-count">{data.projects.length}</span></h2></div><div className="list-tools"><label className="search-box"><span>⌕</span><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search projects" aria-label="Search projects" /></label><button className="text-button" onClick={() => onNavigate('projects', 'list')}>View all <span>→</span></button></div></div><DataTable resource="projects" rows={rows} loading={loading} columns={columns.projects} averages={averages} onEdit={onEdit} onDelete={onDelete} onSubmit={onSubmit} busy={busy} /></section>
  </>
}

function StatusCount({ status, count }) { return <div className="status-count"><span className={`status-dot ${status.toLowerCase()}`} /><span>{status[0] + status.slice(1).toLowerCase()}</span><b>{count}</b></div> }

function ProjectsPage({ data, rows, averages, details, aboveAverage, insightError, loading, view, setView, onEdit, onDelete, onSubmit, busy, query, setQuery }) {
  const grouped = details.reduce((groups, item) => {
    const group = groups.get(item.projectId) || { ...item, participants: [] }
    group.participants.push(item)
    groups.set(item.projectId, group)
    return groups
  }, new Map())
  return <section className="panel resource-panel"><div className="panel-heading project-list-heading"><div><span className="section-kicker">PROJECTS &amp; DATABASE QUERIES</span><h2>Showcase <span className="soft-count">{data.projects.length}</span></h2></div><label className="search-box"><span>⌕</span><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search projects" aria-label="Search projects" /></label></div><div className="view-tabs" role="tablist" aria-label="Project views"><button className={view === 'list' ? 'selected' : ''} onClick={() => setView('list')}>Projects</button><button className={view === 'details' ? 'selected' : ''} onClick={() => setView('details')}>Project details · JOIN</button><button className={view === 'above' ? 'selected' : ''} onClick={() => setView('above')}>Above average</button></div>
    {view === 'list' && <DataTable resource="projects" rows={rows} loading={loading} columns={columns.projects} averages={averages} onEdit={onEdit} onDelete={onDelete} onSubmit={onSubmit} busy={busy} />}
    {view === 'details' && <div className="join-list">{insightError && <div className="mini-error">The JOIN query could not be loaded.</div>}{loading && !details.length ? <div className="insight-empty">Loading project details…</div> : grouped.size ? [...grouped.values()].map((project) => <article className="join-card" key={project.projectId}><div className="join-project"><span className="project-thumb">{project.projectName.slice(0, 1)}</span><div><b>{project.projectName}</b><small>{project.teamName} · {project.participants.length} participant{project.participants.length === 1 ? '' : 's'}</small></div></div><div className="participant-chips">{project.participants.map((person) => <span key={person.participantId}><b>{person.participantName}</b><small>{person.participantEmail}</small></span>)}</div></article>) : <div className="insight-empty">No matching JOIN rows. Projects need participants assigned to their teams.</div>}</div>}
    {view === 'above' && <div className="above-list">{insightError && <div className="mini-error">The above-average query could not be loaded.</div>}{loading && !aboveAverage.length ? <div className="insight-empty">Loading analysis…</div> : aboveAverage.length ? aboveAverage.map((item) => { const project = data.projects.find((row) => row.id === item.projectId); return <article className="above-card" key={item.projectId}><span className="insight-trophy">✳</span><div><b>{item.projectName}</b><small>Score {averages[item.projectId] ?? '—'} / 100 · above the average score for all evaluations</small></div>{project && <StatusBadge status={project.status} />}</article> }) : <div className="insight-empty">No projects are currently above the overall evaluation average.</div>}</div>}
  </section>
}

function ResourcePage({ resource, rows, loading, query, setQuery, onEdit, onDelete, busy }) {
  return <section className="panel resource-panel"><div className="panel-heading project-list-heading"><div><span className="section-kicker">HACKATHON DIRECTORY</span><h2>{resource[0].toUpperCase() + resource.slice(1)} <span className="soft-count">{rows.length}</span></h2></div><label className="search-box"><span>⌕</span><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder={`Search ${resource}`} aria-label={`Search ${resource}`} /></label></div><DataTable resource={resource} rows={rows} loading={loading} columns={columns[resource]} onEdit={onEdit} onDelete={onDelete} busy={busy} /></section>
}

function DataTable({ resource, rows, loading, columns: tableColumns, averages = {}, onEdit, onDelete, onSubmit, busy = '' }) {
  const canMutate = Boolean(onEdit && onDelete)
  return <div className="table-wrap"><table><thead><tr>{tableColumns.map(([, label]) => <th key={label}>{label}</th>)}{canMutate && <th className="actions-heading">Actions</th>}</tr></thead><tbody>
    {loading && rows.length === 0 && <tr><td className="table-empty" colSpan={tableColumns.length + Number(canMutate)}>Loading records…</td></tr>}
    {!loading && rows.length === 0 && <tr><td className="table-empty" colSpan={tableColumns.length + Number(canMutate)}>No records found.</td></tr>}
    {rows.map((row) => <tr key={row.id}>{tableColumns.map(([path]) => {
      const value = path === 'averageScore' ? averages[row.id] ?? row.averageScore : atPath(row, path)
      if (path === 'status') return <td key={path}><StatusBadge status={value} /></td>
      if (path === 'score' || path === 'averageScore') return <td key={path}><span className="score-cell">{value ?? '—'}<small>{value == null ? '' : ' / 100'}</small></span></td>
      if (path === 'projectName') return <td key={path}><span className="project-cell"><span className="project-thumb">{String(value || 'P').slice(0, 1).toUpperCase()}</span><b>{value || 'Untitled project'}</b></span></td>
      return <td key={path}><span className="cell-text" title={value ?? ''}>{value || <span className="muted-value">—</span>}</span></td>
    })}{canMutate && <td className="row-actions"><button onClick={() => onEdit(resource, row)} aria-label={`Edit ${projectName(resource, row)}`}>Edit</button>{resource === 'projects' && String(row.status).toUpperCase() === 'DRAFT' && <button className="submit-action" disabled={busy === `submit-${row.id}`} onClick={() => onSubmit(row)}>{busy === `submit-${row.id}` ? 'Submitting…' : 'Submit'}</button>}<button className="delete-action" disabled={busy === `delete-${resource}-${row.id}`} onClick={() => onDelete(resource, row)}>{busy === `delete-${resource}-${row.id}` ? 'Deleting…' : 'Delete'}</button></td>}</tr>)}
  </tbody></table></div>
}

function StatusBadge({ status }) {
  const value = String(status || 'UNKNOWN').toUpperCase()
  const tone = value === 'EVALUATED' ? 'evaluated' : value === 'SUBMITTED' ? 'submitted' : value === 'DRAFT' ? 'draft' : 'unknown'
  const label = value === 'DRAFT' ? 'Draft' : value === 'SUBMITTED' ? 'Submitted' : value === 'EVALUATED' ? 'Evaluated' : value
  return <span className={`status-pill status-${tone}`}><i />{label}</span>
}

function RecordModal({ editor, data, busy, onClose, onSubmit }) {
  const { resource, record, error } = editor
  const optionsFor = (field) => {
    if (field.type === 'team') {
      const available = resource === 'projects'
        ? data.teams.filter((team) => !data.projects.some((project) => project.team?.id === team.id && project.id !== record?.id))
        : data.teams
      return available.map((item) => [item.id, item.teamName])
    }
    if (field.type === 'project') return data.projects.map((item) => [item.id, item.projectName])
    if (field.type === 'judge') return data.judges.map((item) => [item.id, item.name])
    return []
  }
  const initialValue = (field) => {
    if (field.name === 'teamId') return record?.team?.id ?? ''
    if (field.name === 'projectId') return record?.project?.id ?? ''
    if (field.name === 'judgeId') return record?.judge?.id ?? ''
    return record?.[field.name] ?? ''
  }
  const busyNow = busy === `save-${resource}`
  const missingRelation = fields[resource].filter((field) => ['team', 'project', 'judge'].includes(field.type)).some((field) => optionsFor(field).length === 0)
  return <div className="modal-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget && !busyNow) onClose() }}><section className="project-modal" role="dialog" aria-modal="true" aria-labelledby="record-modal-title"><div className="modal-heading"><div><span className="section-kicker">HACKATHON DIRECTORY</span><h2 id="record-modal-title">{record ? `Edit ${singularNames[resource]}` : `Create ${singularNames[resource]}`}</h2></div><button className="modal-close" onClick={onClose} aria-label="Close" disabled={busyNow}>×</button></div><p className="modal-intro">{resource === 'evaluations' ? 'Record a judge’s score and feedback for a project.' : `Enter the details for this ${singularNames[resource]}.`}</p>{error && <div className="form-error" role="alert">{error}</div>}{resource === 'projects' && <div className="form-hint">New projects start as DRAFT. Submit them for judging after creation; evaluations set the EVALUATED status.</div>}{missingRelation && <div className="form-hint">Add the required {resource === 'evaluations' ? 'projects and judges' : 'teams'} first.</div>}<form onSubmit={onSubmit} key={`${resource}-${record?.id || 'new'}`}>{fields[resource].map((field) => <label key={field.name}>{field.label}{['team', 'project', 'judge'].includes(field.type) ? <select name={field.name} required={field.required} defaultValue={initialValue(field)} disabled={!optionsFor(field).length}><option value="" disabled>Select {field.label.toLowerCase()}</option>{optionsFor(field).map(([id, label]) => <option value={id} key={id}>{label}</option>)}</select> : field.type === 'textarea' ? <textarea name={field.name} rows="3" defaultValue={initialValue(field)} placeholder={field.placeholder} /> : <input name={field.name} type={field.type || 'text'} defaultValue={initialValue(field)} required={field.required} maxLength={field.maxLength} min={field.min} max={field.max} step={field.step} placeholder={field.placeholder} />}</label>)}<div className="modal-actions"><button type="button" className="secondary-button" onClick={onClose} disabled={busyNow}>Cancel</button><button type="submit" className="primary-button" disabled={busyNow || missingRelation}>{busyNow ? 'Saving…' : record ? 'Save changes' : `Create ${singularNames[resource]}`}</button></div></form></section></div>
}

function StatCard({ label, value, note, icon, tone, loading }) {
  return <article className="stat-card"><div className="stat-top"><span>{label}</span><span className={`stat-icon ${tone}`}>{icon}</span></div><div className="stat-value">{loading ? <i className="skeleton" /> : value}</div><div className="stat-note">{note}</div><span className={`stat-spark ${tone}`} aria-hidden="true"><i /><i /><i /><i /><i /><i /><i /><i /><i /><i /><i /><i /></span></article>
}

export default App
