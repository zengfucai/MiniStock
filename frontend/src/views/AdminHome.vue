<script setup>

import {
  ref,
  onMounted,
  watch
} from 'vue'

import {
  useRouter
} from 'vue-router'

import api from '../api/api'

const router = useRouter()

const active = ref('users')

const users = ref([])
const accounts = ref([])
const stocks = ref([])
const applications = ref([])
const trades = ref([])
const fundRecords = ref([])

const stockForm = ref({
  id: null,
  code: '',
  name: '',
  price: '',
  changeRate: 0,
  status: 'NORMAL'
})

const editingField = ref(null)

watch(
    () => stockForm.value.price,
    () => {

      if (editingField.value !== 'price') {
        return
      }

      const price =
          Number(stockForm.value.price)

      if (!price || price <= 0) {
        return
      }

      if (!stockForm.value.id) {
        stockForm.value.changeRate =
            Number(
                stockForm.value.changeRate || 0
            )
      }
    }
)

function updatePrice() {

  editingField.value = 'price'

  const price =
      Number(stockForm.value.price)

  if (!price || price <= 0) {
    return
  }

  if (!stockForm.value.id) {
    return
  }

  const stock =
      stocks.value.find(
          item =>
              item.id === stockForm.value.id
      )

  if (!stock) {
    return
  }

  const basePrice =
      Number(
          stock.basePrice ||
          stock.price
      )

  if (!basePrice || basePrice <= 0) {
    return
  }

  const changeRate =
      (
          price / basePrice - 1
      ) * 100

  stockForm.value.changeRate =
      Number(
          changeRate.toFixed(2)
      )
}

function updateChangeRate() {

  editingField.value = 'changeRate'

  const changeRate =
      Number(stockForm.value.changeRate)

  if (
      Number.isNaN(changeRate)
  ) {
    return
  }

  if (!stockForm.value.id) {
    return
  }

  const stock =
      stocks.value.find(
          item =>
              item.id === stockForm.value.id
      )

  if (!stock) {
    return
  }

  const basePrice =
      Number(
          stock.basePrice ||
          stock.price
      )

  if (!basePrice || basePrice <= 0) {
    return
  }

  const price =
      basePrice *
      (
          1 +
          changeRate / 100
      )

  stockForm.value.price =
      Number(
          price.toFixed(2)
      )
}

async function load() {

  const [
    userResult,
    accountResult,
    stockResult,
    applicationResult,
    tradeResult,
    fundResult
  ] = await Promise.all([

    api.get('/admin/users'),

    api.get('/admin/accounts'),

    api.get('/stocks'),

    api.get('/trades/applications'),

    api.get('/trades/records'),

    api.get('/admin/fund-records')

  ])

  users.value =
      Array.isArray(userResult?.data)
          ? userResult.data
          : []

  accounts.value =
      Array.isArray(accountResult?.data)
          ? accountResult.data
          : []

  stocks.value =
      Array.isArray(stockResult?.data)
          ? stockResult.data
          : []

  applications.value =
      Array.isArray(applicationResult?.data)
          ? applicationResult.data
          : []

  trades.value =
      Array.isArray(tradeResult?.data)
          ? tradeResult.data
          : []

  fundRecords.value =
      Array.isArray(fundResult?.data)
          ? fundResult.data
          : []
}

function money(value) {

  return `¥${Number(value || 0).toLocaleString(
      'zh-CN',
      {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      }
  )}`
}

function getBalance(userId) {

  const account =
      accounts.value.find(
          item =>
              item.userId === userId
      )

  return money(account?.balance)
}

async function updateStatus(
    user,
    status
) {

  await api.post(
      `/admin/users/${user.id}/status`,
      {
        status
      }
  )

  await load()
}

async function adjustMoney(user) {

  if (!user) {
    alert('找不到对应用户')
    return
  }

  const value =
      prompt(
          `请输入 ${user.username} 的资金调整金额\n正数增加，负数扣除`
      )

  if (value === null) {
    return
  }

  const amount =
      Number(value)

  if (
      !amount ||
      Number.isNaN(amount)
  ) {
    alert('请输入正确金额')
    return
  }

  const description =
      prompt('请输入资金调整原因')

  await api.post(
      `/admin/users/${user.id}/balance`,
      {
        amount: String(amount),
        description:
            description ||
            '管理员资金调整'
      }
  )

  await load()
}

function editStock(stock) {

  editingField.value = null

  stockForm.value = {
    id: stock.id,
    code: stock.code,
    name: stock.name,
    price: stock.price,
    changeRate: stock.changeRate,
    status: stock.status
  }

  active.value = 'stocks'
}

function newStock() {

  editingField.value = null

  stockForm.value = {
    id: null,
    code: '',
    name: '',
    price: '',
    changeRate: 0,
    status: 'NORMAL'
  }
}

async function saveStock() {

  if (
      !stockForm.value.code ||
      !stockForm.value.name ||
      !stockForm.value.price
  ) {

    alert('请填写完整')

    return
  }

  if (
      Number(stockForm.value.price) <= 0
  ) {

    alert('价格必须大于0')

    return
  }

  await api.post(
      '/stocks',
      {
        ...stockForm.value,
        price:
            Number(stockForm.value.price),
        changeRate:
            Number(
                stockForm.value.changeRate || 0
            )
      }
  )

  alert('保存成功')

  newStock()

  await load()
}

async function deleteStock(id) {

  if (!confirm('确定删除这只股票吗？')) {
    return
  }

  await api.delete(
      `/stocks/${id}`
  )

  await load()
}

async function approve(id) {

  if (!confirm('确定通过这个交易申请吗？')) {
    return
  }

  await api.post(
      `/trades/approve/${id}`
  )

  alert('交易已经通过')

  await load()
}

async function reject(id) {

  const reason =
      prompt('请输入拒绝原因')

  await api.post(
      `/trades/reject/${id}`,
      {
        reason:
            reason ||
            '管理员拒绝'
      }
  )

  await load()
}

async function logout() {

  await api.post('/auth/logout')

  router.push('/login')
}

onMounted(load)

</script>

<template>

  <div class="app-page">

    <header class="topbar">

      <div>

        <strong>MiniStock</strong>

        <span class="role admin-role">
          管理员后台
        </span>

      </div>

      <div>

        admin

        <button
            class="small-button"
            @click="logout"
        >
          退出
        </button>

      </div>

    </header>

    <div class="dashboard">

      <aside class="sidebar">

        <button
            :class="{ active: active === 'users' }"
            @click="active = 'users'"
        >
          用户管理
        </button>

        <button
            :class="{ active: active === 'accounts' }"
            @click="active = 'accounts'"
        >
          资金管理
        </button>

        <button
            :class="{ active: active === 'stocks' }"
            @click="active = 'stocks'"
        >
          股票管理
        </button>

        <button
            :class="{ active: active === 'applications' }"
            @click="active = 'applications'"
        >
          交易审核
        </button>

        <button
            :class="{ active: active === 'trades' }"
            @click="active = 'trades'"
        >
          交易记录
        </button>

        <button
            :class="{ active: active === 'fund' }"
            @click="active = 'fund'"
        >
          资金流水
        </button>

      </aside>

      <main class="content">

        <section v-if="active === 'users'">

          <h2>用户管理</h2>

          <table>

            <thead>

            <tr>
              <th>ID</th>
              <th>用户名</th>
              <th>角色</th>
              <th>状态</th>
              <th>余额</th>
              <th>操作</th>
            </tr>

            </thead>

            <tbody>

            <tr
                v-for="user in users"
                :key="user.id"
            >

              <td>{{ user.id }}</td>

              <td>{{ user.username }}</td>

              <td>{{ user.role }}</td>

              <td>{{ user.status }}</td>

              <td>{{ getBalance(user.id) }}</td>

              <td>

                <button
                    v-if="
                      user.role === 'USER' &&
                      user.status === 'NORMAL'
                    "
                    class="danger"
                    @click="
                      updateStatus(
                        user,
                        'DISABLED'
                      )
                    "
                >
                  禁用
                </button>

                <button
                    v-if="
                      user.role === 'USER' &&
                      user.status === 'DISABLED'
                    "
                    @click="
                      updateStatus(
                        user,
                        'NORMAL'
                      )
                    "
                >
                  启用
                </button>

                <button
                    v-if="user.role === 'USER'"
                    @click="
                      adjustMoney(user)
                    "
                >
                  调整资金
                </button>

              </td>

            </tr>

            </tbody>

          </table>

        </section>

        <section v-if="active === 'accounts'">

          <h2>资金管理</h2>

          <table>

            <thead>

            <tr>
              <th>用户ID</th>
              <th>余额</th>
              <th>操作</th>
            </tr>

            </thead>

            <tbody>

            <tr
                v-for="account in accounts"
                :key="account.id"
            >

              <td>{{ account.userId }}</td>

              <td>{{ money(account.balance) }}</td>

              <td>

                <button
                    @click="
                      adjustMoney(
                        users.find(
                          user =>
                              user.id ===
                              account.userId
                        )
                      )
                    "
                >
                  调整资金
                </button>

              </td>

            </tr>

            </tbody>

          </table>

        </section>

        <section v-if="active === 'stocks'">

          <h2>股票管理</h2>

          <div class="form-panel">

            <input
                v-model="stockForm.code"
                placeholder="股票代码"
            >

            <input
                v-model="stockForm.name"
                placeholder="股票名称"
            >

            <input
                v-model.number="stockForm.price"
                type="number"
                step="0.01"
                placeholder="价格"
                @input="updatePrice"
            >

            <input
                v-model.number="stockForm.changeRate"
                type="number"
                step="0.01"
                placeholder="涨跌幅"
                @input="updateChangeRate"
            >

            <select
                v-model="stockForm.status"
            >

              <option value="NORMAL">
                正常
              </option>

              <option value="SUSPENDED">
                停牌
              </option>

            </select>

            <button
                @click="saveStock"
            >
              保存股票
            </button>

            <button
                class="secondary"
                @click="newStock"
            >
              新建
            </button>

          </div>

          <table>

            <thead>

            <tr>
              <th>代码</th>
              <th>名称</th>
              <th>价格</th>
              <th>涨跌幅</th>
              <th>状态</th>
              <th>操作</th>
            </tr>

            </thead>

            <tbody>

            <tr
                v-for="stock in stocks"
                :key="stock.id"
            >

              <td>{{ stock.code }}</td>

              <td>{{ stock.name }}</td>

              <td>{{ money(stock.price) }}</td>

              <td>{{ stock.changeRate }}%</td>

              <td>{{ stock.status }}</td>

              <td>

                <button
                    @click="editStock(stock)"
                >
                  编辑
                </button>

                <button
                    class="danger"
                    @click="
                      deleteStock(stock.id)
                    "
                >
                  删除
                </button>

              </td>

            </tr>

            </tbody>

          </table>

        </section>

        <section
            v-if="
              active === 'applications'
            "
        >

          <h2>交易审核</h2>

          <table>

            <thead>

            <tr>
              <th>用户</th>
              <th>股票</th>
              <th>类型</th>
              <th>数量</th>
              <th>价格</th>
              <th>金额</th>
              <th>状态</th>
              <th>操作</th>
            </tr>

            </thead>

            <tbody>

            <tr
                v-for="item in applications"
                :key="item.id"
            >

              <td>{{ item.username }}</td>

              <td>{{ item.stockName }}</td>

              <td>
                {{
                  item.type === 'BUY'
                      ? '买入'
                      : '卖出'
                }}
              </td>

              <td>{{ item.quantity }}</td>

              <td>{{ money(item.price) }}</td>

              <td>{{ money(item.amount) }}</td>

              <td>
                {{
                  item.status === 'PENDING'
                      ? '待审核'
                      : item.status === 'APPROVED'
                          ? '已通过'
                          : '已拒绝'
                }}
              </td>

              <td>

                <template
                    v-if="
                      item.status ===
                      'PENDING'
                    "
                >

                  <button
                      @click="
                        approve(item.id)
                      "
                  >
                    通过
                  </button>

                  <button
                      class="danger"
                      @click="
                        reject(item.id)
                      "
                  >
                    拒绝
                  </button>

                </template>

              </td>

            </tr>

            </tbody>

          </table>

        </section>

        <section
            v-if="active === 'trades'"
        >

          <h2>交易记录</h2>

          <table>

            <thead>

            <tr>
              <th>用户</th>
              <th>股票</th>
              <th>类型</th>
              <th>数量</th>
              <th>价格</th>
              <th>金额</th>
              <th>时间</th>
            </tr>

            </thead>

            <tbody>

            <tr
                v-for="item in trades"
                :key="item.id"
            >

              <td>{{ item.username }}</td>

              <td>{{ item.stockName }}</td>

              <td>
                {{
                  item.type === 'BUY'
                      ? '买入'
                      : '卖出'
                }}
              </td>

              <td>{{ item.quantity }}</td>

              <td>{{ money(item.price) }}</td>

              <td>{{ money(item.amount) }}</td>

              <td>{{ item.createdAt }}</td>

            </tr>

            </tbody>

          </table>

        </section>

        <section
            v-if="active === 'fund'"
        >

          <h2>资金流水</h2>

          <table>

            <thead>

            <tr>

              <th>用户</th>
              <th>类型</th>
              <th>金额</th>
              <th>余额</th>
              <th>说明</th>
              <th>时间</th>

            </tr>

            </thead>

            <tbody>

            <tr
                v-for="item in fundRecords"
                :key="item.id"
            >

              <td>{{ item.username }}</td>

              <td>{{ item.type }}</td>

              <td>{{ money(item.amount) }}</td>

              <td>{{ money(item.balanceAfter) }}</td>

              <td>{{ item.description }}</td>

              <td>{{ item.createdAt }}</td>

            </tr>

            </tbody>

          </table>

        </section>

      </main>

    </div>

  </div>

</template>