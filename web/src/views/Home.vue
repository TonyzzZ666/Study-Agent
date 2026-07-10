<template>
  <div class="home">
    <h2 class="page-title"><el-icon><DataAnalysis /></el-icon> 学习概览</h2>
    <el-row :gutter="20">
      <el-col :span="6"><el-card shadow="hover" class="click-card" @click="goTasks('todo')"><div class="stat-num">{{ stats.total }}</div><div class="stat-label">总任务数</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover" class="click-card" @click="goTasks('done')"><div class="stat-num" style="color:#67C23A">{{ stats.done }}</div><div class="stat-label">已完成</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><div class="stat-num" style="color:#409EFF">{{ stats.rate }}%</div><div class="stat-label">完成率</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><div class="stat-num" style="color:#E6A23C">{{ stats.checkDays }}</div><div class="stat-label">打卡天数</div></el-card></el-col>
    </el-row>

    <!-- 今日需完成 -->
    <h3 class="sec-title"><el-icon><Checked /></el-icon> 今日需完成</h3>
    <div v-if="todayComplete.length===0" class="sec-empty">今天没有需要完成的任务</div>
    <div v-else class="sec-list">
      <div v-for="t in todayComplete" :key="t.id" class="sec-row" :class="{ done: t.status==='DONE', closing: t._closing, overdue: isOverdue(t) }">
        <div class="sec-dot" :class="{ done: t.status==='DONE' }" @click="doComplete(t)">
          <span v-if="t.status==='DONE'" class="dot-fill"></span>
        </div>
        <span class="sec-title" :class="{ 'done-text': t.status==='DONE' }" @click="goTask(t)">{{ t.title }} <span class="sec-deadline" :class="{ overdue: isOverdue(t) }">{{ fmtTime(t.deadline) }}</span></span>
        <div class="sec-del" @click="doDel(t)"><el-icon><Delete /></el-icon></div>
        <span class="sec-pri" :class="'pri-'+t.priority">{{ t.priority==='HIGH'?'高':t.priority==='MEDIUM'?'中':'低' }}</span>
      </div>
    </div>

    <!-- 今日需打卡 -->
    <h3 class="sec-title"><el-icon><CircleCheck /></el-icon> 今日需打卡</h3>
    <div v-if="todayCheckinList.length===0" class="sec-empty">今天没有需要打卡的任务</div>
    <div v-else class="sec-list">
      <div v-for="t in todayCheckinList" :key="t.id" class="sec-row" :class="{ closing: t._closing, 'checked-row': todayCheckedIds.has(t.id), overdue: isOverdue(t) && !todayCheckedIds.has(t.id) }">
        <div class="sec-dot checkin" :class="{ checked: todayCheckedIds.has(t.id) }" @click="doCheckin(t)">
          <span v-if="todayCheckedIds.has(t.id)" class="dot-fill"></span>
        </div>
        <span class="sec-title" @click="goTask(t)">{{ t.title }}</span>
        <span class="sec-pri" :class="'pri-'+t.priority">{{ t.priority==='HIGH'?'高':t.priority==='MEDIUM'?'中':'低' }}</span>
      </div>
    </div>

    <!-- 即将到期 -->
    <h3 class="sec-title"><el-icon><Clock /></el-icon> 即将到期的任务</h3>
    <div v-if="upcoming.length===0" class="sec-empty">暂无即将到期的任务</div>
    <div v-else class="sec-list">
      <div v-for="t in upcoming" :key="t.id" class="sec-row" @click="goTask(t)" :class="{ overdue: isOverdue(t) }">
        <span class="sec-title">{{ t.title }}</span>
        <span class="sec-date">{{ fmtShort(t.deadline) }}</span>
        <span class="sec-pri" :class="'pri-'+t.priority">{{ t.priority==='HIGH'?'高':t.priority==='MEDIUM'?'中':'低' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useStore } from '@/store/index.js'
import routers from '@/router/routers.js'
import { getStatistics } from '@/api/study/statistics.js'
import { getUpcoming, getTodayCheckinTasks, getTodayCompleteTasks } from '@/api/study/task.js'
import { doCheckIn, completeTask, getTodayCheckIns } from '@/api/study/checkin.js'
import { deleteTask } from '@/api/study/task.js'

const store = useStore()
const stats = ref({ total: 0, done: 0, rate: 0, checkDays: 0 })
const upcoming = ref([]), todayCheckinList = ref([]), todayComplete = ref([]), todayCheckedIds = ref(new Set())

const fmtShort = (d) => { if (!d) return ''; const p = d.split('T')[0]; const [,m,da] = p.split('-'); return m+'-'+da }
const isOverdue = (t) => t.status==='TODO' && t.deadline && new Date(t.deadline) < new Date()
const fmtTime = (d) => { if (!d) return ''; const parts = d.split(' '); if (parts.length>1) return parts[1].substring(0,5); const t = d.split('T')[1]; if (t) return t.substring(0,5); return d.substring(11,16) }

onMounted(async () => {
  getStatistics().then(r => { if (r.success) stats.value = r.data }).catch(() => {})
  getUpcoming().then(r => { if (r.success) upcoming.value = r.data.filter(t => !isOverdue(t)) }).catch(() => {})
  getTodayCheckinTasks().then(r => { if (r.success) todayCheckinList.value = r.data }).catch(() => {})
  getTodayCompleteTasks().then(r => { if (r.success) todayComplete.value = r.data }).catch(() => {})
  getTodayCheckIns().then(r => { if (r.success) todayCheckedIds.value = new Set(r.data) }).catch(() => {})
})

const goTasks = (tab) => { store.activeIndex = '任务管理'; store.addTabAction({ name: '任务管理', path: '/tasks' }); routers.push({ path: '/tasks', query: { tab, highlight: '' } }) }
const goTask = (row) => { store.activeIndex = '任务管理'; store.addTabAction({ name: '任务管理', path: '/tasks' }); routers.push({ path: '/tasks', query: { highlight: row.id } }) }

const doComplete = (t) => {
  if (t._closing) return; t._closing = true
  setTimeout(() => {
    completeTask(t.id).then(r => {
      if (r.success) { t.status = t.status === 'DONE' ? 'TODO' : 'DONE'; t._closing = false }
    })
  }, 300)
}
const doCheckin = (t) => {
  if (t._closing) return
  doCheckIn(t.id).then(r => {
    if (r.success) {
      const s = new Set(todayCheckedIds.value)
      s.has(t.id) ? s.delete(t.id) : s.add(t.id)
      todayCheckedIds.value = s
    }
  })
}
const doDel = (t) => {
  if (t._closing) return; t._closing = true
  setTimeout(() => { deleteTask(t.id).then(r => { if (r.success) todayComplete.value = todayComplete.value.filter(x => x.id !== t.id) }) }, 300)
}
</script>

<style scoped>
.home { padding: 10px; }
.page-title { display: flex; align-items: center; gap: 8px; font-size: 27px; font-weight: 700; margin: -4px 0 14px; }
.stat-num { font-size: 36px; font-weight: bold; text-align: center; }
.stat-label { text-align: center; color: #909399; margin-top: 8px; }
.click-card { cursor: pointer; transition: transform 0.15s; }
.click-card:hover { transform: scale(1.03); }

.sec-title { display: flex; align-items: center; gap: 6px; font-size: 18px; font-weight: 700; margin: 22px 0 10px; }
.sec-empty { color: #c0c4cc; font-size: 14px; padding: 12px 0; }
.sec-list { display: flex; flex-direction: column; gap: 10px; }

.sec-row { display: flex; align-items: center; gap: 8px; padding: 0 14px; height: 46px; box-sizing: border-box; background: #fff; border-radius: 10px; border: 1px solid #f0f0f0; box-shadow: 0 1px 3px rgba(0,0,0,0.04); transition: all 0.3s; }
.sec-row:hover { background: #f8faf8; }
.sec-row.closing { opacity: 0.3; filter: grayscale(1); }
.sec-row.done { background: #f9f9f9; }
.sec-row.checked-row { background: #dcfce7; }
.sec-row.overdue { border-color: #fca5a5; background: #fefafa; }
.sec-title { font-size: 18px; font-weight: 600; color: #333; flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; cursor: default; display: flex; align-items: center; gap: 4px; }
.done-text { color: #bbb !important; text-decoration: line-through; }
.sec-date { font-size: 13px; color: #999; flex-shrink: 0; }
.sec-deadline { font-size: 16px; font-weight: 600; color: #3aad78; flex-shrink: 0; white-space: nowrap; }
.sec-deadline.overdue { color: #dc2626; }
.sec-pri { font-size: 12px; font-weight: 600; padding: 2px 8px; border-radius: 4px; flex-shrink: 0; }
.pri-HIGH { color: #991b1b; background: #fef2f2; }
.pri-MEDIUM { color: #6b21a8; background: #faf5ff; }
.pri-LOW { color: #1e40af; background: #eff6ff; }

.sec-dot { width: 16px; height: 16px; border-radius: 50%; border: 2px solid #ccc; flex-shrink: 0; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.3s; position: relative; }
.sec-dot:hover { transform: scale(1.15); }
.sec-dot.done { border-color: #bbb; }
.sec-dot.checkin { border-color: #fb923c; }
.sec-dot.checked { border-color: #22c55e; }
.dot-fill { width: 8px; height: 8px; border-radius: 50%; background: #bbb; position: absolute; }
.sec-dot.checkin .dot-fill { background: #fb923c; }
.sec-dot.checked .dot-fill { background: #22c55e; }

.sec-del { color: #ccc; cursor: pointer; flex-shrink: 0; transition: color 0.2s; display: flex; align-items: center; }
.sec-del:hover { color: #f56c6c; }
</style>
