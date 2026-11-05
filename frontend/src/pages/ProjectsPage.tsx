import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getErrorMessage } from '../api/errors'
import { Button } from '../components/Button'
import { EmptyState } from '../components/EmptyState'
import { ErrorState } from '../components/ErrorState'
import { Modal } from '../components/Modal'
import { ProjectCard } from '../components/ProjectCard'
import { ProjectForm } from '../components/ProjectForm'
import { Spinner } from '../components/Spinner'
import { useProjects } from '../hooks/useProjects'
import styles from './Page.module.css'

export function ProjectsPage() {
  const [creating, setCreating] = useState(false)
  const navigate = useNavigate()
  const projects = useProjects()

  const newProjectButton = <Button onClick={() => setCreating(true)}>New project</Button>

  return (
    <>
      <div className={styles.header}>
        <h1>Your projects</h1>
        {projects.data && projects.data.length > 0 && newProjectButton}
      </div>

      {projects.isPending && <Spinner label="Loading projects…" />}

      {projects.isError && <ErrorState message={getErrorMessage(projects.error)} onRetry={() => projects.refetch()} />}

      {projects.data?.length === 0 && (
        <EmptyState
          title="No projects yet"
          description="Create your first project to start tracking tasks."
          action={newProjectButton}
        />
      )}

      {projects.data && projects.data.length > 0 && (
        <div className={styles.projectGrid}>
          {projects.data.map((project) => (
            <ProjectCard key={project.id} project={project} />
          ))}
        </div>
      )}

      {creating && (
        <Modal title="New project" onClose={() => setCreating(false)}>
          <ProjectForm
            onCancel={() => setCreating(false)}
            onCreated={(project) => {
              setCreating(false)
              navigate(`/projects/${project.id}`)
            }}
          />
        </Modal>
      )}
    </>
  )
}
