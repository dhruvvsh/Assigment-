import type { User } from '../types'
import { apiClient } from './axiosClient'

/** Everyone who can be chosen as a task assignee. */
export async function listUsers(): Promise<User[]> {
  const { data } = await apiClient.get<User[]>('/users')
  return data
}
