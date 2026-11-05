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
