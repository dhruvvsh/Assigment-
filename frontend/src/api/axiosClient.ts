import axios from 'axios'
import { getToken } from '../lib/sessionStorage'

let unauthorizedHandler: (() => void) | null = null

/** The auth provider registers itself here, so this module never imports React code. */
export function setUnauthorizedHandler(handler: (() => void) | null): void {
  unauthorizedHandler = handler
}

/** The one HTTP client for the whole app. Components never call axios directly. */
export const apiClient = axios.create({ baseURL: '/api' })

apiClient.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    // A 401 while holding a token means it expired or was rejected. A 401 without a token
    // (a failed login) is a normal error that the form shows itself.
    if (axios.isAxiosError(error) && error.response?.status === 401 && getToken()) {
      unauthorizedHandler?.()
    }
    return Promise.reject(error)
  },
)
