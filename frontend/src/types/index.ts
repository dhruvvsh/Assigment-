export interface User {
  id: number
  name: string
  email: string
  createdAt: string
}

export interface AuthResponse {
  token: string
  user: User
}

export interface Session {
  token: string
  user: User
}

export interface LoginCredentials {
  email: string
  password: string
}

export interface RegisterData extends LoginCredentials {
  name: string
}

/** Mirrors the backend's single error shape. */
export interface FieldErrorDetail {
  field: string
  message: string
}

export interface ApiErrorBody {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
  fieldErrors: FieldErrorDetail[]
}

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE'
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH'

export interface Project {
  id: number
  name: string
  description: string | null
  owner: User
  createdAt: string
}

export interface CreateProjectData {
  name: string
  description?: string
}

export interface Task {
  id: number
  title: string
  description: string | null
  status: TaskStatus
  priority: TaskPriority
  dueDate: string | null
  overdue: boolean
  assignee: User | null
  projectId: number
  createdAt: string
  updatedAt: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface ProjectSummary {
  total: number
  byStatus: Record<TaskStatus, number>
  overdue: number
}

/** Optional filters and sorting for the task list; omitted values are not sent. */
export interface TaskQuery {
  priority?: TaskPriority
  assigneeId?: number
  sortBy?: 'dueDate' | 'priority'
  direction?: 'asc' | 'desc'
}

export interface TaskActivity {
  id: number
  fromStatus: TaskStatus | null
  toStatus: TaskStatus
  changedBy: User
  changedAt: string
}

/** Fields shared by create and edit. Empty optional values are sent as null. */
export interface TaskInput {
  title: string
  description: string
  priority: TaskPriority
  dueDate: string | null
  assigneeId: number | null
}
