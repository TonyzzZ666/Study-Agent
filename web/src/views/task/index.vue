<template>
  <div class="task-page">
    <div class="toolbar">
      <el-button type="primary" @click="openDialog()">+ 新建任务</el-button>
      <el-select v-model="filterStatus" placeholder="状态筛选" clearable style="width:130px;margin-left:10px" @change="loadTasks">
        <el-option label="全部" value=""></el-option>
        <el-option label="待完成" value="TODO"></el-option>
        <el-option label="已完成" value="DONE"></el-option>
      </el-select>
      <el-select v-model="filterPriority" placeholder="优先级筛选" clearable style="width:130px;margin-left:10px" @change="loadTasks">
        <el-option label="高" value="HIGH"></el-option>
        <el-option label="中" value="MEDIUM"></el-option>
        <el-option label="低" value="LOW"></el-option>
      </el-select>
    </div>

    <el-table :data="tasks" v-loading="loading" style="margin-top:15px">
      <el-table-column prop="title" label="任务" min-width="180"></el-table-column>
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip></el-table-column>
      <el-table-column prop="deadline" label="截止时间" width="170"></el-table-column>
      <el-table-column prop="priority" label="优先级" width="90">
        <template #default="s">
          <el-tag :type="s.row.priority==='HIGH'?'danger':s.row.priority==='MEDIUM'?'warning':'info'">{{ s.row.priority }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="s">
          <el-tag :type="s.row.status==='DONE'?'success':'info'">{{ s.row.status==='DONE'?'已完成':'待完成' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="s">
          <el-button size="small" type="success" v-if="s.row.status!=='DONE'" @click="doCheck(s.row.id)">打卡</el-button>
          <el-button size="small" @click="openDialog(s.row)">编辑</el-button>
          <el-popconfirm title="确定删除？" @confirm="delTask(s.row.id)"><template #reference>
            <el-button size="small" type="danger">删除</el-button>
          </template></el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新建/编辑弹窗 -->
    <el-dialog :title="isEdit?'编辑任务':'新建任务'" v-model="dialogVisible" width="550px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title"></el-input></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea"></el-input></el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="form.priority">
            <el-radio value="HIGH">高</el-radio><el-radio value="MEDIUM">中</el-radio><el-radio value="LOW">低</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="form.deadline" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss"></el-date-picker>
        </el-form-item>
        <el-form-item label="复习提醒"><el-switch v-model="form.needReview"></el-switch></el-form-item>
      </el-form>
      <!-- 推荐模板 -->
      <div style="margin-top:10px">
        <span style="color:#909399;font-size:13px;">💡 快速模板：</span>
        <el-tag v-for="r in recs" :key="r.id" style="margin:3px;cursor:pointer"
                @click="fillRec(r)" type="info" effect="plain">{{ r.title }}</el-tag>
      </div>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="saveTask">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTaskList, createTask, updateTask, deleteTask } from '@/api/study/task.js'
import { doCheckIn } from '@/api/study/checkin.js'
import { getRecommendations } from '@/api/study/recommendation.js'
import { successMsg, errorMsg } from '@/utils/message.js'

const tasks = ref([]), recs = ref([]), loading = ref(false)
const dialogVisible = ref(false), isEdit = ref(false)
const filterStatus = ref(''), filterPriority = ref('')
const form = ref({ title: '', description: '', priority: 'MEDIUM', deadline: '', needReview: false })

const loadTasks = () => {
  loading.value = true
  getTaskList({ status: filterStatus.value, priority: filterPriority.value })
    .then(res => { if (res.success) tasks.value = res.data; loading.value = false })
}
const loadRecs = () => { getRecommendations().then(res => { if (res.success) recs.value = res.data }) }

const openDialog = (row) => {
  if (row) { isEdit.value = true; form.value = { ...row } }
  else { isEdit.value = false; form.value = { title: '', description: '', priority: 'MEDIUM', deadline: '', needReview: false } }
  dialogVisible.value = true
}
const fillRec = (r) => {
  form.value.title = r.title; form.value.description = r.description; form.value.priority = r.priority
}
const saveTask = () => {
  if (isEdit.value) {
    updateTask(form.value.id, form.value).then(res => { if (res.success) { successMsg('更新成功'); dialogVisible.value = false; loadTasks() } else errorMsg(res.msg) })
  } else {
    createTask(form.value).then(res => { if (res.success) { successMsg('创建成功'); dialogVisible.value = false; loadTasks() } else errorMsg(res.msg) })
  }
}
const delTask = (id) => { deleteTask(id).then(res => { if (res.success) { successMsg('删除成功'); loadTasks() } }) }
const doCheck = (taskId) => {
  doCheckIn(taskId).then(res => { if (res.success) { successMsg('打卡成功！'); loadTasks() } else errorMsg(res.msg) })
}

onMounted(() => { loadTasks(); loadRecs() })
</script>

<style scoped>
.task-page { padding: 10px; }
.toolbar { display: flex; align-items: center; }
</style>
