import { QueryClient } from '@tanstack/react-query'
import { getStatus } from '../api/errors'

/** Retrying a 4xx would just repeat the same mistake; only transient failures are retried. */
function shouldRetry(failureCount: number, error: unknown): boolean {
  const status = getStatus(error)
  const isClientError = status !== null && status >= 400 && status < 500
  return !isClientError && failureCount < 2
}

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: shouldRetry, refetchOnWindowFocus: false },
    mutations: { retry: false },
  },
})
