<template>
  <div class="aside-wrap">
    <el-menu :default-active="defaultActive" :unique-opened="true" router
             background-color="#545c64" text-color="#fff" class="menu">
      <div class="logo">
        <span class="logo-text">JADE</span>
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
      <div class="user-info">
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
</script>

<style scoped>
.aside-wrap { height: 100%; display: flex; flex-direction: column; }
.menu { flex: 1; overflow-y: auto; }
.logo{
  display: flex; justify-content: center; align-items: center;
  height: 60px; cursor: pointer;
}
.logo-text { color: #fff; font-size: 24px; font-weight: 900; letter-spacing: 5px; }
.user-entry {
  display: flex; align-items: center; gap: 10px; padding: 12px 12px;
  background: #4a5259; cursor: pointer; transition: background 0.2s;
  box-sizing: border-box; width: 100%;
}
.user-entry:hover { background: #5a6269; }
.user-avatar {
  width: 40px; height: 40px; border-radius: 50%; flex-shrink: 0;
  background: #909399; color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 17px; font-weight: bold;
}
.user-info { display: flex; flex-direction: column; min-width: 0; }
.user-name { color: #fff; font-size: 14px; font-weight: 600; line-height: 1.3; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.user-sig { color: #909399; font-size: 11px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
