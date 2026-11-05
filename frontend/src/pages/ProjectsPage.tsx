import { useAuth } from '../hooks/useAuth'

export function ProjectsPage() {
  const { user } = useAuth()
  return (
    <>
      <h1>Your projects</h1>
      <p>Signed in as {user?.email}.</p>
    </>
  )
}
