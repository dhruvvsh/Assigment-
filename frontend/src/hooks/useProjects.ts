import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import * as projectsApi from '../api/projects'
import type { CreateProjectData } from '../types'

export const projectKeys = {
  all: ['projects'] as const,
}

export function useProjects() {
  return useQuery({ queryKey: projectKeys.all, queryFn: projectsApi.listProjects })
}

export function useCreateProject() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (project: CreateProjectData) => projectsApi.createProject(project),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: projectKeys.all }),
  })
}
