import { useQuery } from '@tanstack/react-query'
import * as usersApi from '../api/users'

export function useUsers() {
  return useQuery({ queryKey: ['users'], queryFn: usersApi.listUsers })
}
