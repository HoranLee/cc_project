<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { User, Lock, Message } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '', email: '', password: '', confirmPassword: '', nickname: '', agreed: false
})

// 密码强度
const strengthInfo = computed(() => {
  const val = form.password
  if (!val) return null
  let score = 0
  if (val.length >= 8) score++
  if (/[a-zA-Z]/.test(val)) score++
  if (/\d/.test(val)) score++
  if (/[^a-zA-Z0-9]/.test(val)) score++
  if (val.length >= 10) score++
  if (score <= 2) return { label: '弱', color: '#e74c3c', width: '33%' }
  if (score <= 3) return { label: '中', color: '#f39c12', width: '66%' }
  return { label: '强', color: '#27ae60', width: '100%' }
})

// 注册按钮状态
const canSubmit = computed(() => {
  return form.username && form.email && form.password &&
         form.confirmPassword && form.agreed &&
         !loading.value
})

const validateUsername = (_rule: any, value: string, cb: Function) => {
  if (!value) return cb(new Error('请输入用户名'))
  if (!/^[a-zA-Z][a-zA-Z0-9_]{3,19}$/.test(value)) {
    return cb(new Error('4~20位字母/数字/下划线，字母开头'))
  }
  cb()
}

const validatePassword = (_rule: any, value: string, cb: Function) => {
  if (!value) return cb(new Error('请输入密码'))
  if (!/^(?=.*[a-zA-Z])(?=.*\d).{8,}$/.test(value)) {
    return cb(new Error('至少8位，包含字母和数字'))
  }
  cb()
}

const validateConfirm = (_rule: any, value: string, cb: Function) => {
  if (!value) return cb(new Error('请确认密码'))
  if (value !== form.password) return cb(new Error('两次密码输入不一致'))
  cb()
}

const rules: FormRules = {
  username: [{ required: true, validator: validateUsername, trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式错误', trigger: 'blur' }
  ],
  password: [{ required: true, validator: validatePassword, trigger: 'change' }],
  confirmPassword: [{ required: true, validator: validateConfirm, trigger: 'change' }]
}

// 唯一性校验
let checkTimer: ReturnType<typeof setTimeout> | null = null
const usernameAvailable = ref<boolean | null>(null)
const emailAvailable = ref<boolean | null>(null)

async function checkAvailability(type: 'username' | 'email', value: string) {
  if (!value) { type === 'username' ? usernameAvailable.value = null : emailAvailable.value = null; return }
  try {
    const res = await authApi.checkAvailability(type, value)
    const flag = res.available
    if (type === 'username') usernameAvailable.value = flag
    else emailAvailable.value = flag
  } catch { /* ignore */ }
}

import { authApi } from '@/api/auth'
import { watch } from 'vue'

watch(() => form.username, (v) => {
  if (checkTimer) clearTimeout(checkTimer)
  checkTimer = setTimeout(() => checkAvailability('username', v), 500)
})
watch(() => form.email, (v) => {
  if (checkTimer) clearTimeout(checkTimer)
  checkTimer = setTimeout(() => checkAvailability('email', v), 500)
})

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await authStore.register({
      username: form.username, email: form.email,
      password: form.password, nickname: form.nickname, agreed: form.agreed
    })
    if (res.code === 200) {
      ElMessage.success('注册成功，即将跳转到登录页')
      setTimeout(() => router.push('/login?registered=1'), 1500)
    } else {
      const errMap: Record<number, string> = {
        1001: 'username', 1004: 'username', 1002: 'email',
        1005: 'email', 1003: 'password', 1007: ''
      }
      const field = errMap[res.code]
      if (field) {
        formRef.value?.validateField(field)
      }
      ElMessage.error(res.message || '注册失败')
    }
  } catch {
    ElMessage.error('网络异常，请稍后再试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-container">
    <el-card class="auth-card auth-card--wide">
      <h1 class="auth-title">创建账号</h1>
      <p class="auth-subtitle">填写以下信息完成注册</p>

      <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleRegister">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="4~20位字母/数字/下划线，字母开头"
            :prefix-icon="User" size="large" maxlength="20">
            <template #suffix>
              <span v-if="usernameAvailable === true" style="color:#27ae60">✓</span>
              <span v-else-if="usernameAvailable === false" style="color:#e74c3c">✗</span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱地址"
            :prefix-icon="Message" size="large" maxlength="100">
            <template #suffix>
              <span v-if="emailAvailable === true" style="color:#27ae60">✓</span>
              <span v-else-if="emailAvailable === false" style="color:#e74c3c">✗</span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="至少8位，包含字母和数字"
            :prefix-icon="Lock" show-password size="large" />
          <div v-if="strengthInfo" class="password-strength">
            <div class="strength-bar"><div class="strength-fill"
              :style="{ width: strengthInfo.width, background: strengthInfo.color }" /></div>
            <span class="strength-text" :style="{ color: strengthInfo.color }">{{ strengthInfo.label }}</span>
          </div>
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码"
            :prefix-icon="Lock" show-password size="large" />
        </el-form-item>

        <el-form-item>
          <el-input v-model="form.nickname" placeholder="昵称（选填，为空默认取用户名）"
            :prefix-icon="User" size="large" maxlength="20" />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="form.agreed">
            我已阅读并同意 <el-link type="primary" :underline="false">《用户协议》</el-link>
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="w-full" :loading="loading" size="large"
            :disabled="!canSubmit" @click="handleRegister">
            {{ loading ? '注册中...' : '注 册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="text-center" style="margin-top: 16px">
        已有账号？<el-link type="primary" href="/login">立即登录</el-link>
      </div>
    </el-card>
  </div>
</template>
