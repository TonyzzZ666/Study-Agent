<template>
  <div class="aside-wrap" :class="{ collapsed: isCollapsed }">
    <el-menu :default-active="defaultActive" :unique-opened="true" router
             background-color="#0f3d2d" text-color="rgba(255,255,255,0.85)" active-text-color="#2dd4a0" class="menu">
      <!-- Logo 触发区域 -->
      <div class="logo-trigger" @click="toggleCollapse">
        <img src="/logo2.png" class="logo-img" alt="JADE">
        <el-icon v-if="!isCollapsed" class="collapse-arrow"><ArrowLeft /></el-icon>
      </div>
      <el-menu-item route="/home" index="首页" @click="openTab('首页', '/home')">
        <el-icon><HomeFilled /></el-icon>
        <template #title>首页</template>
      </el-menu-item>
      <el-menu-item route="/tasks" index="任务管理" @click="openTab('任务管理', '/tasks')">
        <el-icon><List /></el-icon>
        <template #title>任务管理</template>
      </el-menu-item>
      <el-menu-item index="打卡记录" @click="goCheckin">
        <el-icon><CircleCheck /></el-icon>
        <template #title>打卡记录</template>
      </el-menu-item>
      <el-menu-item route="/statistics" index="数据统计" @click="openTab('数据统计', '/statistics')">
        <el-icon><DataAnalysis /></el-icon>
        <template #title>数据统计</template>
      </el-menu-item>
    </el-menu>

    <div class="user-entry" @click="goProfile">
      <div class="user-avatar" :style="avatarStyle">
        <span v-if="!store.userInfo?.avatar">{{ avatarLetter }}</span>
      </div>
      <div class="user-info" v-show="!isCollapsed">
        <span class="user-name">{{ username }}</span>
        <span class="user-sig">{{ store.userInfo?.signature || '写句座右铭鼓励一下自己吧！' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import {useStore} from "@/store/index.js";
import {computed, ref, onMounted, watch} from "vue";
import routers from "@/router/routers.js";
import { getProfile } from "@/api/login/profile.js";

const store = useStore()
const isCollapsed = ref(false)

const loadAvatar = async () => {
  if (!store.token) return
  const res = await getProfile()
  if (res && res.success) {
    store.userInfo = { ...store.userInfo, nickName: res.data.nickName, avatar: res.data.avatar, signature: res.data.signature }
  }
}
onMounted(loadAvatar)
watch(() => store.token, (val) => { if (val) loadAvatar() })

const username = computed(() => store.userInfo?.nickName || store.userInfo?.username || '用户')
const avatarLetter = computed(() => username.value.charAt(0).toUpperCase())
const avatarStyle = computed(() => {
  if (store.userInfo?.avatar) {
    return { backgroundImage: `url(${store.userInfo.avatar})`, backgroundSize: 'cover' }
  }
  return {}
})

const defaultActive = computed(() => store.activeIndex)
const openTab = (name, path) => {
  store.addTabAction({name: name, path: path})
  store.activeIndex = name
}
const goCheckin = () => {
  store.addTabAction({name: '打卡记录', path: '/checkin'})
  store.activeIndex = '打卡记录'
  routers.push('/checkin')
}
const goProfile = () => {
  store.addTabAction({name: '个人中心', path: '/profile'})
  store.activeIndex = '个人中心'
  routers.push('/profile')
}
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}
</script>

<style scoped>
.aside-wrap { height: 100%; display: flex; flex-direction: column; width: 210px; transition: width 0.35s cubic-bezier(0.4, 0, 0.2, 1); overflow: hidden; }
.aside-wrap.collapsed { width: 64px; }
.menu { flex: 1; overflow-y: auto; overflow-x: hidden; border-right: none !important; }
.el-menu { border-right: none !important; }

.aside-wrap.collapsed :deep(.el-menu-item) { padding-left: 20px !important; }
.aside-wrap.collapsed :deep(.el-menu-item .el-icon) { margin-right: 30px !important; }
:deep(.el-menu-item) {
  font-size: 15px !important; height: 48px !important; line-height: 48px !important;
  white-space: nowrap; overflow: hidden;
}
:deep(.el-menu-item .el-tooltip__trigger) { display: flex; align-items: center; }
:deep(.el-menu-item span) { transition: opacity 0.35s ease; }
.aside-wrap.collapsed :deep(.el-menu-item .el-tooltip__trigger span) { opacity: 0; }

.logo-trigger {
  display: flex; align-items: center; justify-content: flex-start;
  height: 60px; cursor: pointer; transition: background 0.2s;
  padding: 0 16px 0 12px; position: relative;
}
.logo-trigger:hover { background: rgba(0,0,0,0.12); }
.logo-trigger:active { background: rgba(0,0,0,0.2); }
.logo-img { height: 34px; object-fit: contain; }
.collapse-arrow {
  position: absolute; right: 8px; top: 50%; transform: translateY(-50%);
  color: rgba(255,255,255,0.4); font-size: 14px;
  transition: color 0.2s;
}
.logo-trigger:hover .collapse-arrow { color: rgba(255,255,255,0.8); }

.user-entry {
  display: flex; align-items: center; gap: 10px; padding: 12px;
  background: rgba(8, 35, 22, 0.95); cursor: pointer; transition: all 0.2s;
  box-sizing: border-box; width: 100%;
  box-shadow: 0 -2px 12px rgba(0,0,0,0.3), 0 -1px 3px rgba(0,0,0,0.2);
  position: relative; z-index: 2;
}
.user-entry:hover { background: rgba(12, 45, 28, 0.95); }
.user-avatar {
  width: 40px; height: 40px; border-radius: 50%; flex-shrink: 0;
  background: #909399; color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 17px; font-weight: bold;
}
.user-info { display: flex; flex-direction: column; min-width: 0; }
.user-name { color: #fff; font-size: 14px; font-weight: 600; line-height: 1.3; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.user-sig { color: #909399; font-size: 12px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
:deep(.el-menu-item) { font-size: 17px !important; font-weight: 600; height: 56px !important; line-height: 56px !important; }
:deep(.el-menu-item .el-icon) { font-size: 22px !important; }
</style>
