import type { AuthResponse, LoginCredentials, RegisterData } from '../types'
import { apiClient } from './axiosClient'

export async function login(credentials: LoginCredentials): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/auth/login', credentials)
  return data
}

export async function register(data: RegisterData): Promise<AuthResponse> {
  const { data: response } = await apiClient.post<AuthResponse>('/auth/register', data)
  return response
}
