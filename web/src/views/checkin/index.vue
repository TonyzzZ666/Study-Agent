<template>
  <div class="checkin-page">
    <h2 class="page-title"><el-icon><CircleCheck /></el-icon> 打卡记录</h2>

    <!-- 今日打卡 -->
    <h3 class="section-title"><el-icon><Clock /></el-icon> 今日打卡</h3>
    <div v-if="todayCheckins.length === 0 && !loading" class="empty-hint">今日还没有完成打卡任务哦</div>
    <el-table v-else :data="todayCheckins" v-loading="loading" style="margin-top:10px" class="auto-height">
      <el-table-column label="任务" min-width="140">
        <template #default="s"><span :class="{ 'done-text': s.row.status === 'DONE' }">{{ s.row.title }}</span></template>
      </el-table-column>
      <el-table-column label="截止时间" width="140">
        <template #default="s">{{ fmtTime(s.row.deadline) }}</template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="90">
        <template #default="s">
          <el-tag :type="s.row.priority==='HIGH'?'danger':s.row.priority==='MEDIUM'?'warning':'info'">{{ s.row.priority }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="打卡时间" width="130">
        <template #default="s">{{ fmtTime(s.row.checkTime) }}</template>
      </el-table-column>
    </el-table>

    <!-- 历史打卡（可折叠） -->
    <div class="history-header" @click="historyOpen = !historyOpen">
      <h3 class="section-title"><el-icon><Timer /></el-icon> 历史打卡</h3>
      <span class="collapse-arrow" :class="{ open: historyOpen }">{{ historyOpen ? '▼' : '▶' }}</span>
    </div>
    <div class="collapse-wrap" :class="{ open: historyOpen }">
      <div class="collapse-inner">
        <el-table :data="historyCheckins" style="margin-top:10px" class="auto-height">
          <el-table-column label="任务" min-width="140">
            <template #default="s"><span :class="{ 'done-text': s.row.status === 'DONE' }">{{ s.row.title }}</span></template>
          </el-table-column>
          <el-table-column label="截止时间" width="140">
            <template #default="s">{{ fmtTime(s.row.deadline) }}</template>
          </el-table-column>
          <el-table-column prop="priority" label="优先级" width="90">
            <template #default="s">
              <el-tag :type="s.row.priority==='HIGH'?'danger':s.row.priority==='MEDIUM'?'warning':'info'">{{ s.row.priority }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="打卡时间" width="130">
            <template #default="s">{{ fmtTime(s.row.checkTime) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getCheckInHistory } from '@/api/study/checkin.js'

const history = ref([]), loading = ref(false), historyOpen = ref(true)

const fmtTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  return `${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

const todayStr = new Date().toDateString()
const todayCheckins = computed(() => history.value.filter(c => new Date(c.checkTime).toDateString() === todayStr))
const historyCheckins = computed(() => history.value.filter(c => new Date(c.checkTime).toDateString() !== todayStr))

onMounted(() => {
  loading.value = true
  getCheckInHistory().then(res => { if (res.success) history.value = res.data; loading.value = false })
})
</script>

<style scoped>
.checkin-page { padding: 10px; }
.page-title { display: flex; align-items: center; gap: 8px; font-size: 20px; font-weight: 700; margin: 0 0 14px; }
.section-title { display: flex; align-items: center; gap: 6px; font-size: 16px; margin: 20px 0 4px; }
.empty-hint { text-align: center; color: #C0C4CC; font-size: 15px; padding: 30px 0; }
.auto-height { height: auto !important; max-height: none !important; font-size: 15px; }
.auto-height :deep(.el-table__row) { font-size: 15px; }
.history-header { display: flex; align-items: center; justify-content: space-between; cursor: pointer; user-select: none; }
.history-header h3 { margin: 20px 0 4px; }
.history-count { font-size: 13px; color: #909399; font-weight: 400; }
.collapse-arrow { font-size: 12px; color: #909399; }
.collapse-wrap { display: grid; grid-template-rows: 0fr; transition: grid-template-rows 0.35s ease; }
.collapse-wrap.open { grid-template-rows: 1fr; }
.collapse-inner { overflow: hidden; }
.done-text { color: #C0C4CC; text-decoration: line-through; }
</style>
