<template>
  <div class="task-page" @dblclick="onPageDblClick">
    <h2 class="page-title"><el-icon><List /></el-icon> 任务管理</h2>
    <div class="toolbar">
      <div class="tab-btns">
        <el-button :type="tab==='todo'?'primary':'default'" @click="tab='todo'">待完成</el-button>
        <el-button :type="tab==='done'?'primary':'default'" @click="tab='done'">已完成</el-button>
      </div>
      <div class="toolbar-right">
        <el-select v-model="filterPriority" placeholder="优先级筛选" clearable style="width:130px" @change="loadTasks">
          <el-option label="高" value="HIGH"></el-option>
          <el-option label="中" value="MEDIUM"></el-option>
          <el-option label="低" value="LOW"></el-option>
        </el-select>
        <el-button type="primary" @click="openDialog()">+ 新建任务</el-button>
      </div>
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
          </div>
          <div class="desc-collapse" :class="{ open: showDesc.has(s.row.id) }">
            <div class="desc-inner">
              <div class="desc-content" v-if="s.row.description">{{ s.row.description }}</div>
            </div>
          </div>
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
            <el-button size="small" type="success" v-if="s.row.status!=='DONE' && (s.row.checkinType || 'DAILY') !== 'NONE'" @click="doCheck(s.row)">
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
        <el-form-item label="打卡频率">
          <el-select v-model="form.checkinType" style="width:100%" class="big-select">
            <el-option label="每日" value="DAILY"></el-option>
            <el-option label="每工作日" value="WEEKDAYS"></el-option>
            <el-option label="无需打卡" value="NONE"></el-option>
            <el-option label="个性化" value="CUSTOM"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="选择日期" v-if="form.checkinType === 'CUSTOM'">
          <el-checkbox-group v-model="customDays">
            <el-checkbox v-for="(n,i) in ['一','二','三','四','五','六','日']" :key="i" :value="String(i+1)" :label="n" style="margin-right:8px"></el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="复习提醒" v-if="!subtaskMode"><el-switch v-model="form.needReview"></el-switch></el-form-item>
      </el-form>
      <div style="margin-top:10px" v-if="!subtaskMode && !isEdit">
        <span style="color:#909399;font-size:13px;"><el-icon><Star /></el-icon> 快速模板：</span>
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
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { getTaskList, createTask, updateTask, deleteTask } from '@/api/study/task.js'
import { doCheckIn, completeTask, getTodayCheckIns } from '@/api/study/checkin.js'
import { getRecommendations } from '@/api/study/recommendation.js'
import { successMsg, errorMsg } from '@/utils/message.js'

const route = useRoute()

const tasks = ref([]), recs = ref([]), loading = ref(false), checkedToday = ref(new Set()), showDesc = ref(new Set())
const dialogVisible = ref(false), isEdit = ref(false), tab = ref('todo'), subtaskMode = ref(false), parentId = ref(null)
const filterPriority = ref('')
const form = ref({ title: '', description: '', priority: 'MEDIUM', deadline: '', needReview: false, parentId: null, checkinType: 'DAILY', checkinDays: '' })
const customDays = ref([])

const dialogTitle = computed(() => {
  if (isEdit.value) return '编辑任务'
  if (subtaskMode.value) return '添加子任务'
  return '新建任务'
})

const priorityWeight = (p) => p === 'HIGH' ? 0 : p === 'MEDIUM' ? 1 : 2
const isOverdue = (task) => task.status === 'TODO' && task.deadline && new Date(task.deadline) < new Date()
const isTodayChecked = (task) => checkedToday.value.has(task.id)

// 判断今天是否需要打卡
const needsCheckinToday = (task) => {
  if (task.status === 'DONE') return false
  const type = task.checkinType || 'DAILY'
  if (type === 'DAILY') return true
  if (type === 'NONE') return false
  const dow = new Date().getDay() || 7 // 1=Mon, 7=Sun
  if (type === 'WEEKDAYS') return dow <= 5
  if (type === 'CUSTOM' && task.checkinDays) {
    return task.checkinDays.split(',').includes(String(dow))
  }
  return false
}

// 父任务如果没有设置打卡，取所有子任务的交集
const getEffectiveCheckin = (task) => {
  if (task.checkinType && task.checkinType !== 'DAILY') return task.checkinType
  if (!task.checkinType || task.checkinType === 'DAILY') {
    const subs = tasks.value.filter(t => t.parentId === task.id && t.status === 'TODO')
    if (subs.length === 0) return task.checkinType || 'DAILY'
    // 取交集：只有所有子任务都需要打卡的日期才打卡
    const allDays = subs.map(s => {
      const t = s.checkinType || 'DAILY'
      if (t === 'DAILY') return [1,2,3,4,5,6,7]
      if (t === 'NONE') return []
      if (t === 'WEEKDAYS') return [1,2,3,4,5]
      if (t === 'CUSTOM') return (s.checkinDays || '').split(',').map(Number).filter(Boolean)
      return []
    })
    const intersect = allDays.reduce((a, b) => a.filter(d => b.includes(d)))
    const dow = new Date().getDay() || 7
    return intersect.includes(dow) ? 'INTERSECT_YES' : 'INTERSECT_NO'
  }
  return task.checkinType
}

const flatTasks = computed(() => {
  const result = []
  // 先按紧急度排父任务
  const checkinPrio = (t) => {
    const type = t.checkinType || 'DAILY'
    if (type === 'NONE') return 2
    if (needsCheckinToday(t)) return 0
    return 1
  }
  const parents = tasks.value.filter(t => !t.parentId).sort((a, b) => {
    const aP = checkinPrio(a), bP = checkinPrio(b)
    if (aP !== bP) return aP - bP
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
const doneTasks = computed(() => flatTasks.value.filter(t => t.status === 'DONE').sort((a, b) => {
  return new Date(b.updateTime || b.createdAt) - new Date(a.updateTime || a.createdAt)
}))

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
    form.value = { ...row, checkinType: row.checkinType || 'DAILY', checkinDays: row.checkinDays || '' }
    customDays.value = (row.checkinDays || '').split(',').filter(Boolean)
  } else {
    isEdit.value = false
    form.value = { title: '', description: '', priority: 'MEDIUM', deadline: '', needReview: false, parentId: pId || null, checkinType: 'DAILY', checkinDays: '' }
    customDays.value = []
  }
  dialogVisible.value = true
}
const toggleDesc = (id) => { showDesc.value.has(id) ? showDesc.value.delete(id) : showDesc.value.add(id) }
const fillRec = (r) => { form.value.title = r.title; form.value.description = r.description; form.value.priority = r.priority }
const saveTask = () => {
  const data = { ...form.value }
  if (data.checkinType === 'CUSTOM') data.checkinDays = customDays.value.join(',')
  if (subtaskMode.value) data.parentId = parentId.value
  if (isEdit.value) {
    updateTask(data.id, data).then(res => { if (res.success) { successMsg('更新成功'); dialogVisible.value = false; loadTasks() } else errorMsg(res.msg) })
  } else {
    createTask(data).then(res => { if (res.success) { successMsg('创建成功'); dialogVisible.value = false; loadTasks() } else errorMsg(res.msg) })
  }
}
const delTask = (id) => { deleteTask(id).then(res => { if (res.success) { successMsg('删除成功'); loadTasks() } }) }
const onPageDblClick = (e) => {
  if (tab.value !== 'todo') return
  // 检查是否双击了父任务行（非子任务）
  const row = e.target.closest('.el-table__row')
  if (row) {
    const index = row.rowIndex || Array.from(row.parentNode.children).indexOf(row)
    const task = displayTasks.value[index]
    if (task && !task.parentId) { openDialog(null, task.id); return }
  }
  openDialog()
}

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

onMounted(async () => {
  loadTasks(); loadRecs(); loadToday()
  // 处理首页跳转参数
  if (route.query.tab === 'done') tab.value = 'done'
  if (route.query.tab === 'checkin') tab.value = 'checkin'
  if (route.query.highlight) {
    await nextTick()
    setTimeout(() => {
      const rows = document.querySelectorAll('.el-table__row')
      const hId = parseInt(route.query.highlight)
      const idx = displayTasks.value.findIndex(t => t.id === hId)
      if (idx >= 0 && rows[idx]) {
        rows[idx].scrollIntoView({ behavior: 'smooth', block: 'center' })
        rows[idx].style.background = '#ecf5ff'
        setTimeout(() => { rows[idx].style.background = '' }, 2000)
      }
    }, 300)
  }
})
</script>

<style scoped>
.task-page { padding: 10px; }
.page-title { display: flex; align-items: center; gap: 8px; font-size: 20px; font-weight: 700; margin: 0 0 12px; }
.toolbar { display: flex; align-items: center; gap: 10px; }
.toolbar-right { display: flex; align-items: center; gap: 10px; margin-left: auto; }
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
:deep(.el-table td) { vertical-align: top !important; }
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

.task-cell { display: flex; align-items: center; gap: 8px; white-space: nowrap; transition: opacity 0.3s; padding: 2px 0; }
.task-cell.closing { opacity: 0.3; }
.task-title { font-weight: 500; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: inline-block; }
.desc-toggle { font-size: 12px; color: #409EFF; cursor: pointer; white-space: nowrap; user-select: none; }
.desc-toggle:hover { color: #337ECC; }

.actions-row { display: flex; gap: 4px; flex-wrap: nowrap; }
.desc-collapse { display: grid; grid-template-rows: 0fr; transition: grid-template-rows 0.25s ease; }
.desc-collapse.open { grid-template-rows: 1fr; }
.desc-inner { overflow: hidden; }
.desc-content { font-size: 13px; font-weight: 400; color: #909399; padding: 6px 10px; background: #fafafa; border-radius: 4px; }
.big-select :deep(.el-input__wrapper) { height: 40px; }
</style>
