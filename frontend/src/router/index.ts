import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guest: true }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { guest: true }
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/views/DashboardView.vue'),
      meta: { requiresAuth: true }
    },
    { path: '/', redirect: '/dashboard' }
  ]
})

router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()

  // 需要登录 → 未登录跳 /login
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return next('/login')
  }

  // 游客页面 → 已登录跳 /dashboard
  if (to.meta.guest && authStore.isLoggedIn) {
    return next('/dashboard')
  }

  next()
})

export default router
