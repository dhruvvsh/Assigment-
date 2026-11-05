import type { PageResponse, ProjectSummary, Task, TaskActivity, TaskInput, TaskQuery, TaskStatus } from '../types'
import { apiClient } from './axiosClient'

/** The board shows every task at once, so it asks for the largest page the API allows. */
export const BOARD_PAGE_SIZE = 100

export async function listTasks(projectId: number, query: TaskQuery = {}): Promise<PageResponse<Task>> {
  const { data } = await apiClient.get<PageResponse<Task>>(`/projects/${projectId}/tasks`, {
    params: { size: BOARD_PAGE_SIZE, ...query },
  })
  return data
}

export async function getProjectSummary(projectId: number): Promise<ProjectSummary> {
  const { data } = await apiClient.get<ProjectSummary>(`/projects/${projectId}/summary`)
  return data
}

export async function updateTaskStatus(taskId: number, status: TaskStatus): Promise<Task> {
  const { data } = await apiClient.patch<Task>(`/tasks/${taskId}/status`, { status })
  return data
}

export async function createTask(projectId: number, input: TaskInput): Promise<Task> {
  const { data } = await apiClient.post<Task>(`/projects/${projectId}/tasks`, input)
  return data
}

/** PUT replaces every editable field, so the current status is sent along unchanged. */
export async function updateTask(task: Task, input: TaskInput): Promise<Task> {
  const { data } = await apiClient.put<Task>(`/tasks/${task.id}`, { ...input, status: task.status })
  return data
}

export async function deleteTask(taskId: number): Promise<void> {
  await apiClient.delete(`/tasks/${taskId}`)
}

export async function getTaskActivity(taskId: number): Promise<TaskActivity[]> {
  const { data } = await apiClient.get<TaskActivity[]>(`/tasks/${taskId}/activity`)
  return data
}
