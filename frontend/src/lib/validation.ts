import type { LoginCredentials, RegisterData } from '../types'

export type FieldErrors = Record<string, string>

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function validateEmail(email: string): string | undefined {
  if (!email.trim()) return 'Email is required'
  if (!EMAIL_PATTERN.test(email.trim())) return 'Enter a valid email address'
}

export function validateLogin({ email, password }: LoginCredentials): FieldErrors {
  const errors: FieldErrors = {}
  const emailError = validateEmail(email)
  if (emailError) errors.email = emailError
  if (!password) errors.password = 'Password is required'
  return errors
}

// The limits below match the backend's validation rules.
export function validateRegister({ name, email, password }: RegisterData): FieldErrors {
  const errors: FieldErrors = {}
  if (!name.trim()) errors.name = 'Name is required'
  else if (name.trim().length > 100) errors.name = 'Name must be at most 100 characters'

  const emailError = validateEmail(email)
  if (emailError) errors.email = emailError

  if (!password) errors.password = 'Password is required'
  else if (password.length < 8) errors.password = 'Password must be at least 8 characters'
  else if (password.length > 72) errors.password = 'Password must be at most 72 characters'
  return errors
}
