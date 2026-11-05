import type { TaskPriority, TaskQuery } from '../types'

export const SORT_OPTIONS = [
  { value: 'dueDate:asc', label: 'Due date: soonest first' },
  { value: 'dueDate:desc', label: 'Due date: latest first' },
  { value: 'priority:desc', label: 'Priority: high first' },
  { value: 'priority:asc', label: 'Priority: low first' },
] as const

export type SortValue = (typeof SORT_OPTIONS)[number]['value']

/** What the filter bar edits. Empty strings mean "no filter" (they come straight from <select>). */
export interface FilterState {
  priority: TaskPriority | ''
  assigneeId: string
  sort: SortValue
}

export const DEFAULT_FILTERS: FilterState = { priority: '', assigneeId: '', sort: 'dueDate:asc' }

export function hasActiveFilters(filters: FilterState): boolean {
  return filters.priority !== '' || filters.assigneeId !== ''
}

/** Translates the form state into the query parameters the API understands. */
export function toTaskQuery(filters: FilterState): TaskQuery {
  const [sortBy, direction] = filters.sort.split(':') as ['dueDate' | 'priority', 'asc' | 'desc']
  return {
    priority: filters.priority || undefined,
    assigneeId: filters.assigneeId ? Number(filters.assigneeId) : undefined,
    sortBy,
    direction,
  }
}
