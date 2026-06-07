import request from './request'
import type { LoginRequest, RegisterRequest, ApiResponse, AvailableResult } from '@/types'

export const authApi = {
  login(data: LoginRequest): Promise<ApiResponse> {
    return request.post('/auth/login', data)
  },
  register(data: RegisterRequest): Promise<ApiResponse> {
    return request.post('/auth/register', data)
  },
  logout(): Promise<ApiResponse> {
    return request.post('/auth/logout')
  },
  getCurrentUser(): Promise<ApiResponse> {
    return request.get('/auth/current-user')
  },
  check(): Promise<ApiResponse> {
    return request.get('/auth/check')
  },
  checkAvailability(type: string, value: string): Promise<AvailableResult> {
    return request.get('/auth/check-availability', { params: { type, value } })
  }
}
