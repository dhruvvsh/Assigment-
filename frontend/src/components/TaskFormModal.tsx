import { getErrorMessage } from '../api/errors'
import { useUsers } from '../hooks/useUsers'
import type { Task } from '../types'
import { ActivityLog } from './ActivityLog'
import { ErrorState } from './ErrorState'
import { Modal } from './Modal'
import { Spinner } from './Spinner'
import { TaskForm } from './TaskForm'

interface TaskFormModalProps {
  projectId: number
  /** The task being edited; leave out to create a new one. */
  task?: Task
  onClose: () => void
}

/** Create/edit dialog. The form waits for the assignee list, so it always has real options. */
export function TaskFormModal({ projectId, task, onClose }: TaskFormModalProps) {
  const users = useUsers()

  return (
    <Modal title={task ? 'Edit task' : 'New task'} onClose={onClose}>
      {users.isPending && <Spinner label="Loading…" />}
      {users.isError && <ErrorState message={getErrorMessage(users.error)} onRetry={() => users.refetch()} />}
      {users.data && (
        <>
          <TaskForm projectId={projectId} users={users.data} task={task} onDone={onClose} />
          {task && <ActivityLog taskId={task.id} />}
        </>
      )}
    </Modal>
  )
}
