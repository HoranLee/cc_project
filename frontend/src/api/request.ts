import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 10000,
  withCredentials: true
})

// 响应拦截：401 自动跳登录
request.interceptors.response.use(
  res => res.data,
  error => {
    if (error.response?.status === 401) {
      const path = window.location.pathname
      if (path !== '/login' && path !== '/register') {
        ElMessage.warning('会话已过期，请重新登录')
        router.push('/login')
      }
    }
    return Promise.reject(error)
  }
)

export default request
