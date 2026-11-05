import type { CreateProjectData, Project } from '../types'
import { apiClient } from './axiosClient'

export async function listProjects(): Promise<Project[]> {
  const { data } = await apiClient.get<Project[]>('/projects')
  return data
}

export async function createProject(project: CreateProjectData): Promise<Project> {
  const { data } = await apiClient.post<Project>('/projects', project)
  return data
}
