<template>
  <div class="checkout-container">

    <!-- 返回按钮 -->
    <div class="back-to-home">
      <el-button type="text" @click="router.go(-1)" class="back-button">
        <el-icon><ArrowLeft/></el-icon>
        返回
      </el-button>
    </div>

    <!-- 步骤条：两步 -->
    <div class="checkout-steps">
      <el-steps :active="activeStep" finish-status="success" align-center>
        <el-step title="确认订单"/>
        <el-step title="支付"/>
      </el-steps>
    </div>

    <!-- 第一步: 确认订单 -->
    <div class="checkout-main" v-if="activeStep === 1">

      <!-- 收货地址 -->
      <div class="checkout-section">
        <div class="section-header">
          <h3>收货地址</h3>
          <el-button type="text" @click="insertAddress" v-if="addressesList.length !== 0">
            + 新增地址
          </el-button>
        </div>
        <div class="no-address-tip" v-if="addressesList.length === 0">
          <el-icon><Warning/></el-icon>
          <span>您还没有收货地址，请先新增地址</span>
          <el-button type="text" @click="insertAddress">+ 新增地址</el-button>
        </div>
        <div class="address-list">
          <div
              class="address-item"
              v-for="address in addressesList"
              :key="address.addressesId"
              :class="{ active: address.addressesId === selectedAddressId }"
              @click="selectAddress(address)"
          >
            <div class="address-info">
              <div class="address-name">
                <span>{{ address.name }}</span>
                <span class="phone">{{ address.phone }}</span>
                <el-tag v-if="address.isDefault" size="small" type="danger">默认</el-tag>
              </div>
              <div class="address-detail">{{ address.detail }}</div>
            </div>
            <div class="address-actions">
              <el-button type="text" @click.stop="updateAddress(address)">编辑</el-button>
              <el-button type="text" @click.stop="deleteAddress(address.addressesId)">删除</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 按农户分组的产品 -->
      <div class="farmer-orders" v-for="farmerOrder in farmerOrders" :key="farmerOrder.productsUserId">
        <div class="farmer-header">
          <span class="farmer-name">{{ farmerOrder.farmerName }} 农户的产品</span>
        </div>
        <div class="checkout-section">
          <div class="product-list">
            <div class="product-item" v-for="item in farmerOrder.ordersProductsList" :key="item.cartId">
              <img :src="resolveImageUrl(item.image)" class="product-image" alt="">
              <div class="product-info">
                <div class="product-name">{{ item.productsName }}</div>
                <div class="product-specs"><span>规格: {{ item.specs }}</span></div>
                <div class="product-farmer">产地: {{ item.origin }}</div>
              </div>
              <div class="product-price">¥{{ item.price.toFixed(2) }}</div>
              <div class="product-quantity">x{{ item.quantity }}</div>
              <div class="product-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
            </div>
          </div>
        </div>
        <div class="checkout-summary">
          <div class="summary-item total">
            <span>应付金额</span>
            <span class="total-amount">¥{{ farmerOrder.totalAmount.toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- 所有订单总金额 -->
      <div class="total-orders-summary">
        <div class="summary-item total">
          <span>共计 {{ farmerOrders.length }} 个订单</span>
          <span class="total-amount">¥{{ totalAllOrdersAmount.toFixed(2) }}</span>
        </div>
      </div>

      <!-- 提交订单按钮 -->
      <div class="checkout-actions">
        <el-button
            type="primary"
            size="large"
            @click="submitOrder"
            :disabled="!selectedAddressId || addressesList.length === 0"
        >
          提交订单（{{ farmerOrders.length }} 个）
        </el-button>
      </div>
    </div>

    <!-- 第二步: 支付 -->
    <div class="checkout-main" v-if="activeStep === 2">
      <div class="payment-container">

        <div class="order-info">
          <el-result icon="success" title="订单提交成功" sub-title="请完成支付以便商家尽快处理订单">
            <template #extra>
              <div class="order-details">
                <p>订单号：{{ orderIdList.join('、') }}</p>
                <p>总应付金额：<span class="amount">¥{{ totalAllOrdersAmount.toFixed(2) }}</span></p>
                <el-alert
                    v-if="orderIdList.length > 1"
                    :title="`当前共生成 ${orderIdList.length} 个订单，本次将先支付第一个订单，其余订单可在“我的订单”中继续支付。`"
                    type="warning"
                    :closable="false"
                    style="margin-top: 12px; text-align: left"
                />
              </div>
            </template>
          </el-result>
        </div>

        <!-- 支付方式（仅支付宝）-->
        <div class="payment-methods">
          <h3 class="payment-title">支付方式</h3>
          <div class="method-list">
            <div class="method-item active">
              <div class="method-icon">
                <SvgIcon style="font-size: 40px" icon-class="支付宝"/>
              </div>
              <div class="method-name">支付宝支付</div>
              <div class="method-check">
                <el-icon><Select/></el-icon>
              </div>
            </div>
          </div>
        </div>

        <div class="payment-actions">
          <el-button type="primary" size="large" @click="confirmPayment">
            立即支付
          </el-button>
        </div>

      </div>
    </div>

    <!-- 收货地址表单弹窗 -->
    <AddressFormDialog ref="addressFormDialog" @ok="getMyAddressList"/>
  </div>
</template>

<script setup>
import { useCartStore } from "@/store/modules/cart.js"
import { useRouter } from "vue-router"
import { ArrowLeft, Select, Warning } from "@element-plus/icons-vue"
import useUserStore from "@/store/modules/user.js"
import { delAddresses, listAddresses } from "@/api/assisting/addresses.js"
import { ElMessage, ElMessageBox } from "element-plus"
import AddressFormDialog from "@/views/dialog/AddressFormDialog.vue"
import { addOrders } from "@/api/assisting/orders.js"
import SvgIcon from "@/components/SvgIcon/index.vue"

// 直接使用后端真实地址，window.location.href 不走 Vite 代理
// 如果后端端口有变化，修改这里即可
const backendUrl = (import.meta.env.VITE_APP_BACKEND_URL || window.location.origin).replace(/\/$/, '')
const baseUrl = import.meta.env.VITE_APP_BASE_API
const CART_CLEANUP_STORAGE_KEY = 'pendingCheckoutCartCleanup'
const CART_CLEANUP_TTL = 1000 * 60 * 60 * 24
const resolveImageUrl = (image) => {
  if (!image) return ''
  const str = String(image)
  if (/^https?:\/\//i.test(str)) return encodeURI(str)
  if (str.startsWith('/downloaded-images/')) return encodeURI(str)
  if (str.startsWith('/.downloaded-images/')) {
    return encodeURI(str.replace('/.downloaded-images/', '/downloaded-images/'))
  }
  return encodeURI(baseUrl + str)
}
const router = useRouter()
const cartStore = useCartStore()
const loginUser = useUserStore()

// 步骤控制
const activeStep = ref(1)

// 地址相关
const addressFormDialog = ref(null)
const addressesList = ref([])
const selectedAddressId = ref(null)
const selectedAddress = ref(null)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  name: null,
  isDefault: null,
  userId: loginUser.id,
  userName: null,
})

const getMyAddressList = () => {
  listAddresses(queryParams.value).then(res => {
    addressesList.value = res.rows
    // 自动选中默认地址，没有默认地址则选第一个
    if (addressesList.value.length > 0 && !selectedAddressId.value) {
      const defaultAddr = addressesList.value.find(item => item.isDefault)
      selectedAddress.value = defaultAddr || addressesList.value[0]
      selectedAddressId.value = selectedAddress.value.addressesId
    }
  })
}

const selectAddress = (address) => {
  selectedAddress.value = address
  selectedAddressId.value = address.addressesId
}

const insertAddress = () => {
  addressFormDialog.value.handleAdd()
}

const updateAddress = (address) => {
  addressFormDialog.value.handleUpdate(address)
}

const deleteAddress = (addressesId) => {
  ElMessageBox.confirm('是否确认删除该地址？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    delAddresses(addressesId).then(() => {
      // 删除后重置选中状态，重新查询
      selectedAddressId.value = null
      selectedAddress.value = null
      getMyAddressList()
      ElMessage({ type: 'success', message: '删除成功' })
    })
  }).catch(() => {})
}

// 订单相关
const checkoutItems = computed(() => cartStore.checkoutItems)
const farmerOrders = ref([])
const orderIdList = ref([])

const totalAllOrdersAmount = computed(() => {
  return farmerOrders.value.reduce((total, order) => total + order.totalAmount, 0)
})

const groupItemsByFarmers = (items) => {
  const farmerMap = {}
  items.forEach(item => {
    if (!farmerMap[item.productsUserId]) {
      farmerMap[item.productsUserId] = {
        productsUserId: item.productsUserId,
        farmerName: item.toUserName,
        ordersProductsList: [],
        totalAmount: 0
      }
    }
    farmerMap[item.productsUserId].ordersProductsList.push(item)
    farmerMap[item.productsUserId].totalAmount += item.price * item.quantity
  })
  return Object.values(farmerMap)
}

const persistPendingCartCleanup = (orderIds, cartIds) => {
  localStorage.setItem(CART_CLEANUP_STORAGE_KEY, JSON.stringify({
    userId: String(loginUser.id || ''),
    orderIds: (orderIds || []).filter(Boolean).map(item => String(item)),
    cartIds: (cartIds || []).filter(Boolean).map(item => String(item)),
    savedAt: Date.now(),
    expiresAt: Date.now() + CART_CLEANUP_TTL
  }))
}

const extractOrderId = (result) => {
  if (!result) {
    return null
  }
  if (typeof result === 'string') {
    return result
  }
  if (typeof result.data === 'string' && result.data.trim()) {
    return result.data
  }
  if (typeof result.msg === 'string' && result.msg.trim() && result.msg !== '操作成功') {
    return result.msg
  }
  return null
}

// 提交订单
const submitOrder = () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  ElMessageBox.confirm(
      `确认提交 ${farmerOrders.value.length} 个订单吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      const createOrderPromises = farmerOrders.value.map(farmerOrder => {
        return addOrders({
          productsUserId: farmerOrder.productsUserId,
          farmerName: farmerOrder.farmerName,
          totalPrice: farmerOrder.totalAmount,
          ordersProductsList: farmerOrder.ordersProductsList,
          name: selectedAddress.value.name,
          phone: selectedAddress.value.phone,
          address: selectedAddress.value.detail,
        })
      })

      const createResults = await Promise.all(createOrderPromises)
      orderIdList.value = createResults.map(extractOrderId).filter(Boolean)
      if (orderIdList.value.length !== createResults.length) {
        throw new Error('订单号生成失败，请稍后重试')
      }

      // 清理购物车中已结算的产品
      // Defer cart cleanup until payment success so failed payments can be retried.
      const cartsId = checkoutItems.value.map(item => item.cartId)
      persistPendingCartCleanup(orderIdList.value, cartsId)

      activeStep.value = 2
      ElMessage({ type: 'success', message: '订单提交成功' })
    } catch (error) {
      ElMessage.error('提交订单失败：' + (error.message || '未知错误，请联系管理员'))
    }
  }).catch(() => {})
}

/**
 * 确认支付
 * 使用后端真实地址跳转支付宝沙盒（window.location.href 不走 Vite 代理）
 * 支付完成后由后端 return-url 跳回 /index/myOrder
 * 多订单时其余订单在【我的订单】页逐个支付
 */
const confirmPayment = () => {
  if (orderIdList.value.length === 0) {
    ElMessage.error('订单信息异常，请重新下单')
    return
  }
  const orderId = orderIdList.value[0]
  // 使用 fetch 获取后端返回的支付宝跳转表单，并在当前用户点击事件内执行 submit
  // 目的：避免某些浏览器对“页面加载后自动提交”的限制导致无法进入支付宝沙盒
  const payUrl = `${backendUrl}/api/pay/${orderId}`
  fetch(payUrl, { method: 'GET', credentials: 'omit' })
    .then(async res => {
      const text = await res.text()
      if (!res.ok) {
        throw new Error(extractPayError(text) || `支付接口请求失败：${res.status}`)
      }
      return text
    })
    .then(html => {
      const container = document.createElement('div')
      container.innerHTML = html
      // 移除脚本，防止重复提交或受浏览器策略影响
      container.querySelectorAll('script').forEach(s => s.remove())
      const sourceForm = container.querySelector('form')
      if (!sourceForm) throw new Error(extractPayError(html) || '支付表单解析失败')
      const action = sourceForm.getAttribute('action') || ''
      if (!/alipay/i.test(action)) {
        throw new Error('支付网关地址异常，请稍后重试')
      }
      const sourceInputs = Array.from(sourceForm.querySelectorAll('input'))
      const hasBizContent = sourceInputs.some(input => input.name === 'biz_content' && String(input.value || '').trim())
      if (!hasBizContent) {
        throw new Error('支付参数缺失（biz_content），请稍后重试')
      }
      // 重建 form，避免直接搬运节点导致个别浏览器丢失 hidden 参数
      const form = document.createElement('form')
      form.method = 'post'
      form.action = action
      form.acceptCharset = 'UTF-8'
      sourceInputs.forEach(input => {
        const hidden = document.createElement('input')
        hidden.type = 'hidden'
        hidden.name = input.name
        hidden.value = input.value
        form.appendChild(hidden)
      })
      document.body.appendChild(form)
      form.submit()
    })
    .catch(err => {
      showPayErrorWithRetry(err, () => confirmPayment())
    })
}

const extractPayError = (raw) => {
  if (!raw) return ''
  const text = String(raw)
  const match = text.match(/\"msg\"\s*:\s*\"([^\"]+)\"/)
  if (match && match[1]) return match[1]
  const htmlTitle = text.match(/<title>([^<]+)<\/title>/i)
  if (htmlTitle && htmlTitle[1]) return htmlTitle[1]
  if (text.includes('订单已支付')) return '订单已支付，无需重复支付'
  if (text.includes('订单已取消')) return '订单已取消，无法发起支付'
  if (text.includes('订单金额异常')) return '订单金额异常，请刷新后重试'
  if (text.includes('支付配置不完整')) return '支付配置不完整，请联系管理员'
  if (text.includes('SYSTEM_ERROR')) return '支付宝沙箱系统繁忙，请稍后再试'
  if (text.includes('unable to handle this request')) return '支付宝沙箱页面暂不可用，请稍后重试'
  if (text.includes('支付网关预检查失败')) return text
  return ''
}

const showPayErrorWithRetry = (err, retryAction) => {
  const rawMessage = String(err?.message || err || '未知错误')
  const refined = extractPayError(rawMessage) || rawMessage
  const isSandboxAbnormal = /SYSTEM_ERROR|unable to handle this request|沙箱|alipaydev/i.test(rawMessage)
  if (!isSandboxAbnormal) {
    ElMessage.error('支付跳转失败：' + refined)
    return
  }
  ElMessageBox.confirm(
    `检测到支付宝沙箱可能异常：${refined}\n\n建议切换无痕窗口或稍后重试。是否立即重试支付？`,
    '支付提示',
    {
      confirmButtonText: '立即重试',
      cancelButtonText: '稍后再试',
      type: 'warning'
    }
  ).then(() => {
    retryAction()
  }).catch(() => {})
}

onMounted(() => {
  getMyAddressList()
  farmerOrders.value = groupItemsByFarmers(checkoutItems.value)
})
</script>

<style scoped>
.checkout-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.checkout-steps {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.back-to-home { margin-bottom: 20px; }

.back-button {
  padding: 0;
  font-size: 16px;
  color: #666;
}

.back-button:hover { color: #3AAE6E; }

.back-button .el-icon { margin-right: 5px; }

.checkout-main {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.checkout-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.no-address-tip {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  background-color: #f5f5f5;
  border-radius: 4px;
  margin-bottom: 15px;
  font-size: 14px;
  color: #666;
}

.no-address-tip .el-icon {
  margin-right: 8px;
  color: red;
}

.no-address-tip .el-button {
  margin-left: 10px;
  padding: 0;
}

.address-list {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.address-item {
  width: calc(50% - 8px);
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  justify-content: space-between;
}

.address-item:hover { border-color: #3AAE6E; }

.address-item.active {
  border-color: #3AAE6E;
  background-color: #f0f9eb;
}

.address-info { flex: 1; }

.address-name {
  font-weight: bold;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.address-name .phone {
  margin: 0 10px;
  color: #666;
}

.address-detail {
  color: #666;
  font-size: 14px;
}

.address-actions {
  display: flex;
  align-items: center;
}

.farmer-orders {
  margin-bottom: 30px;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 15px;
}

.farmer-header {
  padding: 10px 0;
  margin-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.farmer-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.product-list {
  border: 1px solid #eee;
  border-radius: 4px;
}

.product-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.product-item:last-child { border-bottom: none; }

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  margin-right: 15px;
  border-radius: 4px;
}

.product-info {
  flex: 1;
  margin-right: 15px;
}

.product-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 5px;
}

.product-specs {
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
}

.product-specs span { margin-right: 8px; }

.product-farmer {
  font-size: 12px;
  color: #666;
}

.product-price,
.product-quantity,
.product-subtotal {
  width: 100px;
  text-align: center;
  font-size: 14px;
  color: #333;
}

.product-subtotal {
  color: #f56c6c;
  font-weight: bold;
}

.checkout-summary {
  margin-top: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.total-orders-summary {
  margin-top: 30px;
  padding: 20px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
}

.summary-item.total {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #eee;
  font-size: 16px;
  color: #333;
}

.total-amount {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;
}

.checkout-actions {
  margin-top: 30px;
  text-align: right;
}

.payment-container { padding: 20px; }

.order-info {
  margin-bottom: 30px;
  text-align: center;
}

.order-details {
  margin-top: 10px;
  font-size: 14px;
  color: #666;
}

.order-details p { margin: 6px 0; }

.order-details .amount {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;
}

.payment-methods {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.payment-title {
  margin: 0 0 20px 0;
  font-size: 16px;
  color: #333;
}

.method-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.method-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 4px;
  cursor: default;
}

.method-item.active {
  border-color: #3AAE6E;
  background-color: #f0f9eb;
}

.method-icon {
  width: 40px;
  height: 40px;
  margin-right: 15px;
}

.method-name {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.method-check {
  width: 20px;
  color: #3AAE6E;
}

.payment-actions {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

@media (max-width: 768px) {
  .address-item { width: 100%; }
  .product-item { flex-wrap: wrap; }
  .product-info {
    width: 100%;
    margin-bottom: 10px;
  }
  .product-price,
  .product-quantity,
  .product-subtotal {
    width: auto;
    margin-right: 15px;
  }
  .payment-actions .el-button { width: 100%; }
  .method-item { padding: 10px; }
  .method-icon {
    width: 30px;
    height: 30px;
    margin-right: 10px;
  }
}
</style>
