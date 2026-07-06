<template>
  <div class="checkin-page">
    <h2>📅 打卡记录</h2>
    <el-table :data="history" v-loading="loading" style="margin-top:15px">
      <el-table-column prop="taskId" label="任务ID" width="80"></el-table-column>
      <el-table-column prop="checkTime" label="打卡时间" width="180"></el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCheckInHistory } from '@/api/study/checkin.js'

const history = ref([]), loading = ref(false)

onMounted(() => {
  loading.value = true
  getCheckInHistory().then(res => { if (res.success) history.value = res.data; loading.value = false })
})
</script>
