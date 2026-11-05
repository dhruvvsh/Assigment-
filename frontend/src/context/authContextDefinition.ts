import { createContext } from 'react'
import type { LoginCredentials, RegisterData, User } from '../types'

export interface AuthContextValue {
  user: User | null
  isAuthenticated: boolean
  /** True after the server rejected our token, until the user signs in again. */
  sessionExpired: boolean
  login: (credentials: LoginCredentials) => Promise<void>
  register: (data: RegisterData) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)
