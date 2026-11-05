import { STATUS_LABELS } from '../lib/taskMeta'
import type { Task, TaskStatus } from '../types'
import { TaskCard } from './TaskCard'
import styles from './BoardColumn.module.css'

interface BoardColumnProps {
  status: TaskStatus
  tasks: Task[]
  /** The id of the task whose status is currently being saved, if any. */
  busyTaskId?: number
  onStatusChange: (task: Task, status: TaskStatus) => void
  onEdit: (task: Task) => void
}

export function BoardColumn({ status, tasks, busyTaskId, onStatusChange, onEdit }: BoardColumnProps) {
  return (
    <section className={styles.column} aria-label={STATUS_LABELS[status]}>
      <h2 className={styles.heading}>
        {STATUS_LABELS[status]} <span className={styles.count}>{tasks.length}</span>
      </h2>
      {tasks.length === 0 ? (
        <p className={styles.empty}>No tasks here.</p>
      ) : (
        <div className={styles.list}>
          {tasks.map((task) => (
            <TaskCard
              key={task.id}
              task={task}
              busy={task.id === busyTaskId}
              onStatusChange={onStatusChange}
              onEdit={onEdit}
            />
          ))}
        </div>
      )}
    </section>
  )
}
