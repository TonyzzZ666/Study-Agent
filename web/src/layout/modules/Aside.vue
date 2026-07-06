<template>
  <el-menu :default-active="defaultActive" :unique-opened="true" router
           background-color="#545c64" text-color="#fff">
    <div class="logo" @click="changeCollapse">
      <span style="color:white; font-size:18px;">📚 学习养成</span>
    </div>
    <el-menu-item route="/home" index="首页" @click="openTab('首页', '/home')">
      <i class="iconfont icon-home"></i>
      <template #title>首页</template>
    </el-menu-item>
    <el-menu-item route="/tasks" index="任务管理" @click="openTab('任务管理', '/tasks')">
      <i class="iconfont icon-home"></i>
      <template #title>任务管理</template>
    </el-menu-item>
    <el-menu-item route="/checkin" index="打卡记录" @click="openTab('打卡记录', '/checkin')">
      <i class="iconfont icon-home"></i>
      <template #title>打卡记录</template>
    </el-menu-item>
    <el-menu-item route="/statistics" index="数据统计" @click="openTab('数据统计', '/statistics')">
      <i class="iconfont icon-home"></i>
      <template #title>数据统计</template>
    </el-menu-item>
  </el-menu>
</template>

<script setup>
import {useStore} from "@/store/index.js";
import {computed, ref} from "vue";

const store = useStore()
const emit = defineEmits(['update:width'])
const isCollapse = ref(false)

const defaultActive = computed(() => store.activeIndex)

const openTab = (name, path) => {
  store.addTabAction({name: name, path: path})
  store.activeIndex = name
}
const changeCollapse = () => {
  isCollapse.value = !isCollapse.value
  emit('update:width', isCollapse.value ? '64px' : '200px')
}
</script>

<style scoped>
.el-menu{ height: 100%; }
.logo{
  display: flex; justify-content: center; align-items: center;
  color: white; font-size: 20px; height: 60px; cursor: pointer;
}
</style>
