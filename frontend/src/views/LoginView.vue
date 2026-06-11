<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const showSuccess = ref(route.query.registered === '1')

const form = reactive({ account: '', password: '', rememberMe: false })

const rules: FormRules = {
  account: [{ required: true, message: '请输入用户名或邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await authStore.login({
      account: form.account,
      password: form.password,
      rememberMe: form.rememberMe
    })
    if (res.code === 200) {
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } else {
      ElMessage.error(res.message || '账号或密码错误')
    }
  } catch (err: any) {
    ElMessage.error(err?.message || '网络异常，请稍后再试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-container">
    <el-card class="auth-card">
      <h1 class="auth-title">欢迎回来</h1>
      <p class="auth-subtitle">请使用您的账号登录</p>

      <el-alert v-if="showSuccess" title="注册成功！请使用新账号登录" type="success"
        show-icon :closable="true" @close="showSuccess = false" style="margin-bottom: 16px" />

      <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleLogin">
        <el-form-item prop="account">
          <el-input v-model="form.account" placeholder="用户名或邮箱" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码"
            :prefix-icon="Lock" show-password size="large" />
        </el-form-item>
        <el-form-item>
          <div style="display:flex;justify-content:space-between;width:100%">
            <el-checkbox v-model="form.rememberMe">记住我</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码？</el-link>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="w-full" :loading="loading" size="large" @click="handleLogin">
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="text-center" style="margin-top: 16px">
        还没有账号？<el-link type="primary" href="/register">立即注册</el-link>
      </div>
    </el-card>
  </div>
</template>
