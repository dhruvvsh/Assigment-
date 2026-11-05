import type { SelectHTMLAttributes } from 'react'
import styles from './FormField.module.css'

interface SelectFieldProps extends SelectHTMLAttributes<HTMLSelectElement> {
  id: string
  label: string
  error?: string
}

/** A labelled dropdown; pass the <option> elements as children. */
export function SelectField({ id, label, error, children, ...selectProps }: SelectFieldProps) {
  const errorId = `${id}-error`
  return (
    <div className={styles.field}>
      <label htmlFor={id} className={styles.label}>
        {label}
      </label>
      <select
        {...selectProps}
        id={id}
        className={error ? `${styles.input} ${styles.invalid}` : styles.input}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? errorId : undefined}
      >
        {children}
      </select>
      {error && (
        <p id={errorId} className={styles.error}>
          {error}
        </p>
      )}
    </div>
  )
}
