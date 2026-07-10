<template>
  <div class="login">
    <Transition name="form-swap">
      <!-- 登录对话框 -->
      <div v-if="!isSignup" class="form" key="login">
        <div class="logo-wrap">
          <img src="/logo.png" class="logo-img" alt="JADE">
        </div>
        <div class="field-group">
          <label class="field-label">Email</label>
          <div class="input-wrap">
            <el-icon class="prefix-icon"><Message /></el-icon>
            <input type="text" v-model="loginForm.email" placeholder="Enter your email" @keyup.enter="doLogin">
          </div>
        </div>
        <div class="field-group">
          <label class="field-label">Password</label>
          <div class="input-wrap">
            <el-icon class="prefix-icon"><Lock /></el-icon>
            <input :type="showPwd ? 'text' : 'password'" v-model="loginForm.password" placeholder="Enter your password" @keyup.enter="doLogin">
            <el-icon class="suffix-icon" @click="showPwd = !showPwd">
              <View v-if="!showPwd" /><Hide v-else />
            </el-icon>
          </div>
        </div>
        <button class="login-btn" :disabled="isLoading" @click="doLogin">
          {{ isLoading ? 'LOGGING IN...' : 'LOG IN' }}
        </button>
        <div class="footer-links">
          <span class="footer-text">Don't have an account?</span>
          <span class="signup-link" @click="isSignup = true">Sign Up</span>
        </div>
      </div>

      <!-- 注册对话框 -->
      <div v-else class="form signup-form" key="signup">
        <div class="close-btn" @click="isSignup = false">
          <el-icon><Close /></el-icon>
        </div>
        <div class="logo-wrap">
          <img src="/logo.png" class="logo-img" alt="JADE">
        </div>
        <div class="field-group">
          <label class="field-label">Username</label>
          <div class="input-wrap">
            <el-icon class="prefix-icon"><User /></el-icon>
            <input type="text" v-model="regForm.username" placeholder="Choose a username">
          </div>
        </div>
        <div class="field-group">
          <label class="field-label">Email</label>
          <div class="input-wrap">
            <el-icon class="prefix-icon"><Message /></el-icon>
            <input type="text" v-model="regForm.email" placeholder="Enter your email">
          </div>
        </div>
        <div class="field-group">
          <label class="field-label">Password</label>
          <div class="input-wrap">
            <el-icon class="prefix-icon"><Lock /></el-icon>
            <input :type="showPwd ? 'text' : 'password'" v-model="regForm.password" placeholder="Create a password">
            <el-icon class="suffix-icon" @click="showPwd = !showPwd">
              <View v-if="!showPwd" /><Hide v-else />
            </el-icon>
          </div>
        </div>
        <div class="field-group">
          <label class="field-label">Confirm Password</label>
          <div class="input-wrap">
            <el-icon class="prefix-icon"><Lock /></el-icon>
            <input :type="showPwd ? 'text' : 'password'" v-model="regForm.confirmPassword" placeholder="Confirm your password">
          </div>
        </div>
        <button class="login-btn" :disabled="isLoading" @click="doRegister">
          {{ isLoading ? 'CREATING...' : 'SIGN UP' }}
        </button>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import '@/assets/css/login.css'
import {useStore} from "@/store/index.js";
import routers from "@/router/routers.js";
import {login, register} from "@/api/login/login.js";
import {getProfile} from "@/api/login/profile.js";
import {errorMsg} from "@/utils/message.js";
import {ref, reactive} from 'vue'

const store = useStore()
const isLoading = ref(false)
const showPwd = ref(false)
const isSignup = ref(false)

const loginForm = reactive({ email: '', password: '' })
const regForm = reactive({ username: '', email: '', password: '', confirmPassword: '' })

const doLogin = () => {
  if (!loginForm.email || !loginForm.password) { errorMsg('请输入邮箱和密码'); return }
  isLoading.value = true
  login({ username: loginForm.email, password: loginForm.password }).then(res => {
    if (res.success) {
      store.token = res.data.token; store.refreshToken = res.data.refreshToken; store.userInfo = res.data.userDto
      getProfile().then(p => {
        if (p && p.success) store.userInfo = { ...store.userInfo, nickName: p.data.nickName, avatar: p.data.avatar, signature: p.data.signature }
      })
      routers.push({path: '/Layout'})
    } else { errorMsg(res.msg) }
    isLoading.value = false
  })
}

const doRegister = () => {
  if (!regForm.username || !regForm.email || !regForm.password || !regForm.confirmPassword) {
    errorMsg('请填写完整信息'); return
  }
  if (regForm.password !== regForm.confirmPassword) { errorMsg('两次密码不一致'); return }
  isLoading.value = true
  register({ username: regForm.username, email: regForm.email, password: regForm.password }).then(res => {
    if (res.success) {
      login({ username: regForm.email, password: regForm.password }).then(res2 => {
        if (res2.success) {
          store.token = res2.data.token; store.refreshToken = res2.data.refreshToken; store.userInfo = res2.data.userDto
          getProfile().then(p => {
            if (p && p.success) store.userInfo = { ...store.userInfo, nickName: p.data.nickName, avatar: p.data.avatar, signature: p.data.signature }
          })
          routers.push({path: '/Layout'})
        } else { errorMsg(res2.msg) }
        isLoading.value = false
      })
    } else { errorMsg(res.msg); isLoading.value = false }
  })
}
</script>
