import { getErrorMessage } from '../api/errors'
import { useTaskActivity } from '../hooks/useTasks'
import { formatDateTime } from '../lib/format'
import { STATUS_LABELS } from '../lib/taskMeta'
import { ErrorState } from './ErrorState'
import { Spinner } from './Spinner'
import styles from './ActivityLog.module.css'

/** Who changed a task's status and when, newest first. */
export function ActivityLog({ taskId }: { taskId: number }) {
  const activity = useTaskActivity(taskId)

  return (
    <section className={styles.section} aria-label="Activity">
      <h3>Activity</h3>

      {activity.isPending && <Spinner label="Loading activity…" />}
      {activity.isError && <ErrorState message={getErrorMessage(activity.error)} onRetry={() => activity.refetch()} />}
      {activity.data?.length === 0 && <p className={styles.empty}>No activity yet.</p>}

      {activity.data && activity.data.length > 0 && (
        <ul className={styles.list}>
          {activity.data.map((entry) => (
            <li key={entry.id}>
              <strong>{entry.changedBy.name}</strong>{' '}
              {entry.fromStatus
                ? `moved this from ${STATUS_LABELS[entry.fromStatus]} to ${STATUS_LABELS[entry.toStatus]}`
                : 'created this task'}
              <time className={styles.time} dateTime={entry.changedAt}>
                {formatDateTime(entry.changedAt)}
              </time>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
