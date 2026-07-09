<template>
  <div class="login">
    <!-- 登录对话框 -->
    <Transition name="form-swap">
      <div v-if="!isSignup" class="form" key="login">
        <div class="logo-wrap">
          <img src="/logo.png" class="logo-img" alt="JADE">
        </div>
        <div class="field-group">
          <label class="field-label">Username</label>
          <div class="input-wrap">
            <el-icon class="prefix-icon"><User /></el-icon>
            <input type="text" v-model="loginForm.username" placeholder="Enter your username" @keyup.enter="doLogin">
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
            <input type="text" v-model="loginForm.username" placeholder="Choose a username" @keyup.enter="handleSubmit">
          </div>
        </div>
        <div class="field-group">
          <label class="field-label">Password</label>
          <div class="input-wrap">
            <el-icon class="prefix-icon"><Lock /></el-icon>
            <input :type="showPwd ? 'text' : 'password'" v-model="loginForm.password" placeholder="Create a password" @keyup.enter="handleSubmit">
            <el-icon class="suffix-icon" @click="showPwd = !showPwd">
              <View v-if="!showPwd" /><Hide v-else />
            </el-icon>
          </div>
        </div>
        <button class="login-btn" :disabled="isLoading" @click="handleSubmit">
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

const loginForm = reactive({
  username: '',
  password: ''
})

const doLogin = () => {
  if (!loginForm.username || !loginForm.password) {
    errorMsg('请输入用户名和密码')
    return
  }
  isLoading.value = true
  login(loginForm).then(res => {
    if (res.success) {
      store.token = res.data.token
      store.refreshToken = res.data.refreshToken
      store.userInfo = res.data.userDto
      getProfile().then(p => {
        if (p && p.success) store.userInfo = { ...store.userInfo, nickName: p.data.nickName, avatar: p.data.avatar, signature: p.data.signature }
      })
      routers.push({path: '/Layout'})
    } else {
      errorMsg(res.msg)
    }
    isLoading.value = false
  })
}

const doRegister = () => {
  if (!loginForm.username || !loginForm.password) {
    errorMsg('请输入用户名和密码')
    return
  }
  isLoading.value = true
  register(loginForm).then(res => {
    if (res.success) {
      login(loginForm).then(res2 => {
        if (res2.success) {
          store.token = res2.data.token
          store.refreshToken = res2.data.refreshToken
          store.userInfo = res2.data.userDto
          getProfile().then(p => {
            if (p && p.success) store.userInfo = { ...store.userInfo, nickName: p.data.nickName, avatar: p.data.avatar, signature: p.data.signature }
          })
          routers.push({path: '/Layout'})
        } else {
          errorMsg(res2.msg)
        }
        isLoading.value = false
      })
    } else {
      errorMsg(res.msg)
      isLoading.value = false
    }
  })
}
</script>
