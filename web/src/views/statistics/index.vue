<template>
  <div class="stats-page">
    <h2 class="page-title"><el-icon><DataAnalysis /></el-icon> 数据统计</h2>
    <el-card class="stat-card">
      <h3>任务完成进度</h3>
      <el-progress :percentage="stats.rate" :color="'#67C23A'" :stroke-width="18" :text-inside="true"></el-progress>
      <p class="stat-desc">{{ stats.done }} / {{ stats.total }} 已完成，累计打卡 {{ stats.checkDays }} 天</p>
    </el-card>

    <el-card class="stat-card">
      <h3>今日打卡进度</h3>
      <el-progress :percentage="checkinRate" :color="'#409EFF'" :stroke-width="18" :text-inside="true"></el-progress>
      <p class="stat-desc">{{ stats.todayChecked || 0 }} / {{ stats.todoCount || stats.total }} 个任务今日已打卡</p>
    </el-card>

    <!-- 年历打卡热力图 -->
    <el-card class="stat-card">
      <h3>打卡年历</h3>
      <div class="year-cal">
        <!-- 月份行 -->
        <div class="ycal-months">
          <span v-for="m in ycalMonths" :key="m.label" class="ycal-month-label">{{ m.label }}</span>
        </div>
        <!-- 主体：星期行 x 周列 -->
        <div class="ycal-body">
          <div class="ycal-weekdays">
            <span>Mon</span><span></span><span>Wed</span><span></span><span>Fri</span><span></span><span>Sun</span>
          </div>
          <div class="ycal-grid">
            <div v-for="(col, ci) in ycalCols" :key="ci" class="ycal-col">
              <div v-for="(cell, ri) in col" :key="ri"
                   class="ycal-cell"
                   :class="{ filled: cell.count > 0 }"
                   :style="{ backgroundColor: cellColor(cell.count) }"
                   :title="cell.date + ': ' + cell.count + '次打卡'">
              </div>
            </div>
          </div>
        </div>
        <!-- 图例 -->
        <div class="ycal-legend">
          <span>Less</span>
          <span class="leg-swatch" style="background:#fff;border:1px solid #eee"></span>
          <span class="leg-swatch" style="background:#c6e48b"></span>
          <span class="leg-swatch" style="background:#7bc96f"></span>
          <span class="leg-swatch" style="background:#239a3b"></span>
          <span class="leg-swatch" style="background:#196127"></span>
          <span>More</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { getStatistics } from '@/api/study/statistics.js'
import { getYearCalendar } from '@/api/study/checkin.js'
import * as echarts from 'echarts'

const stats = ref({ total: 0, done: 0, rate: 0, checkDays: 0, todayChecked: 0, todoCount: 0, dailyStats: [] })
const loading = ref(false)
const calendarChart = ref(null)
const checkinRate = computed(() => {
  const todo = stats.value.todoCount || stats.value.total || 1
  return Math.round((stats.value.todayChecked || 0) / todo * 100)
})

// 年历数据
const ycalData = ref({})
const ycalMonths = ref([])
const ycalCols = ref([])

const cellColor = (count) => {
  if (!count || count === 0) return '#fff'
  if (count <= 1) return '#c6e48b'
  if (count <= 2) return '#7bc96f'
  if (count <= 4) return '#239a3b'
  return '#196127'
}

const monthNames = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec']
const dayNames = ['Mon','','Wed','','Fri','','']

const buildYearCal = () => {
  const now = new Date()
  // 上个月 + 当前季度 = 4个月
  const months = []
  const cols = []
  const data = ycalData.value

  for (let m = 0; m < 12; m++) {
    let y = now.getFullYear(), mo = now.getMonth() + 1 + m
    if (mo < 1) { mo += 12; y-- }
    if (mo > 12) { mo -= 12; y++ }
    const label = monthNames[mo - 1]
    months.push({ label, y, mo })

    const daysInMonth = new Date(y, mo, 0).getDate()
    // 每月第一个周一之前的空白 + 当月天数 → 分成周列
    const firstDow = new Date(y, mo - 1, 1).getDay() || 7
    let colWeek = []
    // 前置空白
    for (let d = 0; d < firstDow - 1; d++) colWeek.push({ date: '', count: 0 })
    for (let d = 1; d <= daysInMonth; d++) {
      const dateStr = `${y}-${String(mo).padStart(2,'0')}-${String(d).padStart(2,'0')}`
      colWeek.push({ date: dateStr, count: data[dateStr] || 0 })
      if (colWeek.length === 7) {
        cols.push([...colWeek])
        colWeek = []
      }
    }
    if (colWeek.length > 0) {
      while (colWeek.length < 7) colWeek.push({ date: '', count: 0 })
      cols.push([...colWeek])
    }
  }
  ycalMonths.value = months
  ycalCols.value = cols
}

onMounted(() => {
  loading.value = true
  getStatistics().then(res => { if (res.success) stats.value = res.data; loading.value = false })

  const now = new Date()
  let fromY = now.getFullYear(), fromM = now.getMonth() - 9  // 9个月前
  if (fromM <= 0) { fromM += 12; fromY-- }
  getYearCalendar(fromY, fromM, 12).then(res => {
    if (res.success) {
      ycalData.value = res.data
      buildYearCal()
    }
  })
})
</script>

<style scoped>
.stats-page { padding: 10px; }
.page-title { display: flex; align-items: center; gap: 8px; font-size: 27px; font-weight: 700; margin: -4px 0 10px; }
.stat-card { margin-bottom: 12px; padding: 12px 16px; }
.stat-card h3 { margin: 0 0 8px; font-size: 16px; font-weight: 700; }
.stat-desc { color: #909399; margin: 6px 0 0; font-size: 14px; }

.cal-header { display: flex; align-items: center; justify-content: center; gap: 16px; margin-bottom: 12px; }
.cal-table { width: 100%; border-collapse: collapse; text-align: center; font-size: 15px; }
.cal-table th { padding: 8px; color: #909399; font-weight: 600; }
.cal-table td { padding: 8px; border-radius: 6px; cursor: default; }
.cal-table td.other { color: #d0d0d0; }
.cal-table td.today { background: #409EFF; color: #fff; font-weight: 700; border-radius: 50%; }
.cal-table td.today .cal-day { display: inline-flex; align-items: center; justify-content: center; width: 28px; height: 28px; }
.cal-day { display: inline-block; }

.year-cal { font-size: 12px; }
.ycal-months { display: flex; margin-left: 32px; margin-bottom: 4px; }
.ycal-month-label { flex: 1; text-align: center; font-size: 11px; color: #909399; }
.ycal-body { display: flex; }
.ycal-weekdays { display: grid; grid-template-rows: repeat(7, 1fr); width: 26px; padding-right: 6px; gap: 3px; padding-top: 2px; }
.ycal-weekdays span { font-size: 10px; color: #909399; display: flex; align-items: center; }
.ycal-grid { display: flex; overflow-x: auto; flex: 1; gap: 3px; }
.ycal-col { display: flex; flex-direction: column; flex: 1; min-width: 14px; max-width: 22px; gap: 3px; }
.ycal-cell { width: 100%; aspect-ratio: 1; border-radius: 3px; background: #fff; border: 1px solid #eee; }
.ycal-cell.filled { border: 1px solid rgba(0,0,0,0.1); }
.ycal-legend { display: flex; align-items: center; justify-content: flex-end; gap: 4px; margin-top: 8px; font-size: 11px; color: #909399; }
.leg-swatch { width: 12px; height: 12px; border-radius: 2px; }
</style>
