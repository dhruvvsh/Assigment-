import { useMemo } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getErrorMessage, getStatus } from '../api/errors'
import { BOARD_PAGE_SIZE } from '../api/tasks'
import { BoardColumn } from '../components/BoardColumn'
import { EmptyState } from '../components/EmptyState'
import { ErrorState } from '../components/ErrorState'
import { Notice } from '../components/Notice'
import { Spinner } from '../components/Spinner'
import { SummaryBar } from '../components/SummaryBar'
import { useProjects } from '../hooks/useProjects'
import { useProjectSummary, useProjectTasks, useUpdateTaskStatus } from '../hooks/useTasks'
import { STATUSES } from '../lib/taskMeta'
import type { Task, TaskStatus } from '../types'
import styles from './Page.module.css'

export function ProjectBoardPage() {
  const projectId = Number(useParams().projectId)

  const projects = useProjects()
  const tasks = useProjectTasks(projectId)
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

  return (
    <>
      {backLink}
      <div className={styles.header}>
        <h1>{project?.name}</h1>
      </div>

      {summary.data && <SummaryBar summary={summary.data} />}
      {summary.isError && (
        <ErrorState message={`Could not load the summary. ${getErrorMessage(summary.error)}`} onRetry={() => summary.refetch()} />
      )}

      {moveTask.isError && (
        <Notice tone="error">Could not move the task. {getErrorMessage(moveTask.error)}</Notice>
      )}

      {tasks.data.totalElements > BOARD_PAGE_SIZE && (
        <Notice tone="warning">
          This project has {tasks.data.totalElements} tasks; the board shows the first {BOARD_PAGE_SIZE}.
        </Notice>
      )}

      {tasks.data.totalElements === 0 ? (
        <EmptyState title="No tasks yet" description="Tasks added to this project will appear on the board." />
      ) : (
        <div className={styles.board}>
          {STATUSES.map((status) => (
            <BoardColumn
              key={status}
              status={status}
              tasks={tasksByStatus[status]}
              busyTaskId={moveTask.isPending ? moveTask.variables?.taskId : undefined}
              onStatusChange={handleStatusChange}
            />
          ))}
        </div>
      )}
    </>
  )
}
