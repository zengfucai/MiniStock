<script setup>

import {
  ref,
  onMounted,
  computed
} from 'vue'

import {
  useRouter
} from 'vue-router'

import api from '../api/api'

const router = useRouter()

const user = ref(null)
const account = ref(null)

const stocks = ref([])
const positions = ref([])
const applications = ref([])
const trades = ref([])
const fundRecords = ref([])

const active = ref('market')

const selectedStock = ref(null)
const quantity = ref(100)

const mobileMenuOpen = ref(false)

const totalPositionValue = computed(() => {

  return positions.value.reduce(
      (total, position) => {

        return total +
            Number(position.stockPrice || 0) *
            Number(position.quantity || 0)

      },
      0
  )

})

const totalAssets = computed(() => {

  return Number(account.value?.balance || 0) +
      totalPositionValue.value

})

async function load() {

  const info =
      await api.get('/user/info')

  user.value =
      info.data.user

  account.value =
      info.data.account

  positions.value =
      info.data.positions || []

  const stockResult =
      await api.get('/stocks')

  stocks.value =
      stockResult.data || []

  const applicationsResult =
      await api.get(
          '/user/applications'
      )

  applications.value =
      applicationsResult.data || []

  const tradesResult =
      await api.get(
          '/user/trades'
      )

  trades.value =
      tradesResult.data || []

  const fundResult =
      await api.get(
          '/user/fund-records'
      )

  fundRecords.value =
      fundResult.data || []
}

function selectStock(stock) {

  selectedStock.value = stock

  quantity.value = 100
}

function closeTrade() {

  selectedStock.value = null
}

function selectMenu(menu) {

  active.value = menu

  mobileMenuOpen.value = false
}

async function apply(type) {

  if (!selectedStock.value) {

    alert('请选择股票')

    return
  }

  if (
      !quantity.value ||
      quantity.value <= 0
  ) {

    alert('请输入正确数量')

    return
  }

  try {

    await api.post(
        '/trades/apply',
        {
          stockId:
          selectedStock.value.id,

          type,

          quantity:
              Number(quantity.value)
        }
    )

    alert(
        '交易申请已经提交，请等待管理员审核'
    )

    selectedStock.value = null

    await load()

  } catch (error) {
  }
}

async function logout() {

  await api.post('/auth/logout')

  router.push('/login')
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

function formatDate(value) {

  if (!value) {
    return '-'
  }

  return String(value)
      .replace('T', ' ')
      .substring(0, 19)
}

onMounted(load)

</script>

<template>

  <div class="app-page">

    <header class="topbar">

      <div class="brand-area">

        <button
            class="mobile-menu-button"
            @click="mobileMenuOpen = true"
            aria-label="打开导航"
        >
          ☰
        </button>

        <strong class="brand">
          MiniStock
        </strong>

        <span class="role">
          普通用户
        </span>

      </div>

      <div class="user-area">

        <span class="username">
          {{ user?.username }}
        </span>

        <button
            class="small-button"
            @click="logout"
        >
          退出
        </button>

      </div>

    </header>

    <div
        v-if="mobileMenuOpen"
        class="mobile-menu-overlay"
        @click="mobileMenuOpen = false"
    ></div>

    <div class="dashboard">

      <aside
          class="sidebar"
          :class="{
            'mobile-open': mobileMenuOpen
          }"
      >

        <div class="sidebar-header">

          <div class="sidebar-title">
            交易中心
          </div>

          <button
              class="mobile-close-button"
              @click="mobileMenuOpen = false"
              aria-label="关闭导航"
          >
            ×
          </button>

        </div>

        <button
            :class="{active: active === 'market'}"
            @click="selectMenu('market')"
        >
          <span>股票行情</span>
        </button>

        <button
            :class="{active: active === 'positions'}"
            @click="selectMenu('positions')"
        >
          <span>我的持仓</span>
        </button>

        <button
            :class="{active: active === 'applications'}"
            @click="selectMenu('applications')"
        >
          <span>交易申请</span>
        </button>

        <button
            :class="{active: active === 'trades'}"
            @click="selectMenu('trades')"
        >
          <span>交易记录</span>
        </button>

        <button
            :class="{active: active === 'fund'}"
            @click="selectMenu('fund')"
        >
          <span>资金流水</span>
        </button>

      </aside>

      <main class="content">

        <div class="asset-cards">

          <div class="asset-card">

            <span class="asset-title">
              账户余额
            </span>

            <strong class="asset-value">
              {{ money(account?.balance) }}
            </strong>

          </div>

          <div class="asset-card">

            <span class="asset-title">
              持仓市值
            </span>

            <strong class="asset-value">
              {{ money(totalPositionValue) }}
            </strong>

          </div>

          <div class="asset-card total-asset-card">

            <span class="asset-title">
              总资产
            </span>

            <strong class="asset-value">
              {{ money(totalAssets) }}
            </strong>

          </div>

        </div>

        <section
            v-if="active === 'market'"
            class="content-section"
        >

          <div class="section-header">

            <div>

              <h2>
                股票行情
              </h2>

              <p>
                查看当前股票价格与交易状态
              </p>

            </div>

          </div>

          <div class="table-container">

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

                <td class="code-cell">
                  {{ stock.code }}
                </td>

                <td class="stock-name">
                  {{ stock.name }}
                </td>

                <td class="number-cell">
                  {{ money(stock.price) }}
                </td>

                <td
                    class="number-cell"
                    :class="{
                      'rise': Number(stock.changeRate) > 0,
                      'fall': Number(stock.changeRate) < 0
                    }"
                >
                  {{ stock.changeRate }}%
                </td>

                <td>

                  <span
                      class="status-tag"
                      :class="stock.status === 'NORMAL'
                          ? 'status-normal'
                          : 'status-stop'"
                  >
                    {{
                      stock.status === 'NORMAL'
                          ? '正常'
                          : '停牌'
                    }}
                  </span>

                </td>

                <td>

                  <button
                      v-if="stock.status === 'NORMAL'"
                      class="action-button"
                      @click="selectStock(stock)"
                  >
                    交易
                  </button>

                  <span
                      v-else
                      class="muted-text"
                  >
                    暂停交易
                  </span>

                </td>

              </tr>

              <tr v-if="stocks.length === 0">

                <td
                    colspan="6"
                    class="empty-cell"
                >
                  暂无股票数据
                </td>

              </tr>

              </tbody>

            </table>

          </div>

          <div
              v-if="selectedStock"
              class="trade-box"
          >

            <div class="trade-header">

              <div>

                <h3>
                  {{ selectedStock.name }}
                </h3>

                <span>
                  {{ selectedStock.code }}
                </span>

              </div>

              <button
                  class="close-button"
                  @click="closeTrade"
              >
                ×
              </button>

            </div>

            <div class="trade-info">

              <div>

                <span>
                  当前价格
                </span>

                <strong>
                  {{ money(selectedStock.price) }}
                </strong>

              </div>

              <div>

                <span>
                  数量
                </span>

                <input
                    v-model.number="quantity"
                    type="number"
                    min="1"
                    placeholder="请输入数量"
                >

              </div>

            </div>

            <div class="trade-actions">

              <button
                  @click="apply('BUY')"
              >
                申请买入
              </button>

              <button
                  class="danger"
                  @click="apply('SELL')"
              >
                申请卖出
              </button>

            </div>

          </div>

        </section>

        <section
            v-if="active === 'positions'"
            class="content-section"
        >

          <div class="section-header">

            <div>

              <h2>
                我的持仓
              </h2>

              <p>
                当前账户持有的股票
              </p>

            </div>

          </div>

          <div class="table-container">

            <table>

              <thead>

              <tr>
                <th>股票</th>
                <th>数量</th>
                <th>成本价</th>
                <th>当前价</th>
                <th>市值</th>
              </tr>

              </thead>

              <tbody>

              <tr
                  v-for="item in positions"
                  :key="item.id"
              >

                <td class="stock-name">
                  {{ item.stockName }}
                </td>

                <td class="number-cell">
                  {{ item.quantity }}
                </td>

                <td class="number-cell">
                  {{ money(item.averagePrice) }}
                </td>

                <td class="number-cell">
                  {{ money(item.stockPrice) }}
                </td>

                <td class="number-cell emphasis">
                  {{
                    money(
                        Number(item.stockPrice || 0) *
                        Number(item.quantity || 0)
                    )
                  }}
                </td>

              </tr>

              <tr v-if="positions.length === 0">

                <td
                    colspan="5"
                    class="empty-cell"
                >
                  暂无持仓
                </td>

              </tr>

              </tbody>

            </table>

          </div>

        </section>

        <section
            v-if="active === 'applications'"
            class="content-section"
        >

          <div class="section-header">

            <div>

              <h2>
                交易申请
              </h2>

              <p>
                查看提交的买卖申请及审核状态
              </p>

            </div>

          </div>

          <div class="table-container">

            <table>

              <thead>

              <tr>
                <th>股票</th>
                <th>类型</th>
                <th>数量</th>
                <th>价格</th>
                <th>金额</th>
                <th>状态</th>
                <th>原因</th>
              </tr>

              </thead>

              <tbody>

              <tr
                  v-for="item in applications"
                  :key="item.id"
              >

                <td class="stock-name">
                  {{ item.stockName }}
                </td>

                <td>

                  <span
                      class="type-tag"
                      :class="item.type === 'BUY'
                          ? 'buy-tag'
                          : 'sell-tag'"
                  >
                    {{
                      item.type === 'BUY'
                          ? '买入'
                          : '卖出'
                    }}
                  </span>

                </td>

                <td class="number-cell">
                  {{ item.quantity }}
                </td>

                <td class="number-cell">
                  {{ money(item.price) }}
                </td>

                <td class="number-cell">
                  {{ money(item.amount) }}
                </td>

                <td>

                  <span
                      class="status-tag"
                      :class="{
                        'status-pending':
                            item.status === 'PENDING',

                        'status-normal':
                            item.status === 'APPROVED',

                        'status-stop':
                            item.status === 'REJECTED'
                      }"
                  >
                    {{
                      item.status === 'PENDING'
                          ? '待审核'
                          : item.status === 'APPROVED'
                              ? '已通过'
                              : '已拒绝'
                    }}
                  </span>

                </td>

                <td>
                  {{ item.reason || '-' }}
                </td>

              </tr>

              <tr v-if="applications.length === 0">

                <td
                    colspan="7"
                    class="empty-cell"
                >
                  暂无交易申请
                </td>

              </tr>

              </tbody>

            </table>

          </div>

        </section>

        <section
            v-if="active === 'trades'"
            class="content-section"
        >

          <div class="section-header">

            <div>

              <h2>
                交易记录
              </h2>

              <p>
                已完成的股票交易记录
              </p>

            </div>

          </div>

          <div class="table-container">

            <table>

              <thead>

              <tr>
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

                <td class="stock-name">
                  {{ item.stockName }}
                </td>

                <td>

                  <span
                      class="type-tag"
                      :class="item.type === 'BUY'
                          ? 'buy-tag'
                          : 'sell-tag'"
                  >
                    {{
                      item.type === 'BUY'
                          ? '买入'
                          : '卖出'
                    }}
                  </span>

                </td>

                <td class="number-cell">
                  {{ item.quantity }}
                </td>

                <td class="number-cell">
                  {{ money(item.price) }}
                </td>

                <td class="number-cell">
                  {{ money(item.amount) }}
                </td>

                <td>
                  {{ formatDate(item.createdAt) }}
                </td>

              </tr>

              <tr v-if="trades.length === 0">

                <td
                    colspan="6"
                    class="empty-cell"
                >
                  暂无交易记录
                </td>

              </tr>

              </tbody>

            </table>

          </div>

        </section>

        <section
            v-if="active === 'fund'"
            class="content-section"
        >

          <div class="section-header">

            <div>

              <h2>
                资金流水
              </h2>

              <p>
                查看账户资金变动记录
              </p>

            </div>

          </div>

          <div class="table-container">

            <table>

              <thead>

              <tr>
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

                <td>
                  {{ item.type }}
                </td>

                <td
                    class="number-cell"
                    :class="{
                      'rise': Number(item.amount) > 0,
                      'fall': Number(item.amount) < 0
                    }"
                >
                  {{ money(item.amount) }}
                </td>

                <td class="number-cell">
                  {{ money(item.balanceAfter) }}
                </td>

                <td>
                  {{ item.description || '-' }}
                </td>

                <td>
                  {{ formatDate(item.createdAt) }}
                </td>

              </tr>

              <tr v-if="fundRecords.length === 0">

                <td
                    colspan="5"
                    class="empty-cell"
                >
                  暂无资金流水
                </td>

              </tr>

              </tbody>

            </table>

          </div>

        </section>

      </main>

    </div>

    <div
        v-if="selectedStock"
        class="mobile-trade-modal"
    >

      <div
          class="mobile-trade-overlay"
          @click="closeTrade"
      ></div>

      <div class="mobile-trade-dialog">

        <div class="trade-header">

          <div>

            <h3>
              {{ selectedStock.name }}
            </h3>

            <span>
              {{ selectedStock.code }}
            </span>

          </div>

          <button
              class="close-button"
              @click="closeTrade"
          >
            ×
          </button>

        </div>

        <div class="trade-info">

          <div>

            <span>
              当前价格
            </span>

            <strong>
              {{ money(selectedStock.price) }}
            </strong>

          </div>

          <div>

            <span>
              数量
            </span>

            <input
                v-model.number="quantity"
                type="number"
                min="1"
                placeholder="请输入数量"
            >

          </div>

        </div>

        <div class="trade-actions">

          <button
              @click="apply('BUY')"
          >
            申请买入
          </button>

          <button
              class="danger"
              @click="apply('SELL')"
          >
            申请卖出
          </button>

        </div>

      </div>

    </div>

  </div>

</template>