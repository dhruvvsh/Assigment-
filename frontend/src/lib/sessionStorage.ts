import type { Session } from '../types'

const KEY = 'taskflow.session'

/** Reads the `exp` claim of a JWT. Anything unreadable is treated as expired. */
export function isTokenExpired(token: string): boolean {
  try {
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const { exp } = JSON.parse(atob(payload)) as { exp?: number }
    return typeof exp !== 'number' || exp * 1000 <= Date.now()
  } catch {
    return true
  }
}

export function saveSession(session: Session): void {
  try {
    localStorage.setItem(KEY, JSON.stringify(session))
  } catch {
    // Storage can be unavailable (private mode); the user simply has to log in again later.
  }
}

export function clearSession(): void {
  try {
    localStorage.removeItem(KEY)
  } catch {
    // ignore
  }
}

/** Returns the stored session, or null if there is none or its token has expired. */
export function loadSession(): Session | null {
  try {
    const raw = localStorage.getItem(KEY)
    if (!raw) return null
    const session = JSON.parse(raw) as Session
    if (!session?.token || !session?.user || isTokenExpired(session.token)) {
      clearSession()
      return null
    }
    return session
  } catch {
    return null
  }
}

export function getToken(): string | null {
  return loadSession()?.token ?? null
}
