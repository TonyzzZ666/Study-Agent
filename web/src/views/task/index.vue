<template>
  <div class="task-page">
    <div class="toolbar">
      <el-button type="primary" @click="openDialog()">+ 新建任务</el-button>
      <div class="tab-btns">
        <el-button :type="tab==='todo'?'primary':'default'" @click="tab='todo'">📋 待完成</el-button>
        <el-button :type="tab==='done'?'primary':'default'" @click="tab='done'">✅ 已完成</el-button>
      </div>
      <el-select v-model="filterPriority" placeholder="优先级筛选" clearable style="width:130px;margin-left:10px" @change="loadTasks">
        <el-option label="高" value="HIGH"></el-option>
        <el-option label="中" value="MEDIUM"></el-option>
        <el-option label="低" value="LOW"></el-option>
      </el-select>
    </div>

    <el-table :data="displayTasks" v-loading="loading" style="margin-top:15px" :row-class-name="rowClass">
      <el-table-column width="58" align="center">
        <template #default="s">
          <div :style="{ marginLeft: s.row.parentId ? '22px' : '0' }" class="circle" :class="{ done: s.row.status === 'DONE', closing: s.row._closing }" @click="complete(s.row)">
            <span v-if="s.row.status === 'DONE'" class="checkmark">✓</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="任务" :min-width="140">
        <template #default="s">
          <div class="task-cell" :class="{ closing: s.row._closing }">
            <span class="task-title" :class="{ 'done-text': s.row.status === 'DONE' }" :title="s.row.title" :style="{ marginLeft: s.row.parentId ? '22px' : '0' }">{{ s.row.title }}</span>
            <span v-if="s.row.description" class="desc-toggle" @click="toggleDesc(s.row.id)">
              {{ showDesc.has(s.row.id) ? '▼' : '▶' }} 描述
            </span>
            <span class="spacer"></span>
            <el-button v-if="!s.row.parentId && s.row.status!=='DONE'" size="small" class="subtask-btn" @click="openDialog(null, s.row.id)">+ 子任务</el-button>
          </div>
          <div v-if="showDesc.has(s.row.id)" class="desc-content">{{ s.row.description }}</div>
        </template>
      </el-table-column>
      <el-table-column label="截止时间" width="220">
        <template #default="s">
          <span :style="{ color: isOverdue(s.row) ? '#F56C6C' : '', fontWeight: isOverdue(s.row) ? 'bold' : '' }">
            {{ s.row.deadline }}
            <el-tag v-if="isOverdue(s.row)" type="danger" size="small" style="margin-left:6px">逾时</el-tag>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="90">
        <template #default="s">
          <el-tag :type="s.row.priority==='HIGH'?'danger':s.row.priority==='MEDIUM'?'warning':'info'">{{ s.row.priority }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="s">
          <div class="actions-row">
            <el-button size="small" type="success" v-if="s.row.status!=='DONE'" @click="doCheck(s.row)">
              {{ checkedToday.has(s.row.id) ? '今日已打卡' : '打卡' }}
            </el-button>
            <el-button size="small" @click="openDialog(s.row)">编辑</el-button>
            <el-popconfirm title="确定删除？" @confirm="delTask(s.row.id)"><template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template></el-popconfirm>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新建/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="550px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title"></el-input></el-form-item>
        <el-form-item label="描述" v-if="!subtaskMode"><el-input v-model="form.description" type="textarea"></el-input></el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="form.priority">
            <el-radio value="HIGH">高</el-radio><el-radio value="MEDIUM">中</el-radio><el-radio value="LOW">低</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="form.deadline" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss"></el-date-picker>
        </el-form-item>
        <el-form-item label="复习提醒" v-if="!subtaskMode"><el-switch v-model="form.needReview"></el-switch></el-form-item>
      </el-form>
      <div style="margin-top:10px" v-if="!subtaskMode && !isEdit">
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
import { ref, computed, onMounted } from 'vue'
import { getTaskList, createTask, updateTask, deleteTask } from '@/api/study/task.js'
import { doCheckIn, completeTask, getTodayCheckIns } from '@/api/study/checkin.js'
import { getRecommendations } from '@/api/study/recommendation.js'
import { successMsg, errorMsg } from '@/utils/message.js'

const tasks = ref([]), recs = ref([]), loading = ref(false), checkedToday = ref(new Set()), showDesc = ref(new Set())
const dialogVisible = ref(false), isEdit = ref(false), tab = ref('todo'), subtaskMode = ref(false), parentId = ref(null)
const filterPriority = ref('')
const form = ref({ title: '', description: '', priority: 'MEDIUM', deadline: '', needReview: false, parentId: null })

const dialogTitle = computed(() => {
  if (isEdit.value) return '编辑任务'
  if (subtaskMode.value) return '添加子任务'
  return '新建任务'
})

const priorityWeight = (p) => p === 'HIGH' ? 0 : p === 'MEDIUM' ? 1 : 2
const isOverdue = (task) => task.status === 'TODO' && task.deadline && new Date(task.deadline) < new Date()
const isTodayChecked = (task) => checkedToday.value.has(task.id)

const flatTasks = computed(() => {
  const result = []
  // 先按紧急度排父任务
  const parents = tasks.value.filter(t => !t.parentId).sort((a, b) => {
    const aOver = isOverdue(a) ? 0 : 1
    const bOver = isOverdue(b) ? 0 : 1
    if (aOver !== bOver) return aOver - bOver
    if (priorityWeight(a.priority) !== priorityWeight(b.priority))
      return priorityWeight(a.priority) - priorityWeight(b.priority)
    return new Date(a.deadline) - new Date(b.deadline)
  })
  parents.forEach(p => {
    result.push(p)
    // 子任务紧跟父任务
    const subs = tasks.value.filter(t => t.parentId === p.id).sort((a, b) => {
      const aOver = isOverdue(a) ? 0 : 1
      const bOver = isOverdue(b) ? 0 : 1
      if (aOver !== bOver) return aOver - bOver
      if (priorityWeight(a.priority) !== priorityWeight(b.priority))
        return priorityWeight(a.priority) - priorityWeight(b.priority)
      return new Date(a.deadline) - new Date(b.deadline)
    })
    subs.forEach(s => result.push(s))
  })
  return result
})

const todoTasks = computed(() => flatTasks.value.filter(t => t.status === 'TODO'))
const doneTasks = computed(() => flatTasks.value.filter(t => t.status === 'DONE'))

const displayTasks = computed(() => tab.value === 'todo' ? todoTasks.value : doneTasks.value)

const rowClass = ({ row }) => {
  if (row._closing) return 'closing-row'
  if (isTodayChecked(row)) return 'today-checked-row'
  if (isOverdue(row)) return 'overdue-row'
  return ''
}

const loadTasks = () => {
  loading.value = true
  getTaskList({ priority: filterPriority.value || '' })
    .then(res => { if (res.success) tasks.value = res.data; loading.value = false })
}
const loadRecs = () => { getRecommendations().then(res => { if (res.success) recs.value = res.data }) }
const loadToday = () => {
  getTodayCheckIns().then(res => { if (res.success) checkedToday.value = new Set(res.data) })
}

const openDialog = (row, pId) => {
  subtaskMode.value = !!pId
  parentId.value = pId || null
  if (row) {
    isEdit.value = true
    form.value = { ...row }
  } else {
    isEdit.value = false
    form.value = { title: '', description: '', priority: 'MEDIUM', deadline: '', needReview: false, parentId: pId || null }
  }
  dialogVisible.value = true
}
const toggleDesc = (id) => { showDesc.value.has(id) ? showDesc.value.delete(id) : showDesc.value.add(id) }
const fillRec = (r) => { form.value.title = r.title; form.value.description = r.description; form.value.priority = r.priority }
const saveTask = () => {
  const data = { ...form.value }
  if (subtaskMode.value) data.parentId = parentId.value
  if (isEdit.value) {
    updateTask(data.id, data).then(res => { if (res.success) { successMsg('更新成功'); dialogVisible.value = false; loadTasks() } else errorMsg(res.msg) })
  } else {
    createTask(data).then(res => { if (res.success) { successMsg('创建成功'); dialogVisible.value = false; loadTasks() } else errorMsg(res.msg) })
  }
}
const delTask = (id) => { deleteTask(id).then(res => { if (res.success) { successMsg('删除成功'); loadTasks() } }) }
const doCheck = (row) => {
  doCheckIn(row.id).then(res => {
    if (res.success) { successMsg(res.data); loadToday() } else errorMsg(res.msg)
  })
}
const complete = (row) => {
  if (row._closing) return
  // 父任务联动所有子任务一起动画
  const affected = [row]
  if (!row.parentId) {
    tasks.value.filter(t => t.parentId === row.id).forEach(s => affected.push(s))
  }
  affected.forEach(r => { r._closing = true })
  setTimeout(() => {
    completeTask(row.id).then(res => {
      if (res.success) {
        affected.forEach(r => { r._closing = false })
        successMsg(res.data)
        loadTasks()
      } else {
        affected.forEach(r => { r._closing = false })
        errorMsg(res.msg)
      }
    })
  }, 800)
}

onMounted(() => { loadTasks(); loadRecs(); loadToday() })
</script>

<style scoped>
.task-page { padding: 10px; }
.toolbar { display: flex; align-items: center; gap: 10px; }
.tab-btns { display: flex; gap: 0; margin-left: 10px; }
.tab-btns .el-button { border-radius: 0; }
.tab-btns .el-button:first-child { border-radius: 4px 0 0 4px; }
.tab-btns .el-button:last-child { border-radius: 0 4px 4px 0; }

:deep(.overdue-row) { background-color: #fef0f0 !important; }
:deep(.today-checked-row) { background-color: #f0f9eb !important; }
:deep(.closing-row) { opacity: 0.3; filter: grayscale(1); transition: all 0.8s ease; }
:deep(.el-table__body-wrapper .el-table__row) { transition: transform 0.3s ease, opacity 0.3s ease; }
:deep(.el-table) { font-size: 15px; font-weight: 500; }
:deep(.el-table th) { font-size: 15px; font-weight: 600; }
:deep(.circle-col) { padding-right: 0 !important; }
:deep(.circle-col .cell) { padding-right: 2px !important; }

.circle {
  width: 18px; height: 18px; border-radius: 50%;
  border: 2px solid #C0C4CC; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.3s; margin: 0 auto;
}
.circle:hover { border-color: #909399; }
.circle.done { background-color: #909399; border-color: #909399; }
.circle.closing { background-color: #909399; border-color: #909399; transform: scale(0.8); opacity: 0.5; }
.checkmark { color: #fff; font-size: 11px; font-weight: bold; }
.done-text { color: #C0C4CC; text-decoration: line-through; }

.spacer { flex: 1; }
.subtask-btn { font-size: 11px; padding: 0 6px; height: 22px; margin-left: auto; }
.task-cell { display: flex; align-items: center; gap: 8px; white-space: nowrap; transition: opacity 0.3s; }
.task-cell.closing { opacity: 0.3; }
.task-title { font-weight: 500; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: inline-block; }
.desc-toggle { font-size: 12px; color: #409EFF; cursor: pointer; white-space: nowrap; user-select: none; }
.desc-toggle:hover { color: #337ECC; }
.desc-content { font-size: 13px; font-weight: 400; color: #909399; margin-top: 6px; padding: 6px 10px; background: #fafafa; border-radius: 4px; }

.actions-row { display: flex; gap: 4px; flex-wrap: nowrap; }
</style>
