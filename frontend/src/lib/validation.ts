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

export function validateProject({ name, description }: { name: string; description: string }): FieldErrors {
  const errors: FieldErrors = {}
  if (!name.trim()) errors.name = 'Name is required'
  else if (name.trim().length > 100) errors.name = 'Name must be at most 100 characters'
  if (description.length > 1000) errors.description = 'Description must be at most 1000 characters'
  return errors
}

interface TaskFormValues {
  title: string
  description: string
  dueDate: string
}

/** Today as YYYY-MM-DD in the viewer's timezone, the format a date input uses. */
export function todayAsInputValue(now: Date = new Date()): string {
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

/** A past due date is rejected only when creating, matching the backend rule. */
export function validateTask(
  { title, description, dueDate }: TaskFormValues,
  mode: 'create' | 'edit',
  today: string = todayAsInputValue(),
): FieldErrors {
  const errors: FieldErrors = {}
  if (!title.trim()) errors.title = 'Title is required'
  else if (title.trim().length > 120) errors.title = 'Title must be at most 120 characters'
  if (description.length > 2000) errors.description = 'Description must be at most 2000 characters'
  if (mode === 'create' && dueDate && dueDate < today) errors.dueDate = 'Due date cannot be in the past'
  return errors
}
