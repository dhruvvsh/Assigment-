import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AxiosError } from 'axios'
import type { AxiosResponse } from 'axios'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import * as authApi from '../api/auth'
import { AuthProvider } from '../context/AuthContext'
import { LoginPage } from './LoginPage'

jest.mock('../api/auth')
const mockedLogin = jest.mocked(authApi.login)

function renderLoginPage() {
  const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } })
  render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/login']}>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/projects" element={<p>Projects page</p>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

function apiError(status: number, message: string) {
  const response = { status, data: { message, fieldErrors: [] } } as AxiosResponse
  return new AxiosError(message, String(status), undefined, undefined, response)
}

beforeEach(() => {
  localStorage.clear()
  mockedLogin.mockReset()
})

describe('LoginPage', () => {
  it('shows validation errors and does not call the API when the form is empty', async () => {
    renderLoginPage()

    await userEvent.click(screen.getByRole('button', { name: 'Log in' }))

    expect(screen.getByText('Email is required')).toBeInTheDocument()
    expect(screen.getByText('Password is required')).toBeInTheDocument()
    expect(mockedLogin).not.toHaveBeenCalled()
  })

  it('rejects a malformed email', async () => {
    renderLoginPage()

    await userEvent.type(screen.getByLabelText('Email'), 'not-an-email')
    await userEvent.type(screen.getByLabelText('Password'), 'secret')
    await userEvent.click(screen.getByRole('button', { name: 'Log in' }))

    expect(screen.getByText('Enter a valid email address')).toBeInTheDocument()
    expect(mockedLogin).not.toHaveBeenCalled()
  })

  it('logs in, stores the session and redirects to the projects page', async () => {
    mockedLogin.mockResolvedValue({
      token: 'header.payload.signature',
      user: { id: 1, name: 'Ann', email: 'ann@test.com', createdAt: '2026-01-01T00:00:00Z' },
    })
    renderLoginPage()

    await userEvent.type(screen.getByLabelText('Email'), '  ann@test.com ')
    await userEvent.type(screen.getByLabelText('Password'), 'Password123!')
    await userEvent.click(screen.getByRole('button', { name: 'Log in' }))

    expect(await screen.findByText('Projects page')).toBeInTheDocument()
    expect(mockedLogin).toHaveBeenCalledWith({ email: 'ann@test.com', password: 'Password123!' })
    expect(localStorage.getItem('taskflow.session')).toContain('header.payload.signature')
  })

  it('shows the server message when the credentials are wrong', async () => {
    mockedLogin.mockRejectedValue(apiError(401, 'Invalid email or password'))
    renderLoginPage()

    await userEvent.type(screen.getByLabelText('Email'), 'ann@test.com')
    await userEvent.type(screen.getByLabelText('Password'), 'wrong-password')
    await userEvent.click(screen.getByRole('button', { name: 'Log in' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Invalid email or password')
    expect(screen.queryByText('Projects page')).not.toBeInTheDocument()
  })
})
