<template>
  <div class="cd-page">
    <h2 class="page-title"><el-icon><Timer /></el-icon> 倒数日</h2>

    <div class="cd-list" v-loading="loading">
      <div v-for="item in items" :key="item.id" class="cd-row" :class="{ urgent: item.daysLeft <= 3 }">
        <div class="cd-left">{{ item.title }} 还有</div>
        <div class="cd-right">
          <span class="cd-num">{{ item.daysLeft }}</span>
          <span class="cd-unit">天</span>
        </div>
      </div>
      <div v-if="items.length===0 && !loading" class="cd-empty">还没有倒数事项</div>
    </div>

    <div class="cd-add-btn" @click="openImport">＋</div>

    <el-dialog title="选择任务导入" v-model="showImport" width="460px">
      <div v-if="availTasks.length===0" style="text-align:center;color:#c0c4cc;padding:30px">暂无可导入的任务</div>
      <div class="cd-import-list">
        <div v-for="t in availTasks" :key="t.id" class="imp-row" @click="importTask(t)">
          <div class="imp-left">
            <div class="imp-title">{{ t.title }}</div>
            <div class="imp-date">{{ fmtDate(t.deadline) }}</div>
          </div>
          <div class="imp-pri" :class="'pri-'+t.priority">{{ t.priority==='HIGH'?'高':t.priority==='MEDIUM'?'中':'低' }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCountdowns, createCountdown, getAvailableTasks } from '@/api/countdown/index.js'

const items=ref([]),loading=ref(false),showImport=ref(false),availTasks=ref([])

const fmtDate=(d)=>{if(!d)return'';const p=d.split('T')[0];return p}

const load=async()=>{loading.value=true;const r=await getCountdowns();if(r.success)items.value=r.data;loading.value=false}
const openImport=async()=>{const r=await getAvailableTasks();if(r.success)availTasks.value=r.data;showImport.value=true}
const importTask=(t)=>{createCountdown({title:t.title,targetDate:fmtDate(t.deadline)}).then(r=>{if(r.success){showImport.value=false;load()}})}
onMounted(load)
</script>

<style scoped>
.cd-page{padding:10px;position:relative;min-height:200px}
.page-title{display:flex;align-items:center;gap:8px;font-size:27px;font-weight:700;margin:-4px 0 16px}
.cd-list{display:flex;flex-direction:column;gap:14px;margin-top:38px}
.cd-row{display:flex;align-items:stretch;overflow:hidden;border-radius:12px;border:1px solid rgba(226,232,240,0.6);box-shadow:0 3px 12px rgba(0,0,0,0.08),0 1px 3px rgba(0,0,0,0.06);height:64px}
.cd-left{flex:1;display:flex;align-items:center;padding-left:24px;background:#fff;font-size:20px;font-weight:700;color:#1e293b}
.cd-right{display:flex;align-items:stretch;flex-shrink:0}
.cd-num{display:flex;align-items:center;padding:0 14px;font-size:30px;font-weight:800;font-family:Menlo,Monaco,monospace;color:#fff;background:#3aad78;min-width:48px;justify-content:center}
.cd-unit{display:flex;align-items:center;padding:0 16px;font-size:20px;font-weight:800;color:#fff;background:#248a5a}
.urgent .cd-num{background:#fb923c}
.urgent .cd-unit{background:#ea580c}

.cd-add-btn{position:absolute;top:30px;right:12px;width:38px;height:38px;border-radius:10px;display:flex;align-items:center;justify-content:center;cursor:pointer;color:#222;font-size:26px;font-weight:900;transition:all .2s;user-select:none}
.cd-add-btn:hover{background:#f0f0f0}
.cd-add-btn:active{background:#e0e0e0}

.cd-import-list{display:flex;flex-direction:column;gap:6px}
.imp-row{display:flex;align-items:center;justify-content:space-between;padding:14px 16px;border-radius:10px;cursor:pointer;transition:all .15s;border:1px solid #f0f0f0}
.imp-row:hover{background:#f8faf8;border-color:#d0e8d0}
.imp-left{display:flex;flex-direction:column;gap:3px}
.imp-title{font-size:17px;font-weight:700;color:#333}
.imp-date{font-size:13px;color:#999}
.imp-pri{font-size:12px;font-weight:600;padding:3px 10px;border-radius:5px}
.pri-HIGH{color:#991b1b;background:#fef2f2}
.pri-MEDIUM{color:#6b21a8;background:#faf5ff}
.pri-LOW{color:#1e40af;background:#eff6ff}
</style>
