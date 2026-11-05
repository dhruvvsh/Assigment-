import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { getErrorMessage, getFieldErrors } from '../api/errors'
import { Button } from '../components/Button'
import { FormField } from '../components/FormField'
import { Notice } from '../components/Notice'
import { useAuth } from '../hooks/useAuth'
import { validateLogin } from '../lib/validation'
import type { FieldErrors } from '../lib/validation'
import styles from './AuthPage.module.css'

export function LoginPage() {
  const { login, sessionExpired } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const redirectTo = (location.state as { from?: string } | null)?.from ?? '/projects'

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errors, setErrors] = useState<FieldErrors>({})

  const mutation = useMutation({
    mutationFn: login,
    onSuccess: () => navigate(redirectTo, { replace: true }),
    onError: (error) => setErrors(getFieldErrors(error)),
  })

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    const found = validateLogin({ email, password })
    setErrors(found)
    if (Object.keys(found).length === 0) mutation.mutate({ email: email.trim(), password })
  }

  // Field errors are shown on the fields; anything else (wrong password, server down) goes in a banner.
  const bannerError = mutation.isError && Object.keys(errors).length === 0 ? getErrorMessage(mutation.error) : null

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h1>Welcome back</h1>
        <p className={styles.subtitle}>Log in to manage your projects.</p>

        {sessionExpired && <Notice tone="warning">Your session expired. Please log in again.</Notice>}
        {bannerError && <Notice tone="error">{bannerError}</Notice>}

        <form onSubmit={handleSubmit} noValidate>
          <FormField
            id="email"
            label="Email"
            type="email"
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            error={errors.email}
          />
          <FormField
            id="password"
            label="Password"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            error={errors.password}
          />
          <Button type="submit" className={styles.submit} loading={mutation.isPending}>
            Log in
          </Button>
        </form>

        <p className={styles.switch}>
          New here? <Link to="/register">Create an account</Link>
        </p>
        <p className={styles.hint}>Demo account: demo@taskflow.com / Password123!</p>
      </div>
    </div>
  )
}
