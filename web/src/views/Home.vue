<template>
  <div class="home">
    <h2 class="page-title"><el-icon><DataAnalysis /></el-icon> 学习概览</h2>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="click-card" @click="goTasks('todo')"><div class="stat-num">{{ stats.total }}</div><div class="stat-label">总任务数</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="click-card" @click="goTasks('done')"><div class="stat-num" style="color:#67C23A">{{ stats.done }}</div><div class="stat-label">已完成</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div class="stat-num" style="color:#409EFF">{{ stats.rate }}%</div><div class="stat-label">完成率</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div class="stat-num" style="color:#E6A23C">{{ stats.checkDays }}</div><div class="stat-label">打卡天数</div></el-card>
      </el-col>
    </el-row>

    <h3 style="margin-top:30px;"><el-icon><Clock /></el-icon> 即将到期的任务</h3>
    <el-table :data="upcoming" empty-text="暂无即将到期的任务" class="upcoming-table" @row-click="goTask">
      <el-table-column prop="title" label="任务" min-width="120">
        <template #default="s">
          <span class="up-task-title">{{ s.row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="deadline" label="截止时间" width="180"></el-table-column>
      <el-table-column prop="priority" label="优先级" width="100">
        <template #default="s">
          <el-tag :type="s.row.priority==='HIGH'?'danger':s.row.priority==='MEDIUM'?'warning':'info'">
            {{ s.row.priority === 'HIGH' ? '高' : s.row.priority === 'MEDIUM' ? '中' : '低' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <h3 style="margin-top:24px;">● 今日需打卡</h3>
    <el-table :data="todayCheckinList" empty-text="今日没有需要打卡的任务" class="upcoming-table" @row-click="goTask" :row-class-name="checkinRowClass">
      <el-table-column prop="title" label="任务" min-width="120">
        <template #default="s">
          <span class="up-task-title">{{ s.row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="deadline" label="截止时间" width="180"></el-table-column>
      <el-table-column prop="priority" label="优先级" width="100">
        <template #default="s">
          <el-tag :type="s.row.priority==='HIGH'?'danger':s.row.priority==='MEDIUM'?'warning':'info'">
            {{ s.row.priority === 'HIGH' ? '高' : s.row.priority === 'MEDIUM' ? '中' : '低' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90">
        <template #default="s">
          <el-button size="small" type="success" :disabled="checkinDoneBtn(s.row.id)" @click.stop="doCheckFromHome(s.row)">
            {{ checkinDoneBtn(s.row.id) ? '已打卡' : '打卡' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useStore } from '@/store/index.js'
import routers from '@/router/routers.js'
import { getStatistics } from '@/api/study/statistics.js'
import { getUpcoming, getTodayCheckinTasks } from '@/api/study/task.js'
import { doCheckIn, getTodayCheckIns } from '@/api/study/checkin.js'

const store = useStore()
const loading = ref(false)
const stats = ref({ total: 0, done: 0, rate: 0, checkDays: 0 })
const upcoming = ref([])
const todayCheckinList = ref([])
const todayCheckedIds = ref(new Set())

onMounted(() => {
  loading.value = true
  getStatistics().then(res => { if (res.success) stats.value = res.data })
    .catch(() => {})
    .finally(() => { loading.value = false })

  getUpcoming().then(res => { if (res.success) upcoming.value = res.data }).catch(() => {})
  getTodayCheckinTasks().then(res => { if (res.success) todayCheckinList.value = res.data }).catch(() => {})
  getTodayCheckIns().then(res => { if (res.success) todayCheckedIds.value = new Set(res.data) }).catch(e => console.error(e))
})

const goTasks = (tab) => {
  store.activeIndex = '任务管理'
  store.addTabAction({ name: '任务管理', path: '/tasks' })
  routers.push({ path: '/tasks', query: { tab, highlight: '' } })
}
const goTask = (row) => {
  store.activeIndex = '任务管理'
  store.addTabAction({ name: '任务管理', path: '/tasks' })
  routers.push({ path: '/tasks', query: { highlight: row.id } })
}
const checkinDoneBtn = (id) => todayCheckedIds.value.has(id)
const checkinRowClass = ({ row }) => row._closing ? 'closing-row' : ''

const doCheckFromHome = (row) => {
  if (row._closing) return
  row._closing = true
  setTimeout(() => {
    doCheckIn(row.id).then(res => {
      if (res.success) {
        todayCheckinList.value = todayCheckinList.value.filter(t => t.id !== row.id)
        const s = new Set(todayCheckedIds.value)
        s.add(row.id)
        todayCheckedIds.value = s
      } else {
        row._closing = false
      }
    })
  }, 800)
}
</script>

<style scoped>
.home { padding: 10px; }
.page-title { display: flex; align-items: center; gap: 8px; font-size: 27px; font-weight: 700; margin: -4px 0 10px; }
.stat-num { font-size: 36px; font-weight: bold; text-align: center; }
.stat-label { text-align: center; color: #909399; margin-top: 8px; }
.click-card { cursor: pointer; transition: transform 0.15s; }
.click-card:hover { transform: scale(1.03); }
.upcoming-table :deep(.el-table__row) { cursor: pointer; }
.upcoming-table :deep(.el-table__row:hover) { background-color: #f5f7fa; }
.upcoming-table { height: auto !important; max-height: none !important; }
.upcoming-table :deep(.el-table__row) { height: 52px; }
.upcoming-table :deep(.el-table__body-wrapper .el-table__row) { transition: transform 0.3s ease; }
.upcoming-table :deep(.closing-row) { opacity: 0.3; filter: grayscale(1); transition: all 0.8s ease; }
.up-task-title { font-size: 16px; font-weight: 700; }
</style>
