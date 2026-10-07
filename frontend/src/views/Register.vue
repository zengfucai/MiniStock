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
const confirmPassword = ref('')

async function register() {

  if (!username.value || !password.value) {
    alert('请填写完整')
    return
  }

  if (password.value.length < 6) {
    alert('密码至少6位')
    return
  }

  if (
      password.value !==
      confirmPassword.value
  ) {
    alert('两次密码不一致')
    return
  }

  try {

    await api.post(
        '/auth/register',
        {
          username: username.value,
          password: password.value
        }
    )

    alert(
        '注册成功，已获得100000模拟资金'
    )

    router.push('/login')

  } catch (error) {
  }
}

</script>

<template>

  <div class="auth-page">

    <div class="auth-card">

      <h1>注册 MiniStock</h1>

      <input
          v-model="username"
          placeholder="用户名"
      >

      <input
          v-model="password"
          type="password"
          placeholder="密码"
      >

      <input
          v-model="confirmPassword"
          type="password"
          placeholder="确认密码"
      >

      <button @click="register">
        注册
      </button>

      <button
          class="secondary"
          @click="router.push('/login')"
      >
        返回登录
      </button>

    </div>

  </div>

</template>