import type { ReactNode } from 'react'
import styles from './Notice.module.css'

interface NoticeProps {
  tone: 'error' | 'warning'
  children: ReactNode
}

/** A short banner for errors (announced to screen readers) and warnings. */
export function Notice({ tone, children }: NoticeProps) {
  return (
    <div className={`${styles.notice} ${styles[tone]}`} role={tone === 'error' ? 'alert' : 'status'}>
      {children}
    </div>
  )
}
