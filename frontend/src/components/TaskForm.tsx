import { useState } from 'react'
import type { FormEvent } from 'react'
import { getErrorMessage, getFieldErrors } from '../api/errors'
import { useCreateTask, useDeleteTask, useUpdateTask } from '../hooks/useTasks'
import { PRIORITY_LABELS } from '../lib/taskMeta'
import { validateTask } from '../lib/validation'
import type { FieldErrors } from '../lib/validation'
import type { Task, TaskInput, TaskPriority, User } from '../types'
import { Button } from './Button'
import { FormField } from './FormField'
import { Notice } from './Notice'
import { SelectField } from './SelectField'
import { TextAreaField } from './TextAreaField'
import styles from './TaskForm.module.css'

interface TaskFormProps {
  projectId: number
  users: User[]
  /** Present when editing an existing task; absent when creating. */
  task?: Task
  /** Called after a successful save or delete, and when the user cancels. */
  onDone: () => void
}

export function TaskForm({ projectId, users, task, onDone }: TaskFormProps) {
  const mode = task ? 'edit' : 'create'

  const [title, setTitle] = useState(task?.title ?? '')
  const [description, setDescription] = useState(task?.description ?? '')
  const [priority, setPriority] = useState<TaskPriority>(task?.priority ?? 'MEDIUM')
  const [dueDate, setDueDate] = useState(task?.dueDate ?? '')
  const [assigneeId, setAssigneeId] = useState(task?.assignee ? String(task.assignee.id) : '')
  const [errors, setErrors] = useState<FieldErrors>({})
  const [confirmingDelete, setConfirmingDelete] = useState(false)

  const createTask = useCreateTask(projectId)
  const updateTask = useUpdateTask(projectId)
  const deleteTask = useDeleteTask(projectId)

  const saving = createTask.isPending || updateTask.isPending
  const failure = createTask.error ?? updateTask.error ?? deleteTask.error

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    const found = validateTask({ title, description, dueDate }, mode)
    setErrors(found)
    if (Object.keys(found).length > 0) return

    const input: TaskInput = {
      title: title.trim(),
      description: description.trim(),
      priority,
      dueDate: dueDate || null,
      assigneeId: assigneeId ? Number(assigneeId) : null,
    }
    const callbacks = { onSuccess: onDone, onError: (error: unknown) => setErrors(getFieldErrors(error)) }

    if (task) updateTask.mutate({ task, input }, callbacks)
    else createTask.mutate(input, callbacks)
  }

  // Field errors show on their fields; anything else (server down, task gone) goes in a banner.
  const bannerError = failure && Object.keys(errors).length === 0 ? getErrorMessage(failure) : null

  return (
    <form onSubmit={handleSubmit} noValidate>
      {bannerError && <Notice tone="error">{bannerError}</Notice>}

      <FormField
        id="task-title"
        label="Title"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        error={errors.title}
        autoFocus
      />
      <TextAreaField
        id="task-description"
        label="Description (optional)"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        error={errors.description}
      />

      <div className={styles.row}>
        <SelectField
          id="task-priority"
          label="Priority"
          value={priority}
          onChange={(e) => setPriority(e.target.value as TaskPriority)}
        >
          {(Object.keys(PRIORITY_LABELS) as TaskPriority[]).map((value) => (
            <option key={value} value={value}>
              {PRIORITY_LABELS[value]}
            </option>
          ))}
        </SelectField>
        <FormField
          id="task-due-date"
          label="Due date"
          type="date"
          value={dueDate}
          onChange={(e) => setDueDate(e.target.value)}
          error={errors.dueDate}
        />
      </div>

      <SelectField
        id="task-assignee"
        label="Assignee"
        value={assigneeId}
        onChange={(e) => setAssigneeId(e.target.value)}
        error={errors.assigneeId}
      >
        <option value="">Unassigned</option>
        {users.map((user) => (
          <option key={user.id} value={user.id}>
            {user.name}
          </option>
        ))}
      </SelectField>

      <div className={styles.actions}>
        {task && !confirmingDelete && (
          <Button variant="secondary" className={styles.delete} onClick={() => setConfirmingDelete(true)}>
            Delete
          </Button>
        )}
        {task && confirmingDelete && (
          <>
            <span className={styles.confirmText}>Delete this task?</span>
            <Button variant="danger" loading={deleteTask.isPending} onClick={() => deleteTask.mutate(task.id, { onSuccess: onDone })}>
              Yes, delete
            </Button>
            <Button variant="secondary" onClick={() => setConfirmingDelete(false)}>
              Keep
            </Button>
          </>
        )}
        <span className={styles.spacer} />
        <Button variant="secondary" onClick={onDone}>
          Cancel
        </Button>
        <Button type="submit" loading={saving}>
          {mode === 'create' ? 'Create task' : 'Save changes'}
        </Button>
      </div>
    </form>
  )
}
