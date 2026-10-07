<script setup>

import {
  ref
} from 'vue'

import {
  useRouter
} from 'vue-router'

import api from '../api/api'

const router = useRouter()

const username = ref('')
const password = ref('')

async function login() {

  if (!username.value || !password.value) {
    alert('请输入用户名和密码')
    return
  }

  try {

    const result =
        await api.post(
            '/auth/login',
            {
              username: username.value,
              password: password.value
            }
        )

    const user = result.data

    if (user.role === 'ADMIN') {
      router.push('/admin')
    } else {
      router.push('/user')
    }

  } catch (error) {
  }
}

</script>

<template>

  <div class="auth-page">

    <div class="auth-card">

      <h1>MiniStock</h1>

      <p class="subtitle">
        小型模拟股票交易系统
      </p>

      <input
          v-model="username"
          placeholder="用户名"
      >

      <input
          v-model="password"
          type="password"
          placeholder="密码"
          @keyup.enter="login"
      >

      <button @click="login">
        登录
      </button>

      <button
          class="secondary"
          @click="router.push('/register')"
      >
        注册账号
      </button>

      <div class="demo">

        管理员账号：

        <strong>admin</strong>

        <br>

        密码：

        <strong>admin123</strong>

      </div>

    </div>

  </div>

</template>