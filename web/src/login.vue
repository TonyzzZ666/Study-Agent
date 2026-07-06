<template>
  <div class="login">
    <el-form class="form" :model="loginForm" :rules="rules" ref="loginRef">
      <h1>学习养成计划</h1>
      <el-form-item prop="username">
        <el-input type="text" v-model="loginForm.username" placeholder="请输入用户名" @keyup.enter="submitLogin(loginRef)">
          <template #prefix><span>👤</span></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input type="password" v-model="loginForm.password" placeholder="请输入密码" @keyup.enter="submitLogin(loginRef)">
          <template #prefix><span>🔒</span></template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button class="button" type="primary" :loading="isLoading" @click="submitLogin(loginRef)">登  录</el-button>
      </el-form-item>
      <el-form-item>
        <el-button class="button" type="success" @click="goRegister">注  册</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import '@/assets/css/login.css'
import {useStore} from "@/store/index.js";
import routers from "@/router/routers.js";
import {login, register} from "@/api/login/login.js";
import {errorMsg} from "@/utils/message.js";
import {ref, reactive} from 'vue'

const store = useStore()
const loginRef = ref(null)
const isLoading = ref(false)
const isRegister = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = reactive({
  username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }]
})

const submitLogin = (loginRef) => {
  loginRef.validate((valid) => {
    if (valid) {
      isLoading.value = true
      login(loginForm).then(res => {
        if (res.success) {
          store.token = res.data.token
          store.refreshToken = res.data.refreshToken
          store.userInfo = res.data.userDto
          routers.push({path: '/Layout'})
        } else {
          errorMsg(res.msg)
        }
        isLoading.value = false
      })
    }
  })
}

const goRegister = () => {
  if (!loginForm.username || !loginForm.password) {
    errorMsg('请先输入用户名和密码')
    return
  }
  isLoading.value = true
  register(loginForm).then(res => {
    if (res.success) {
      // 注册成功后自动登录
      submitLogin(loginRef)
    } else {
      errorMsg(res.msg)
      isLoading.value = false
    }
  })
}
</script>
