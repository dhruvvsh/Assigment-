import { useQueryClient } from '@tanstack/react-query'
import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import * as authApi from '../api/auth'
import { setUnauthorizedHandler } from '../api/axiosClient'
import { clearSession, loadSession, saveSession } from '../lib/sessionStorage'
import type { AuthResponse, LoginCredentials, RegisterData, Session } from '../types'
import { AuthContext } from './authContextDefinition'
import type { AuthContextValue } from './authContextDefinition'

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()
  const [session, setSession] = useState<Session | null>(loadSession)
  const [sessionExpired, setSessionExpired] = useState(false)

  const startSession = useCallback((response: AuthResponse) => {
    const next = { token: response.token, user: response.user }
    saveSession(next)
    setSession(next)
    setSessionExpired(false)
  }, [])

  const logout = useCallback(() => {
    clearSession()
    setSession(null)
    // Cached data belongs to the previous user and must not leak into the next session.
    queryClient.clear()
  }, [queryClient])

  const login = useCallback(
    async (credentials: LoginCredentials) => startSession(await authApi.login(credentials)),
    [startSession],
  )

  const register = useCallback(
    async (data: RegisterData) => startSession(await authApi.register(data)),
    [startSession],
  )

  // The HTTP layer reports 401s here; we end the session and flag why.
  useEffect(() => {
    setUnauthorizedHandler(() => {
      logout()
      setSessionExpired(true)
    })
    return () => setUnauthorizedHandler(null)
  }, [logout])

  const value = useMemo<AuthContextValue>(
    () => ({
      user: session?.user ?? null,
      isAuthenticated: session !== null,
      sessionExpired,
      login,
      register,
      logout,
    }),
    [session, sessionExpired, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
