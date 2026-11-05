import { useState } from 'react'
import type { FormEvent } from 'react'
import { getErrorMessage, getFieldErrors } from '../api/errors'
import { useCreateProject } from '../hooks/useProjects'
import { validateProject } from '../lib/validation'
import type { FieldErrors } from '../lib/validation'
import type { Project } from '../types'
import { Button } from './Button'
import { FormField } from './FormField'
import { Notice } from './Notice'
import { TextAreaField } from './TextAreaField'
import styles from './ProjectForm.module.css'

interface ProjectFormProps {
  onCreated: (project: Project) => void
  onCancel: () => void
}

export function ProjectForm({ onCreated, onCancel }: ProjectFormProps) {
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [errors, setErrors] = useState<FieldErrors>({})

  const createProject = useCreateProject()

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    const found = validateProject({ name, description })
    setErrors(found)
    if (Object.keys(found).length > 0) return

    createProject.mutate(
      { name: name.trim(), description: description.trim() || undefined },
      {
        onSuccess: onCreated,
        onError: (error) => setErrors(getFieldErrors(error)),
      },
    )
  }

  const bannerError =
    createProject.isError && Object.keys(errors).length === 0 ? getErrorMessage(createProject.error) : null

  return (
    <form onSubmit={handleSubmit} noValidate>
      {bannerError && <Notice tone="error">{bannerError}</Notice>}
      <FormField
        id="project-name"
        label="Name"
        value={name}
        onChange={(e) => setName(e.target.value)}
        error={errors.name}
        autoFocus
      />
      <TextAreaField
        id="project-description"
        label="Description (optional)"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        error={errors.description}
      />
      <div className={styles.actions}>
        <Button variant="secondary" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" loading={createProject.isPending}>
          Create project
        </Button>
      </div>
    </form>
  )
}
