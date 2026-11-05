import type { TaskPriority, TaskStatus } from '../types'

/** Column order on the board. */
export const STATUSES: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE']

export const STATUS_LABELS: Record<TaskStatus, string> = {
  TODO: 'To Do',
  IN_PROGRESS: 'In Progress',
  DONE: 'Done',
}

export const PRIORITY_LABELS: Record<TaskPriority, string> = {
  LOW: 'Low',
  MEDIUM: 'Medium',
  HIGH: 'High',
}
