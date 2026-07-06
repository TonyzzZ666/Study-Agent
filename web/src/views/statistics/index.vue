<template>
  <div class="stats-page">
    <h2>📊 数据统计</h2>
    <!-- 进度条 -->
    <el-card style="margin-bottom:20px">
      <h3>任务完成进度</h3>
      <el-progress :percentage="stats.rate" :color="'#67C23A'" :stroke-width="20" :text-inside="true"></el-progress>
      <p style="color:#909399;margin-top:10px">{{ stats.done }} / {{ stats.total }} 已完成，累计打卡 {{ stats.checkDays }} 天</p>
    </el-card>

    <!-- 统计表 -->
    <el-card style="margin-bottom:20px">
      <h3>每日统计（近7天）</h3>
      <el-table :data="stats.dailyStats || []" v-loading="loading">
        <el-table-column prop="date" label="日期" width="130"></el-table-column>
        <el-table-column prop="created" label="新增任务" width="100"></el-table-column>
        <el-table-column prop="completed" label="完成数" width="100"></el-table-column>
      </el-table>
    </el-card>

    <!-- 打卡日历 -->
    <el-card>
      <h3>打卡日历（本月）</h3>
      <div ref="calendarChart" style="width:100%;height:200px;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { getStatistics } from '@/api/study/statistics.js'
import { getCalendarData } from '@/api/study/checkin.js'
import * as echarts from 'echarts'

const stats = ref({ total: 0, done: 0, rate: 0, checkDays: 0, dailyStats: [] })
const loading = ref(false)
const calendarChart = ref(null)

onMounted(() => {
  loading.value = true
  getStatistics().then(res => { if (res.success) stats.value = res.data; loading.value = false })

  const now = new Date()
  getCalendarData(now.getFullYear(), now.getMonth() + 1).then(res => {
    if (res.success && res.data) {
      nextTick(() => {
        const chart = echarts.init(calendarChart.value)
        const data = res.data.map(d => [d.date, d.count])
        chart.setOption({
          tooltip: { formatter: p => p.data[0] + ': ' + p.data[1] + '次打卡' },
          visualMap: { min: 0, max: Math.max(...res.data.map(d => d.count), 1), type: 'piecewise',
            orient: 'horizontal', left: 'center', top: 0,
            pieces: [{ min: 1, color: '#C6E48B' }, { min: 0, color: '#EBEDF0' }] },
          calendar: { range: [now.getFullYear() + '-' + String(now.getMonth()+1).padStart(2,'0')],
            cellSize: ['auto', 20], dayLabel: { nameMap: 'ZH' }, monthLabel: { nameMap: 'ZH' } },
          series: [{ type: 'heatmap', coordinateSystem: 'calendar', data: data }]
        })
      })
    }
  })
})
</script>

<style scoped>
.stats-page { padding: 10px; }
</style>
