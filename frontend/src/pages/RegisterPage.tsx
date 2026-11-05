import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getErrorMessage, getFieldErrors } from '../api/errors'
import { Button } from '../components/Button'
import { FormField } from '../components/FormField'
import { Notice } from '../components/Notice'
import { useAuth } from '../hooks/useAuth'
import { validateRegister } from '../lib/validation'
import type { FieldErrors } from '../lib/validation'
import styles from './AuthPage.module.css'

export function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()

  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errors, setErrors] = useState<FieldErrors>({})

  const mutation = useMutation({
    mutationFn: register,
    onSuccess: () => navigate('/projects', { replace: true }),
    onError: (error) => setErrors(getFieldErrors(error)),
  })

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    const found = validateRegister({ name, email, password })
    setErrors(found)
    if (Object.keys(found).length === 0) {
      mutation.mutate({ name: name.trim(), email: email.trim(), password })
    }
  }

  // A duplicate email comes back as a 409 with no field errors, so it appears in the banner.
  const bannerError = mutation.isError && Object.keys(errors).length === 0 ? getErrorMessage(mutation.error) : null

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h1>Create your account</h1>
        <p className={styles.subtitle}>Start organising your team's tasks.</p>

        {bannerError && <Notice tone="error">{bannerError}</Notice>}

        <form onSubmit={handleSubmit} noValidate>
          <FormField
            id="name"
            label="Name"
            autoComplete="name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            error={errors.name}
          />
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
            autoComplete="new-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            error={errors.password}
          />
          <Button type="submit" className={styles.submit} loading={mutation.isPending}>
            Create account
          </Button>
        </form>

        <p className={styles.switch}>
          Already have an account? <Link to="/login">Log in</Link>
        </p>
      </div>
    </div>
  )
}
