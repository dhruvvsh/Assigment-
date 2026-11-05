import { hasActiveFilters, SORT_OPTIONS } from '../lib/taskFilters'
import type { FilterState, SortValue } from '../lib/taskFilters'
import { PRIORITY_LABELS } from '../lib/taskMeta'
import type { TaskPriority, User } from '../types'
import { Button } from './Button'
import styles from './FilterBar.module.css'

interface FilterBarProps {
  filters: FilterState
  users: User[]
  onChange: (filters: FilterState) => void
  onClear: () => void
}

export function FilterBar({ filters, users, onChange, onClear }: FilterBarProps) {
  return (
    <div className={styles.bar} role="group" aria-label="Filter and sort tasks">
      <label className={styles.control}>
        <span>Priority</span>
        <select
          value={filters.priority}
          onChange={(e) => onChange({ ...filters, priority: e.target.value as TaskPriority | '' })}
        >
          <option value="">All priorities</option>
          {(Object.keys(PRIORITY_LABELS) as TaskPriority[]).map((value) => (
            <option key={value} value={value}>
              {PRIORITY_LABELS[value]}
            </option>
          ))}
        </select>
      </label>

      <label className={styles.control}>
        <span>Assignee</span>
        <select value={filters.assigneeId} onChange={(e) => onChange({ ...filters, assigneeId: e.target.value })}>
          <option value="">Everyone</option>
          {users.map((user) => (
            <option key={user.id} value={user.id}>
              {user.name}
            </option>
          ))}
        </select>
      </label>

      <label className={styles.control}>
        <span>Sort by</span>
        <select value={filters.sort} onChange={(e) => onChange({ ...filters, sort: e.target.value as SortValue })}>
          {SORT_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </label>

      {hasActiveFilters(filters) && (
        <Button variant="secondary" onClick={onClear}>
          Clear filters
        </Button>
      )}
    </div>
  )
}
