import axios from 'axios'
import type { ApiErrorBody } from '../types'

export function getApiError(error: unknown): ApiErrorBody | null {
  if (axios.isAxiosError<ApiErrorBody>(error) && error.response?.data?.message) {
    return error.response.data
  }
  return null
}

export function getStatus(error: unknown): number | null {
  return axios.isAxiosError(error) ? (error.response?.status ?? null) : null
}

/** A readable message for any failed request. */
export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error) && !error.response) {
    return 'Cannot reach the server. Check your connection and try again.'
  }
  return getApiError(error)?.message ?? 'Something went wrong. Please try again.'
}

/** Field-level messages from a 400 response, keyed by field name. */
export function getFieldErrors(error: unknown): Record<string, string> {
  const details = getApiError(error)?.fieldErrors ?? []
  return Object.fromEntries(details.map((d) => [d.field, d.message]))
}
