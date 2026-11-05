import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import * as tasksApi from '../api/tasks'
import type { TaskQuery, TaskStatus } from '../types'
import { projectKeys } from './useProjects'

// Everything for one project lives under ['projects', id, ...], so a single
// invalidation after any change refreshes both the task list and the summary bar.
const projectScope = (projectId: number) => [...projectKeys.all, projectId] as const

export const taskKeys = {
  project: projectScope,
  list: (projectId: number, query: TaskQuery) => [...projectScope(projectId), 'tasks', query] as const,
  summary: (projectId: number) => [...projectScope(projectId), 'summary'] as const,
}

export function useProjectTasks(projectId: number, query: TaskQuery = {}) {
  return useQuery({
    queryKey: taskKeys.list(projectId, query),
    queryFn: () => tasksApi.listTasks(projectId, query),
    enabled: Number.isFinite(projectId),
  })
}

export function useProjectSummary(projectId: number) {
  return useQuery({
    queryKey: taskKeys.summary(projectId),
    queryFn: () => tasksApi.getProjectSummary(projectId),
    enabled: Number.isFinite(projectId),
  })
}

export function useUpdateTaskStatus(projectId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ taskId, status }: { taskId: number; status: TaskStatus }) =>
      tasksApi.updateTaskStatus(taskId, status),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: taskKeys.project(projectId) }),
  })
}
