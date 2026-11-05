import { Link } from 'react-router-dom'
import { formatDate } from '../lib/format'
import type { Project } from '../types'
import styles from './ProjectCard.module.css'

export function ProjectCard({ project }: { project: Project }) {
  return (
    <Link to={`/projects/${project.id}`} className={styles.card}>
      <h2 className={styles.name}>{project.name}</h2>
      {project.description && <p className={styles.description}>{project.description}</p>}
      <p className={styles.meta}>Created {formatDate(project.createdAt)}</p>
    </Link>
  )
}
