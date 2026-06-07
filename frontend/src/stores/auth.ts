import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import type { UserInfo, LoginRequest } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserInfo | null>(null)
  const isLoggedIn = computed(() => !!user.value)

  async function login(data: LoginRequest) {
    const res = await authApi.login(data)
    if (res.code === 200) {
      await fetchUser()
    }
    return res
  }

  async function register(data: { username: string; email: string; password: string; nickname: string; agreed: boolean }) {
    return authApi.register(data)
  }

  async function fetchUser() {
    try {
      const res = await authApi.getCurrentUser()
      if (res.code === 200) {
        user.value = res.data
      }
    } catch {
      user.value = null
    }
  }

  async function logout() {
    try {
      await authApi.logout()
    } finally {
      user.value = null
    }
  }

  async function checkAuth(): Promise<boolean> {
    try {
      const res = await authApi.check()
      if (res.code === 200) {
        await fetchUser()
        return true
      }
    } catch {
      // ignore
    }
    return false
  }

  return { user, isLoggedIn, login, register, fetchUser, logout, checkAuth }
})
