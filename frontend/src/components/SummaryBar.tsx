import { STATUS_LABELS, STATUSES } from '../lib/taskMeta'
import type { ProjectSummary } from '../types'
import styles from './SummaryBar.module.css'

/** Task counts per status, plus how many open tasks are past their due date. */
export function SummaryBar({ summary }: { summary: ProjectSummary }) {
  return (
    <dl className={styles.bar} aria-label="Project summary">
      <div className={styles.item}>
        <dt>Total</dt>
        <dd>{summary.total}</dd>
      </div>
      {STATUSES.map((status) => (
        <div key={status} className={styles.item}>
          <dt>{STATUS_LABELS[status]}</dt>
          <dd>{summary.byStatus[status]}</dd>
        </div>
      ))}
      <div className={summary.overdue > 0 ? `${styles.item} ${styles.overdue}` : styles.item}>
        <dt>Overdue</dt>
        <dd>{summary.overdue}</dd>
      </div>
    </dl>
  )
}
