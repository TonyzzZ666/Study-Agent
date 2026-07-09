<template>
  <div class="profile-page">
    <div class="profile-header">
      <div class="header-left">
        <div class="big-avatar" :style="avatarStyle" @click="triggerUpload">
          <span v-if="!avatarPreview && !store.userInfo?.avatar" class="avatar-letter">{{ avatarLetter }}</span>
          <input type="file" ref="fileInput" accept="image/*" style="display:none" @change="onFileChange">
        </div>
        <div class="header-info">
          <span class="header-name clickable" @click="focusField('nickRef')" :title="username">{{ username }}</span>
          <span class="header-sig clickable" @click="focusField('sigRef')" :title="form.signature">{{ form.signature || '写句座右铭鼓励一下自己吧！' }}</span>
        </div>
      </div>
      <el-button circle class="close-btn" @click="goBack">✕</el-button>
    </div>

    <div class="tabs">
      <span :class="{ active: tab==='info' }" @click="tab='info'"><el-icon><User /></el-icon>个人信息</span>
      <span :class="{ active: tab==='pwd' }" @click="tab='pwd'"><el-icon><Lock /></el-icon>修改密码</span>
      <span :class="{ active: tab==='logout' }" @click="tab='logout'"><el-icon><SwitchButton /></el-icon>退出登录</span>
    </div>

    <div v-if="tab==='info'" class="tab-content">
      <div class="field">
        <div class="field-label">用户名</div>
        <el-input ref="nickRef" v-model="form.nickName" placeholder="输入用户名" class="round-input"></el-input>
      </div>
      <div class="field">
        <div class="field-label">个性签名</div>
        <el-input ref="sigRef" v-model="form.signature" placeholder="写一句个性签名" class="round-input"></el-input>
      </div>
      <div class="field">
        <div class="field-label">邮箱</div>
        <el-input v-model="form.email" placeholder="输入邮箱地址" class="round-input"></el-input>
      </div>
      <div class="field">
        <div class="field-label">手机号</div>
        <el-input v-model="form.phone" placeholder="输入手机号" class="round-input"></el-input>
      </div>
      <el-button type="primary" class="save-btn" @click="saveProfile">保存</el-button>
    </div>

    <div v-if="tab==='pwd'" class="tab-content">
      <div class="field">
        <div class="field-label">旧密码</div>
        <el-input v-model="pwdForm.oldPassword" type="password" placeholder="输入旧密码" class="round-input"></el-input>
      </div>
      <div class="field">
        <div class="field-label">新密码</div>
        <el-input v-model="pwdForm.newPassword" type="password" placeholder="输入新密码" class="round-input"></el-input>
      </div>
      <div class="field">
        <div class="field-label">确认新密码</div>
        <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="再次输入新密码" class="round-input"></el-input>
      </div>
      <el-button type="primary" class="save-btn" @click="changePwd">修改密码</el-button>
    </div>

    <div v-if="tab==='logout'" class="tab-content logout-tab">
      <p style="color:#909399;font-size:16px;margin-bottom:20px;">确定要退出当前账号吗？</p>
      <el-button type="danger" @click="doLogout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from '@/store/index.js'
import { getProfile, updateProfile, changePassword } from '@/api/login/profile.js'
import { successMsg, errorMsg } from '@/utils/message.js'
import routers from '@/router/routers.js'

const store = useStore()
const tab = ref('info')
const fileInput = ref(null)
const avatarPreview = ref(null)
const nickRef = ref(null)
const sigRef = ref(null)
const form = ref({ nickName: '', signature: '', email: '', phone: '' })
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

const username = computed(() => store.userInfo?.nickName || store.userInfo?.username || '用户')
const avatarLetter = computed(() => username.value.charAt(0).toUpperCase())
const avatarStyle = computed(() => {
  const src = avatarPreview.value || store.userInfo?.avatar
  if (src) return { backgroundImage: `url(${src})`, backgroundSize: 'cover' }
  return {}
})

onMounted(async () => {
  const res = await getProfile()
  if (res && res.success) {
    const u = res.data
    form.value = { nickName: u.nickName || u.username || '', signature: u.signature || '', email: u.email || '', phone: u.phone || '' }
    store.userInfo = { ...store.userInfo, nickName: u.nickName || '', avatar: u.avatar || '' }
  }
})

const goBack = () => {
  store.activeIndex = '首页'
  routers.push('/home')
}
const focusField = (refName) => {
  tab.value = 'info'
  const el = refName === 'nickRef' ? nickRef.value : sigRef.value
  if (el) {
    setTimeout(() => {
      const input = el.$el?.querySelector('input') || el.$el?.querySelector('textarea')
      if (input) input.focus()
    }, 100)
  }
}
const triggerUpload = () => fileInput.value?.click()
const onFileChange = (e) => {
  const file = e.target.files[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (ev) => {
    avatarPreview.value = ev.target.result
    updateProfile({ avatar: ev.target.result }).then(res => {
      if (res && res.success) {
        store.userInfo = { ...store.userInfo, avatar: ev.target.result, nickName: form.value.nickName }
        successMsg('头像已更新')
      }
    })
  }
  reader.readAsDataURL(file)
}

const saveProfile = () => {
  updateProfile(form.value).then(res => {
    if (res && res.success) { store.userInfo = { ...store.userInfo, nickName: form.value.nickName, signature: form.value.signature }; successMsg('保存成功') }
    else errorMsg(res?.msg || '保存失败')
  })
}
const changePwd = () => {
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) { errorMsg('请填写完整'); return }
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) { errorMsg('两次输入的密码不一致'); return }
  changePassword(pwdForm.value).then(res => {
    if (res && res.success) { successMsg('密码修改成功'); pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' } }
    else errorMsg(res?.msg || '修改失败')
  })
}
const doLogout = () => {
  store.token = null
  store.refreshToken = null
  store.userInfo = null
  routers.push('/login')
}
</script>

<style scoped>
.profile-page { max-width: 600px; margin: 0 auto; padding: 30px 20px; }

.profile-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 30px; }
.header-left { display: flex; align-items: flex-start; gap: 18px; }
.big-avatar {
  width: 96px; height: 96px; border-radius: 50%; flex-shrink: 0;
  background: #3aad78; color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 36px; font-weight: bold; cursor: pointer; overflow: hidden;
}
.big-avatar:hover { opacity: 0.8; }
.avatar-letter { user-select: none; }
.header-info { display: flex; flex-direction: column; padding-top: 10px; }
.header-name { font-size: 28px; font-weight: 700; line-height: 1.3; }
.header-sig { font-size: 14px; color: #C0C4CC; margin-top: 4px; }
.clickable { cursor: pointer; }
.clickable:hover { opacity: 0.7; }
.close-btn { border: none; font-size: 20px; color: #909399; flex-shrink: 0; }

.tabs { display: flex; gap: 28px; border-bottom: 1px solid #e8e8e8; padding-bottom: 12px; margin-bottom: 24px; }
.tabs span { font-size: 16px; color: #909399; cursor: pointer; padding-bottom: 12px; border-bottom: 2px solid transparent; margin-bottom: -13px; display: inline-flex; align-items: center; gap: 5px; }
.tabs span:hover { color: #3aad78; }
.tabs span.active { color: #3aad78; border-bottom-color: #3aad78; font-weight: 600; }

.tab-content { padding: 10px 0; }
.field { margin-bottom: 24px; }
.field-label { font-size: 16px; font-weight: 700; color: #303133; margin-bottom: 10px; }

.round-input :deep(.el-input__wrapper) {
  border-radius: 12px !important; box-shadow: none !important;
  border: 1px solid #e0e0e0; padding: 8px 14px;
}
.round-input :deep(.el-input__wrapper:hover) { border-color: #3aad78; }
.round-input :deep(.el-input__wrapper.is-focus) { border-color: #3aad78; box-shadow: 0 0 0 1px #3aad78 !important; }
.round-input :deep(.el-input__inner) { font-size: 16px; height: 24px; }

.save-btn { margin-top: 10px; border-radius: 10px; padding: 12px 40px; font-size: 16px; }
.logout-tab { text-align: center; padding-top: 20px; }
</style>
