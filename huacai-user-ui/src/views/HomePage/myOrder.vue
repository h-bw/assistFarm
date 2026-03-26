<template>
  <div class="order-page">
    <section class="order-hero">
      <div>
        <p class="hero-kicker">订单中心</p>
        <h1 class="hero-title">查看订单进度与支付状态</h1>
      </div>
      <div class="hero-summary">
        <div class="summary-pill">
          <span>当前筛选</span>
          <strong>{{ activeStatus }}</strong>
        </div>
        <div class="summary-pill">
          <span>订单数量</span>
          <strong>{{ total }}</strong>
        </div>
      </div>
    </section>

    <section class="order-filter-card">
      <el-tabs v-model="activeStatus" @tab-change="fetchOrders">
        <el-tab-pane
          v-for="dict in combinedOrderStatus"
          :key="dict.name"
          :label="dict.label"
          :name="dict.name"
        />
      </el-tabs>
    </section>

    <section class="order-list-card" v-loading="loading">
      <div v-if="total === 0" class="empty-order">
        <div class="empty-state-card">
          <el-empty description="暂无订单">
            <template #description>
              <div class="empty-state-text">
                <strong>你还没有产生订单</strong>
                <p>可以先去浏览产品并完成加入购物车、下单与支付流程。</p>
              </div>
            </template>
            <el-button type="primary" @click="router.push('/index/products')">去逛逛</el-button>
          </el-empty>
        </div>
      </div>

      <div v-else class="order-list">
        <article class="order-item" v-for="order in ordersList" :key="order.ordersId">
          <header class="order-header">
            <div class="order-header-main">
              <div class="order-number">订单号：{{ order.ordersId }}</div>
              <div class="order-time">下单时间：{{ order.createTime }}</div>
            </div>
            <div class="order-status">
              <dict-tag :options="order_status" :value="order.status" />
            </div>
          </header>

          <div class="order-products">
            <div class="product-row" v-for="item in order.ordersProductsList" :key="item.opId">
              <img :src="baseUrl + item.image" alt="" class="product-image" />
              <div class="product-info">
                <div class="product-name">{{ item.productsName }}</div>
                <div class="product-farmer">来自农户：{{ order.farmersName }}</div>
                <div class="product-specs">规格：{{ item.specs }}</div>
              </div>
              <div class="product-data">
                <span class="product-price">￥{{ Number(item.price).toFixed(2) }}</span>
                <span class="product-quantity">x{{ item.quantity }}</span>
                <span class="product-subtotal">小计 ￥{{ (Number(item.price) * Number(item.quantity)).toFixed(2) }}</span>
              </div>
            </div>
          </div>

          <footer class="order-footer">
            <div class="order-total">
              共 {{ order.ordersProductsList.length }} 件商品
              <span class="total-amount">￥{{ Number(order.totalPrice).toFixed(2) }}</span>
            </div>
            <div class="order-actions">
              <el-button size="small" @click="viewOrderDetail(order.ordersId)">订单详情</el-button>
              <template v-if="order.status === '待付款'">
                <el-button size="small" @click="cancelOrder(order)">取消订单</el-button>
                <el-button type="primary" size="small" @click="confirmPayment(order.ordersId)">去支付</el-button>
              </template>
              <template v-else-if="order.status === '待收货'">
                <el-button type="primary" size="small" @click="receipt(order)">确认收货</el-button>
              </template>
            </div>
          </footer>
        </article>
      </div>

      <div class="pagination-wrap">
        <pagination
          v-show="total > 0"
          :total="total"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
        />
      </div>
    </section>

    <Recommend
      :userId="loginUser.id"
      :topN="4"
      scene="buy_again"
      title="再次购买推荐"
      kicker="复购推荐"
      description="结合历史订单频次与最近购买记录，优先展示更适合再次下单的助农商品。"
    />

    <vxe-modal title="订单详情" v-model="open" width="760px" show-maximize showFooter resize>
      <div class="order-detail" v-if="currentOrder">
        <section class="detail-section">
          <h3>订单信息</h3>
          <div class="detail-grid">
            <div class="detail-card">
              <span>订单号</span>
              <strong>{{ currentOrder.ordersId }}</strong>
            </div>
            <div class="detail-card">
              <span>下单时间</span>
              <strong>{{ currentOrder.createTime }}</strong>
            </div>
            <div class="detail-card">
              <span>订单状态</span>
              <strong>{{ currentOrder.status }}</strong>
            </div>
            <div class="detail-card" v-if="currentOrder.shippingTime">
              <span>发货时间</span>
              <strong>{{ currentOrder.shippingTime }}</strong>
            </div>
            <div class="detail-card" v-if="currentOrder.completeTime">
              <span>完成时间</span>
              <strong>{{ currentOrder.completeTime }}</strong>
            </div>
          </div>
        </section>

        <section class="detail-section">
          <h3>收货信息</h3>
          <div class="detail-grid">
            <div class="detail-card">
              <span>收货人</span>
              <strong>{{ currentOrder.name }}</strong>
            </div>
            <div class="detail-card">
              <span>联系电话</span>
              <strong>{{ currentOrder.phone }}</strong>
            </div>
            <div class="detail-card detail-card-wide">
              <span>收货地址</span>
              <strong>{{ currentOrder.address }}</strong>
            </div>
          </div>
        </section>

        <section class="detail-section">
          <h3>商品信息</h3>
          <div class="detail-products">
            <div class="detail-product-row" v-for="item in currentOrder.ordersProductsList" :key="item.opId">
              <img :src="baseUrl + item.image" alt="" class="detail-product-image" />
              <div class="detail-product-info">
                <div class="detail-product-name">{{ item.productsName }}</div>
                <div class="detail-product-specs">规格：{{ item.specs }}</div>
              </div>
              <div class="detail-product-price">￥{{ Number(item.price).toFixed(2) }}</div>
              <div class="detail-product-price">x{{ item.quantity }}</div>
              <div class="detail-product-total">￥{{ (Number(item.price) * Number(item.quantity)).toFixed(2) }}</div>
            </div>
          </div>
        </section>

        <section class="detail-section amount-section">
          <h3>订单金额</h3>
          <div class="amount-box">
            <div class="amount-row">
              <span>商品总价</span>
              <strong>￥{{ Number(currentOrder.totalPrice).toFixed(2) }}</strong>
            </div>
          </div>
        </section>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="open = false">关闭</el-button>
        </div>
      </template>
    </vxe-modal>
  </div>
</template>

<script setup>
import { computed, getCurrentInstance, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import useUserStore from '@/store/modules/user.js'
import { listOrders, updateOrders, queryPaymentStatus } from '@/api/assisting/orders.js'
import Recommend from '@/components/Recommend/index.vue'

const router = useRouter()
const route = useRoute()
const { proxy } = getCurrentInstance()
const { order_status } = proxy.useDict('order_status')
const baseUrl = import.meta.env.VITE_APP_BASE_API
const backendUrl = 'http://localhost:8080'

const open = ref(false)
const currentOrder = ref(null)
const activeStatus = ref('全部订单')
const loading = ref(false)

const loginUser = useUserStore()

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  ordersId: null,
  status: '全部订单',
  userId: loginUser.id
})

const total = ref(0)
const ordersList = ref([])

const combinedOrderStatus = computed(() => [
  { label: '全部订单', name: '全部订单' },
  ...(order_status.value || []).map(item => ({
    label: item.label,
    value: item.value,
    name: item.value
  }))
])

const viewOrderDetail = ordersId => {
  currentOrder.value = ordersList.value.find(order => order.ordersId === ordersId)
  open.value = true
}

const fetchOrders = () => {
  queryParams.value.pageNum = 1
  queryParams.value.status = activeStatus.value
  getList()
}

const syncPaymentReturn = async () => {
  const orderId = Array.isArray(route.query.orderId) ? route.query.orderId[0] : route.query.orderId
  if (!orderId) {
    return
  }

  try {
    await queryPaymentStatus(String(orderId))
    ElMessage.success('支付结果已同步')
  } catch (error) {
    console.error('同步支付结果失败：', error)
  } finally {
    await router.replace({ path: route.path })
  }
}

const receipt = order => {
  ElMessageBox.confirm('确认收到产品了吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const item = {
      ordersId: order.ordersId,
      status: '已完成',
      completeTime: new Date().getTime()
    }
    updateOrders(item).then(() => {
      getList()
      ElMessage({ type: 'success', message: '确认收货成功~' })
    })
  }).catch(() => {})
}

const confirmPayment = ordersId => {
  ElMessageBox.confirm('确认支付该订单吗', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    window.location.href = `${backendUrl}/api/pay/${ordersId}`
  }).catch(() => {
    ElMessage({ type: 'info', message: '取消支付' })
  })
}

const cancelOrder = order => {
  ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const item = {
      ordersId: order.ordersId,
      status: '已取消'
    }
    updateOrders(item).then(() => {
      getList()
      ElMessage({ type: 'success', message: '已取消订单' })
    })
  }).catch(() => {})
}

const getList = () => {
  loading.value = true
  const params = {
    ...queryParams.value,
    status: queryParams.value.status === '全部订单' ? null : queryParams.value.status
  }
  listOrders(params).then(res => {
    total.value = res.total
    ordersList.value = res.rows
  }).finally(() => {
    loading.value = false
  })
}

onMounted(async () => {
  await syncPaymentReturn()
  getList()
})
</script>

<style scoped>
.order-page {
  max-width: 1320px;
  margin: 0 auto;
  padding: 24px 20px 40px;
}

.order-hero,
.order-filter-card,
.order-list-card {
  border: 1px solid rgba(102, 151, 83, 0.14);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 16px 40px rgba(31, 72, 42, 0.07);
}

.order-hero {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 20px;
  padding: 28px 30px;
  border-radius: 26px;
  background:
    radial-gradient(circle at top left, rgba(92, 160, 102, 0.12), transparent 28%),
    linear-gradient(180deg, rgba(249, 253, 248, 0.96), rgba(255, 255, 255, 0.96));
}

.hero-kicker {
  margin: 0 0 8px;
  color: #2f8850;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.hero-title {
  margin: 0 0 10px;
  font-size: 34px;
  color: #1c3b28;
}

.hero-summary {
  display: flex;
  gap: 14px;
}

.summary-pill {
  min-width: 128px;
  padding: 16px 18px;
  border-radius: 18px;
  background: #fff;
  border: 1px solid rgba(106, 156, 88, 0.14);
}

.summary-pill span {
  display: block;
  color: #768b7f;
  font-size: 12px;
}

.summary-pill strong {
  display: block;
  margin-top: 6px;
  color: #254632;
  font-size: 20px;
}

.order-filter-card {
  margin-bottom: 20px;
  padding: 8px 20px 0;
  border-radius: 22px;
}

:deep(.order-filter-card .el-tabs__nav-wrap::after) {
  display: none;
}

:deep(.order-filter-card .el-tabs__item) {
  height: 46px;
  font-weight: 600;
}

.order-list-card {
  padding: 22px;
  border-radius: 26px;
}

.empty-order {
  padding: 50px 0;
}

.empty-state-card {
  padding: 18px;
  border-radius: 22px;
  border: 1px dashed rgba(102, 151, 83, 0.24);
  background: rgba(248, 252, 247, 0.74);
}

.empty-state-text strong {
  display: block;
  margin-bottom: 6px;
  color: #264432;
  font-size: 18px;
}

.empty-state-text p {
  margin: 0;
  color: #6f8177;
  line-height: 1.8;
}

.order-list {
  display: grid;
  gap: 18px;
}

.order-item {
  overflow: hidden;
  border-radius: 22px;
  border: 1px solid #e7efe8;
  background: linear-gradient(180deg, #ffffff 0%, #fbfdfb 100%);
}

.order-header,
.order-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 22px;
}

.order-header {
  border-bottom: 1px solid #edf2ee;
  background: rgba(246, 250, 245, 0.9);
}

.order-header-main {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 22px;
}

.order-number {
  color: #233a2a;
  font-weight: 700;
}

.order-time {
  color: #6f8378;
  font-size: 13px;
}

.order-products {
  padding: 8px 22px;
}

.product-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 0;
}

.product-row:not(:last-child) {
  border-bottom: 1px dashed #e5ece6;
}

.product-image {
  width: 88px;
  height: 88px;
  border-radius: 16px;
  object-fit: cover;
  box-shadow: 0 8px 20px rgba(31, 73, 44, 0.08);
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  margin-bottom: 8px;
  color: #203826;
  font-size: 16px;
  font-weight: 700;
}

.product-farmer,
.product-specs {
  color: #73867b;
  font-size: 13px;
  line-height: 1.8;
}

.product-data {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 18px;
  min-width: 290px;
  text-align: right;
}

.product-price,
.product-quantity {
  color: #31493a;
  font-size: 14px;
}

.product-subtotal {
  color: #eb5c3c;
  font-size: 15px;
  font-weight: 700;
}

.order-footer {
  border-top: 1px solid #edf2ee;
  background: rgba(249, 251, 248, 0.92);
}

.order-total {
  color: #66796e;
  font-size: 14px;
}

.total-amount {
  margin-left: 12px;
  color: #eb5c3c;
  font-size: 20px;
  font-weight: 800;
}

.order-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

.order-detail {
  padding: 10px 6px 20px;
}

.detail-section + .detail-section {
  margin-top: 24px;
}

.detail-section h3 {
  margin: 0 0 14px;
  color: #23422f;
  font-size: 17px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.detail-card {
  padding: 16px;
  border-radius: 16px;
  background: #f8fbf7;
  border: 1px solid rgba(106, 152, 87, 0.12);
}

.detail-card-wide {
  grid-column: 1 / -1;
}

.detail-card span {
  display: block;
  margin-bottom: 6px;
  color: #7a8d83;
  font-size: 12px;
}

.detail-card strong {
  color: #274333;
  font-size: 14px;
  line-height: 1.7;
}

.detail-products {
  overflow: hidden;
  border-radius: 18px;
  border: 1px solid #e6ede7;
}

.detail-product-row {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr) 92px 60px 110px;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: #fff;
}

.detail-product-row:not(:last-child) {
  border-bottom: 1px solid #edf2ee;
}

.detail-product-image {
  width: 72px;
  height: 72px;
  border-radius: 14px;
  object-fit: cover;
}

.detail-product-name {
  margin-bottom: 6px;
  color: #223b29;
  font-weight: 700;
}

.detail-product-specs {
  color: #72867b;
  font-size: 13px;
}

.detail-product-price {
  color: #42584b;
  text-align: right;
}

.detail-product-total {
  color: #eb5c3c;
  font-weight: 700;
  text-align: right;
}

.amount-box {
  margin-left: auto;
  max-width: 320px;
  padding: 18px;
  border-radius: 18px;
  background: linear-gradient(135deg, #fff6f2, #ffffff);
  border: 1px solid rgba(234, 123, 79, 0.12);
}

.amount-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #6c7b73;
}

.amount-row strong {
  color: #eb5c3c;
  font-size: 20px;
}

.dialog-footer {
  text-align: center;
}

@media (max-width: 992px) {
  .order-hero,
  .order-header,
  .order-footer {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-summary,
  .order-actions {
    width: 100%;
  }

  .detail-grid {
    grid-template-columns: 1fr 1fr;
  }

  .detail-product-row {
    grid-template-columns: 72px minmax(0, 1fr);
  }

  .detail-product-price,
  .detail-product-total {
    text-align: left;
  }
}

@media (max-width: 768px) {
  .order-page {
    padding: 16px 14px 34px;
  }

  .order-list-card,
  .order-filter-card,
  .order-hero {
    border-radius: 20px;
  }

  .hero-title {
    font-size: 28px;
  }

  .product-row,
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .product-data {
    width: 100%;
    min-width: 0;
    justify-content: space-between;
    text-align: left;
  }

  .detail-product-row {
    grid-template-columns: 1fr;
  }

  .amount-box {
    max-width: none;
  }
}
</style>
