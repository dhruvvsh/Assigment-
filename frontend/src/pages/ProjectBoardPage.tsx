import { useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getErrorMessage, getStatus } from '../api/errors'
import { BOARD_PAGE_SIZE } from '../api/tasks'
import { BoardColumn } from '../components/BoardColumn'
import { Button } from '../components/Button'
import { EmptyState } from '../components/EmptyState'
import { ErrorState } from '../components/ErrorState'
import { FilterBar } from '../components/FilterBar'
import { Notice } from '../components/Notice'
import { Spinner } from '../components/Spinner'
import { SummaryBar } from '../components/SummaryBar'
import { TaskFormModal } from '../components/TaskFormModal'
import { useProjects } from '../hooks/useProjects'
import { useProjectSummary, useProjectTasks, useUpdateTaskStatus } from '../hooks/useTasks'
import { useUsers } from '../hooks/useUsers'
import { DEFAULT_FILTERS, hasActiveFilters, toTaskQuery } from '../lib/taskFilters'
import type { FilterState } from '../lib/taskFilters'
import { STATUSES } from '../lib/taskMeta'
import type { Task, TaskStatus } from '../types'
import styles from './Page.module.css'

export function ProjectBoardPage() {
  const projectId = Number(useParams().projectId)

  const [filters, setFilters] = useState<FilterState>(DEFAULT_FILTERS)
  const [creating, setCreating] = useState(false)
  const [editing, setEditing] = useState<Task | null>(null)

  const query = useMemo(() => toTaskQuery(filters), [filters])

  const projects = useProjects()
  const users = useUsers()
  const tasks = useProjectTasks(projectId, query)
  const summary = useProjectSummary(projectId)
  const moveTask = useUpdateTaskStatus(projectId)

  const project = projects.data?.find((p) => p.id === projectId)

  const tasksByStatus = useMemo(() => {
    const groups: Record<TaskStatus, Task[]> = { TODO: [], IN_PROGRESS: [], DONE: [] }
    tasks.data?.content.forEach((task) => groups[task.status].push(task))
    return groups
  }, [tasks.data])

  const backLink = (
    <Link to="/projects" className={styles.back}>
      ← All projects
    </Link>
  )

  // An id that is not a number, is not ours, or does not exist all look the same: not found.
  const notFound =
    !Number.isFinite(projectId) || getStatus(tasks.error) === 404 || (projects.data && !project)
  if (notFound) {
    return (
      <>
        {backLink}
        <EmptyState title="Project not found" description="It may have been removed, or it belongs to someone else." />
      </>
    )
  }

  if (projects.isPending || tasks.isPending) return <Spinner label="Loading board…" />

  const loadError = projects.error ?? tasks.error
  if (loadError) {
    return (
      <>
        {backLink}
        <ErrorState
          message={getErrorMessage(loadError)}
          onRetry={() => {
            projects.refetch()
            tasks.refetch()
            summary.refetch()
          }}
        />
      </>
    )
  }

  const handleStatusChange = (task: Task, status: TaskStatus) => {
    if (status !== task.status) moveTask.mutate({ taskId: task.id, status })
  }

  const newTaskButton = <Button onClick={() => setCreating(true)}>New task</Button>

  return (
    <>
      {backLink}
      <div className={styles.header}>
        <h1>{project?.name}</h1>
        {newTaskButton}
      </div>

      {summary.data && <SummaryBar summary={summary.data} />}
      {summary.isError && (
        <ErrorState message={`Could not load the summary. ${getErrorMessage(summary.error)}`} onRetry={() => summary.refetch()} />
      )}

      <FilterBar
        filters={filters}
        users={users.data ?? []}
        onChange={setFilters}
        onClear={() => setFilters(DEFAULT_FILTERS)}
      />
      {users.isError && (
        <Notice tone="warning">Could not load the people list, so the assignee filter is empty.</Notice>
      )}

      {moveTask.isError && (
        <Notice tone="error">Could not move the task. {getErrorMessage(moveTask.error)}</Notice>
      )}

      {tasks.data.totalElements > BOARD_PAGE_SIZE && (
        <Notice tone="warning">
          {tasks.data.totalElements} tasks match; the board shows the first {BOARD_PAGE_SIZE}.
        </Notice>
      )}

      {tasks.data.totalElements === 0 ? (
        hasActiveFilters(filters) ? (
          <EmptyState
            title="No tasks match your filters"
            action={
              <Button variant="secondary" onClick={() => setFilters(DEFAULT_FILTERS)}>
                Clear filters
              </Button>
            }
          />
        ) : (
          <EmptyState title="No tasks yet" description="Add the first task to this project." action={newTaskButton} />
        )
      ) : (
        <div className={styles.board}>
          {STATUSES.map((status) => (
            <BoardColumn
              key={status}
              status={status}
              tasks={tasksByStatus[status]}
              busyTaskId={moveTask.isPending ? moveTask.variables?.taskId : undefined}
              onStatusChange={handleStatusChange}
              onEdit={setEditing}
            />
          ))}
        </div>
      )}

      {(creating || editing) && (
        <TaskFormModal
          key={editing?.id ?? 'new'}
          projectId={projectId}
          task={editing ?? undefined}
          onClose={() => {
            setCreating(false)
            setEditing(null)
          }}
        />
      )}
    </>
  )
}
