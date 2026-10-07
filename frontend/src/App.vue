<script setup>

import {
  onMounted,
  onBeforeUnmount
} from 'vue'

import {
  useRoute,
  useRouter
} from 'vue-router'

import api from './api/api'

const router = useRouter()
const route = useRoute()

let timer = null
let checking = false

async function checkLoginStatus() {

  if (
      route.path === '/login' ||
      route.path === '/register'
  ) {
    return
  }

  if (checking) {
    return
  }

  checking = true

  try {

    const result =
        await api.get('/auth/me')

    if (
        !result ||
        result.success === false
    ) {
      return
    }

  } catch (error) {

    if (
        error?.message === '账号已被禁用'
    ) {

      await router.push('/login')

      return
    }

    if (
        error?.message === '账号不存在'
    ) {

      await router.push('/login')
    }

  } finally {

    checking = false
  }
}

onMounted(() => {

  timer =
      setInterval(
          checkLoginStatus,
          3000
      )

})

onBeforeUnmount(() => {

  if (timer) {
    clearInterval(timer)
    timer = null
  }

})

</script>

<template>

  <router-view />

</template>