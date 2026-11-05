import { formatDate } from '../lib/format'
import { PRIORITY_LABELS, STATUS_LABELS, STATUSES } from '../lib/taskMeta'
import type { Task, TaskStatus } from '../types'
import { Button } from './Button'
import styles from './TaskCard.module.css'

interface TaskCardProps {
  task: Task
  /** True while this card's status change is being saved. */
  busy?: boolean
  onStatusChange: (task: Task, status: TaskStatus) => void
  onEdit: (task: Task) => void
}

export function TaskCard({ task, busy = false, onStatusChange, onEdit }: TaskCardProps) {
  const className = task.overdue ? `${styles.card} ${styles.overdue}` : styles.card

  return (
    <article className={className} aria-label={task.title}>
      <div className={styles.top}>
        <h3 className={styles.title}>{task.title}</h3>
        <span className={`${styles.badge} ${styles[task.priority]}`}>{PRIORITY_LABELS[task.priority]}</span>
      </div>

      {task.description && <p className={styles.description}>{task.description}</p>}

      <div className={styles.meta}>
        {task.dueDate && (
          <span className={task.overdue ? styles.dueOverdue : undefined}>
            Due {formatDate(task.dueDate)}
            {task.overdue && <strong className={styles.overdueBadge}>Overdue</strong>}
          </span>
        )}
        <span>{task.assignee ? task.assignee.name : 'Unassigned'}</span>
      </div>

      <div className={styles.actions}>
        <label className={styles.statusLabel}>
          <span className={styles.srOnly}>Status for {task.title}</span>
          <select
            className={styles.select}
            value={task.status}
            disabled={busy}
            onChange={(e) => onStatusChange(task, e.target.value as TaskStatus)}
          >
            {STATUSES.map((status) => (
              <option key={status} value={status}>
                {STATUS_LABELS[status]}
              </option>
            ))}
          </select>
        </label>
        <Button variant="secondary" className={styles.edit} onClick={() => onEdit(task)} aria-label={`Edit ${task.title}`}>
          Edit
        </Button>
      </div>
    </article>
  )
}
