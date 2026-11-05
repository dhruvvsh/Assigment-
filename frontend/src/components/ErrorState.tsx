import { Button } from './Button'
import { Notice } from './Notice'

interface ErrorStateProps {
  message: string
  onRetry?: () => void
}

/** Shown when a request fails: what went wrong, and a way to try again. */
export function ErrorState({ message, onRetry }: ErrorStateProps) {
  return (
    <Notice tone="error">
      <p style={{ margin: '0 0 0.5rem' }}>{message}</p>
      {onRetry && (
        <Button variant="secondary" onClick={onRetry}>
          Try again
        </Button>
      )}
    </Notice>
  )
}
