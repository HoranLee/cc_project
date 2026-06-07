<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确认退出登录吗？', '提示', {
      confirmButtonText: '确认退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await authStore.logout()
    router.push('/login')
  } catch {
    // 用户取消
  }
}

// 头像首字母
const avatarChar = computed(() => {
  const name = authStore.user?.nickname || authStore.user?.username || 'U'
  return name.charAt(0).toUpperCase()
})
</script>

<script lang="ts">
import { computed } from 'vue'
</script>

<template>
  <div class="dashboard">
    <el-header class="dashboard-header">
      <span class="logo">Demo</span>
      <el-button :icon="SwitchButton" @click="handleLogout" text>退出登录</el-button>
    </el-header>

    <el-main class="dashboard-main">
      <div class="welcome">
        <h2>欢迎回来，{{ authStore.user?.nickname || authStore.user?.username }}！</h2>
        <p>这是您的个人仪表盘</p>
      </div>

      <el-card class="info-card" v-if="authStore.user">
        <template #header><span>个人信息</span></template>
        <el-row :gutter="24">
          <el-col :span="24" style="text-align:center;margin-bottom:20px">
            <el-avatar :size="80" style="background:#409eff;font-size:32px">
              {{ avatarChar }}
            </el-avatar>
          </el-col>
          <el-col :span="12">
            <div class="info-item"><span class="label">用户名</span><span>{{ authStore.user.username }}</span></div>
          </el-col>
          <el-col :span="12">
            <div class="info-item"><span class="label">昵称</span><span>{{ authStore.user.nickname || '-' }}</span></div>
          </el-col>
          <el-col :span="12">
            <div class="info-item"><span class="label">邮箱</span><span>{{ authStore.user.email }}</span></div>
          </el-col>
          <el-col :span="12">
            <div class="info-item"><span class="label">最后登录</span><span>{{ authStore.user.lastLoginTime || '首次登录' }}</span></div>
          </el-col>
          <el-col :span="12">
            <div class="info-item"><span class="label">状态</span>
              <el-tag :type="authStore.user.status === 1 ? 'success' : 'danger'">
                {{ authStore.user.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </el-main>
  </div>
</template>

<style scoped>
.dashboard-header {
  display: flex; align-items: center; justify-content: space-between;
  background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,.06);
  padding: 0 32px; position: sticky; top: 0; z-index: 10;
}
.logo { font-size: 20px; font-weight: 700; color: #409eff; }
.dashboard-main { max-width: 800px; margin: 0 auto; padding: 32px 16px; }
.welcome { margin-bottom: 24px; }
.welcome h2 { margin: 0 0 4px; }
.welcome p { color: #909399; margin: 0; }
.info-item { padding: 8px 0; }
.info-item .label { display: block; font-size: 12px; color: #909399; margin-bottom: 2px; }
</style>
