import { Link, Outlet } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { Button } from './Button'
import styles from './AppLayout.module.css'

/** The signed-in shell: a header with the user's name and a logout button around each page. */
export function AppLayout() {
  const { user, logout } = useAuth()
  return (
    <>
      <header className={styles.header}>
        <Link to="/projects" className={styles.brand}>
          TaskFlow
        </Link>
        <div className={styles.user}>
          <span className={styles.name}>{user?.name}</span>
          <Button variant="secondary" onClick={logout}>
            Log out
          </Button>
        </div>
      </header>
      <main className={styles.main}>
        <Outlet />
      </main>
    </>
  )
}
