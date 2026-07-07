<template>
  <div class="home" v-loading="loading">
    <h2>📊 学习概览</h2>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover"><div class="stat-num">{{ stats.total }}</div><div class="stat-label">总任务数</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div class="stat-num" style="color:#67C23A">{{ stats.done }}</div><div class="stat-label">已完成</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div class="stat-num" style="color:#409EFF">{{ stats.rate }}%</div><div class="stat-label">完成率</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div class="stat-num" style="color:#E6A23C">{{ stats.checkDays }}</div><div class="stat-label">打卡天数</div></el-card>
      </el-col>
    </el-row>

    <h3 style="margin-top:30px;">⏰ 即将到期的任务</h3>
    <el-table :data="upcoming" empty-text="暂无即将到期的任务 🎉">
      <el-table-column prop="title" label="任务"></el-table-column>
      <el-table-column prop="deadline" label="截止时间" width="180"></el-table-column>
      <el-table-column prop="priority" label="优先级" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.priority==='HIGH'?'danger':scope.row.priority==='MEDIUM'?'warning':'info'">
            {{ scope.row.priority === 'HIGH' ? '高' : scope.row.priority === 'MEDIUM' ? '中' : '低' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStatistics } from '@/api/study/statistics.js'
import { getUpcoming } from '@/api/study/task.js'

const loading = ref(false)
const stats = ref({ total: 0, done: 0, rate: 0, checkDays: 0 })
const upcoming = ref([])

onMounted(() => {
  loading.value = true
  getStatistics().then(res => { if (res.success) stats.value = res.data })
    .catch(() => {})
    .finally(() => { loading.value = false })

  getUpcoming().then(res => { if (res.success) upcoming.value = res.data })
    .catch(() => {})
})
</script>

<style scoped>
.home { padding: 10px; }
.stat-num { font-size: 36px; font-weight: bold; text-align: center; }
.stat-label { text-align: center; color: #909399; margin-top: 8px; }
</style>
