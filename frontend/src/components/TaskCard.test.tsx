import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import type { Task } from '../types'
import { TaskCard } from './TaskCard'

const task: Task = {
  id: 7,
  title: 'Fix login bug',
  description: 'Redirect loops after login',
  status: 'TODO',
  priority: 'HIGH',
  dueDate: '2026-03-10',
  overdue: false,
  assignee: { id: 1, name: 'Ann Lee', email: 'ann@test.com', createdAt: '2026-01-01T00:00:00Z' },
  projectId: 1,
  createdAt: '2026-01-01T00:00:00Z',
  updatedAt: '2026-01-01T00:00:00Z',
}

function renderCard(overrides: Partial<Task> = {}, props: { busy?: boolean } = {}) {
  const onStatusChange = jest.fn()
  const onEdit = jest.fn()
  const merged = { ...task, ...overrides }
  render(<TaskCard task={merged} onStatusChange={onStatusChange} onEdit={onEdit} {...props} />)
  return { merged, onStatusChange, onEdit }
}

describe('TaskCard', () => {
  it('shows the title, priority, description and assignee', () => {
    renderCard()

    expect(screen.getByRole('heading', { name: 'Fix login bug' })).toBeInTheDocument()
    expect(screen.getByText('High')).toBeInTheDocument()
    expect(screen.getByText('Redirect loops after login')).toBeInTheDocument()
    expect(screen.getByText('Ann Lee')).toBeInTheDocument()
  })

  it('shows "Unassigned" when nobody is assigned', () => {
    renderCard({ assignee: null })

    expect(screen.getByText('Unassigned')).toBeInTheDocument()
  })

  it('highlights an overdue task with an Overdue badge', () => {
    renderCard({ overdue: true })

    expect(screen.getByText('Overdue')).toBeInTheDocument()
  })

  it('does not show the Overdue badge for a task that is on time', () => {
    renderCard({ overdue: false })

    expect(screen.queryByText('Overdue')).not.toBeInTheDocument()
  })

  it('reports the new status when the dropdown changes', async () => {
    const { merged, onStatusChange } = renderCard()

    await userEvent.selectOptions(screen.getByLabelText('Status for Fix login bug'), 'DONE')

    expect(onStatusChange).toHaveBeenCalledWith(merged, 'DONE')
  })

  it('disables the status dropdown while the change is being saved', () => {
    renderCard({}, { busy: true })

    expect(screen.getByLabelText('Status for Fix login bug')).toBeDisabled()
  })

  it('opens the editor when Edit is clicked', async () => {
    const { merged, onEdit } = renderCard()

    await userEvent.click(screen.getByRole('button', { name: 'Edit Fix login bug' }))

    expect(onEdit).toHaveBeenCalledWith(merged)
  })
})
