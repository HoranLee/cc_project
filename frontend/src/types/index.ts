// 类型定义
export interface LoginRequest {
  account: string
  password: string
  rememberMe: boolean
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  nickname: string
  agreed: boolean
}

export interface LoginResponse {
  id: number
  username: string
  email: string
  nickname: string
  avatarUrl: string | null
  lastLoginTime: string
  message: string
}

export interface UserInfo {
  id: number
  username: string
  email: string
  nickname: string
  avatarUrl: string | null
  status: number
  lastLoginTime: string
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface AvailableResult {
  available: boolean
  message?: string
}
