<template>
  <div class="cal-page">
    <h2 class="page-title"><el-icon><Calendar /></el-icon> 月历</h2>

    <div class="cal-nav">
      <el-button size="small" @click="prevMonth"><el-icon><ArrowLeft /></el-icon></el-button>
      <span class="cal-range">{{ calYear }} 年 {{ calMonth }} 月</span>
      <el-button size="small" @click="nextMonth"><el-icon><ArrowRight /></el-icon></el-button>
      <el-button size="small" @click="goToday">今天</el-button>
    </div>

    <div class="cal-grid" v-loading="loading">
      <div class="cal-header-row">
        <div v-for="d in wdays" :key="d" class="cal-hcell">{{ d }}</div>
      </div>
      <div v-for="(row, ri) in calRows" :key="ri" class="cal-row">
        <div v-for="cell in row" :key="cell.date || cell.day"
             class="cal-cell" :class="{ today: cell.isToday, other: !cell.inMonth }">
          <div class="cal-dnum">{{ cell.day }}</div>
          <div class="cal-dline"></div>
          <div class="cal-tasks">
            <div v-for="t in (dataMap[cell.date] || [])" :key="t.id"
                 :class="['cal-t', taskRowClass(t)]">
              <div class="cal-t-dot" :class="{ 'has-fill': t.status === 'DONE' || (isCheckinType(t) && t.checkedToday) }" @click="doComplete(t)">
                <span v-if="t.status === 'DONE' || (isCheckinType(t) && t.checkedToday)" class="dot-fill"></span>
              </div>
              <div class="cal-t-body">
                <div class="cal-t-title" :title="t.title" @click="goTask(t)">{{ t.title }}</div>
                <div v-if="!isCheckinType(t)" class="cal-t-time">{{ fmtT(t.deadline) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from '@/store/index.js'
import routers from '@/router/routers.js'
import { getCalendarTasks } from '@/api/calendar/index.js'
import { doCheckIn, completeTask } from '@/api/study/checkin.js'

const store = useStore()
const loading = ref(false)
const dataMap = ref({})
const now = new Date()
const calYear = ref(now.getFullYear())
const calMonth = ref(now.getMonth() + 1)
const wdays = ['一','二','三','四','五','六','日']

const isCheckinType = (t) => {
  const ct = t.checkinType || 'DAILY'
  return ct !== 'NONE'
}
const isOverdue = (t) => t.status === 'TODO' && t.deadline && new Date(t.deadline) < new Date() && !t.checkedToday
const taskRowClass = (t) => {
  if (t.status === 'DONE') return 'done'
  if (isCheckinType(t) && t.checkedToday) return 'checked'
  if (isOverdue(t)) return 'overdue'
  if (isCheckinType(t)) return 'checkin'
  if (t.priority === 'HIGH') return 'pri-high'
  if (t.priority === 'MEDIUM') return 'pri-mid'
  return 'pri-low'
}
const fmtT = (d) => {
  if (!d) return ''
  const p = d.split('T')[1] || d
  return p.substring(0, 5)
}

const calRows = computed(() => {
  const firstDay = new Date(calYear.value, calMonth.value - 1, 1)
  const lastDay = new Date(calYear.value, calMonth.value, 0)
  const startDow = firstDay.getDay() || 7
  const daysInMonth = lastDay.getDate()
  const today = now.toISOString().split('T')[0]
  const rows = []; let row = []
  const prevLast = new Date(calYear.value, calMonth.value - 1, 0)
  for (let i = 1; i < startDow; i++) {
    row.push({ day: prevLast.getDate() - startDow + i + 1, date: '', inMonth: false, isToday: false })
  }
  for (let d = 1; d <= daysInMonth; d++) {
    const ds = `${calYear.value}-${String(calMonth.value).padStart(2,'0')}-${String(d).padStart(2,'0')}`
    row.push({ day: d, date: ds, inMonth: true, isToday: ds === today })
    if (row.length === 7) { rows.push(row); row = [] }
  }
  if (row.length > 0) {
    let nd = 1
    while (row.length < 7) { row.push({ day: nd++, date: '', inMonth: false, isToday: false }) }
    rows.push(row)
  }
  return rows
})

const load = async () => {
  loading.value = true
  const res = await getCalendarTasks(calYear.value, calMonth.value)
  if (res.success) {
    dataMap.value = res.data
  }
  loading.value = false
}

const doComplete = (t) => {
  if (isCheckinType(t)) {
    doCheckIn(t.id).then(r => {
      if (r.success) {
        // 只更新当前点击这个任务实例
        t.checkedToday = !t.checkedToday
      }
    })
  } else {
    completeTask(t.id).then(r => {
      if (r.success) {
        t.status = t.status === 'DONE' ? 'TODO' : 'DONE'
      }
    })
  }
}
const goTask = (t) => {
  store.activeIndex = '任务管理'
  store.addTabAction({ name: '任务管理', path: '/tasks' })
  routers.push({ path: '/tasks', query: { highlight: t.id } })
}

const prevMonth = () => {
  if (calMonth.value === 1) { calMonth.value = 12; calYear.value-- } else calMonth.value--
  load()
}
const nextMonth = () => {
  if (calMonth.value === 12) { calMonth.value = 1; calYear.value++ } else calMonth.value++
  load()
}
const goToday = () => { calYear.value = now.getFullYear(); calMonth.value = now.getMonth() + 1; load() }

onMounted(load)
</script>

<style scoped>
.cal-page { padding: 10px; background: #fff; height: calc(100vh - 100px); display: flex; flex-direction: column; }
.page-title { display: flex; align-items: center; gap: 8px; font-size: 27px; font-weight: 700; margin: -4px 0 10px; flex-shrink: 0; }
.cal-nav { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; flex-shrink: 0; }
.cal-range { font-size: 16px; font-weight: 700; color: #333; }

.cal-grid { flex: 1; display: flex; flex-direction: column; border-top: 2px solid #e0e0e0; }
.cal-header-row { display: grid; grid-template-columns: repeat(7, 1fr); border-bottom: 1px solid #e8e8e8; flex-shrink: 0; }
.cal-hcell { text-align: center; padding: 4px; font-size: 12px; font-weight: 600; color: #999; }
.cal-row { display: grid; grid-template-columns: repeat(7, 1fr); flex: 1; border-bottom: 1px solid #e8e8e8; }
.cal-row:last-child { border-bottom: none; flex: 0; }
.cal-cell { padding: 2px 3px; min-height: 0; overflow: hidden; display: flex; flex-direction: column; }
.cal-cell.other { opacity: 0.35; }
.cal-cell.today { background: #f9fdf9; }

.cal-dnum { text-align: center; font-size: 20px; font-weight: 700; padding: 4px 0 2px; flex-shrink: 0; color: #444; }
.cal-cell.today .cal-dnum { color: #3aad78; }
.cal-dline { height: 1px; background: #eee; margin: 2px 6px 4px; flex-shrink: 0; }

.cal-tasks { flex: 1; overflow-y: auto; min-height: 0; }
.cal-t { display: flex; align-items: flex-start; gap: 4px; padding: 2px 3px; margin: 1px 0; border-radius: 4px; transition: all 0.3s; }
.cal-t.overdue { color: #991b1b; background: #fef2f2; }
.cal-t.overdue .cal-t-title { text-decoration: line-through; }
.cal-t.checkin { color: #c2410c; background: #fff7ed; }
.cal-t.checked { color: #166534; background: #dcfce7; }
.cal-t.pri-high { color: #991b1b; background: #fef2f2; }
.cal-t.pri-mid { color: #6b21a8; background: #faf5ff; }
.cal-t.pri-low { color: #1e40af; background: #eff6ff; }
.cal-t.done { color: #999; background: #f5f5f5; }

.cal-t-dot { width: 12px; height: 12px; border-radius: 50%; border: 2px solid #ccc; flex-shrink: 0; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.3s cubic-bezier(0.4,0,0.2,1); position: relative; margin-top: 2px; }
.cal-t.overdue .cal-t-dot { border-color: #dc2626; }
.cal-t.overdue .dot-fill { background: #dc2626; }
.cal-t.checkin .cal-t-dot { border-color: #fb923c; }
.cal-t.checked .cal-t-dot { border-color: #22c55e; background: #fff; }
.cal-t.pri-high .cal-t-dot { border-color: #ef4444; }
.cal-t.pri-mid .cal-t-dot { border-color: #a855f7; }
.cal-t.pri-low .cal-t-dot { border-color: #60a5fa; }
.cal-t.done .cal-t-dot { border-color: #bbb; }
.dot-fill { width: 6px; height: 6px; border-radius: 50%; position: absolute; transition: all 0.3s cubic-bezier(0.4,0,0.2,1); }
.cal-t.checkin .dot-fill { background: #fb923c; width: 0; height: 0; }
.cal-t.checked .dot-fill { background: #22c55e; }
.cal-t.pri-high .dot-fill { background: #ef4444; }
.cal-t.pri-mid .dot-fill { background: #a855f7; }
.cal-t.pri-low .dot-fill { background: #60a5fa; }
.cal-t.done .dot-fill { background: #bbb; }

.cal-t-body { flex: 1; min-width: 0; font-size: 14px; font-weight: 700; }
.cal-t-title { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; cursor: pointer; line-height: 1.4; }
.cal-t-time { font-size: 16px; font-weight: 700; color: inherit; opacity: 0.85; display: block; width: 100%; }
</style>
