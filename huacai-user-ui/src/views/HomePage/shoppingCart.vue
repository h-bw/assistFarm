<template>
  <div class="cart-page">
    <section class="cart-hero">
      <div>
        <p class="hero-kicker">购物车</p>
        <h1 class="hero-title">整理待结算商品并快速进入订单流程</h1>
      </div>
      <div class="hero-stats">
        <div class="stat-card">
          <span>商品数量</span>
          <strong>{{ total }}</strong>
        </div>
        <div class="stat-card">
          <span>已选商品</span>
          <strong>{{ selectedItems.length }}</strong>
        </div>
      </div>
    </section>

    <section class="cart-main" v-loading="loading">
      <div class="cart-header">
        <div>
          <h2>我的购物车</h2>
        </div>
        <div class="cart-tips">
          <span>共 {{ total }} 件产品</span>
        </div>
      </div>

      <div class="cart-empty" v-if="cartList.length === 0">
        <div class="empty-state-card">
          <el-empty description="购物车空空如也~">
            <template #description>
              <div class="empty-state-text">
                <strong>购物车里还没有商品</strong>
                <p>去商品页挑选几件助农产品，加入购物车后即可继续结算。</p>
              </div>
            </template>
            <el-button type="primary" @click="router.push('/index/products')">去逛逛</el-button>
          </el-empty>
        </div>
      </div>

      <div class="cart-items" v-else>
        <el-table :data="cartList" style="width: 100%" border @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" align="center" />

          <el-table-column label="商品信息" min-width="360">
            <template #default="{ row }">
              <div class="product-info-cell">
                <img :src="resolveImageUrl(row.image)" class="product-image" alt="">
                <div class="product-details">
                  <div class="product-name">{{ row.productsName }}</div>
                  <div class="product-origin">规格：{{ row.specs }}</div>
                  <div class="product-origin">产地：{{ row.origin }}</div>
                  <div class="product-origin">来自农户：{{ row.toUserName }}</div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="单价" width="130" align="center">
            <template #default="{ row }">
              <div class="product-price">¥{{ Number(row.price).toFixed(2) }}</div>
            </template>
          </el-table-column>

          <el-table-column label="数量" width="220" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                :max="Number(row.inventory || 0) > 0 ? Number(row.inventory) : 999"
                size="large"
                @change="handleQuantityChange(row)"
                :disabled="Number(row.inventory || 0) <= 0"
              />
            </template>
          </el-table-column>

          <el-table-column label="剩余库存" width="140" align="center">
            <template #default="{ row }">
              <span>{{ Number(row.inventory || 0) }} 件</span>
            </template>
          </el-table-column>

          <el-table-column label="小计" width="140" align="center">
            <template #default="{ row }">
              <div class="product-subtotal">¥{{ (Number(row.price) * Number(row.quantity)).toFixed(2) }}</div>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="90" align="center">
            <template #default="{ row }">
              <el-button
                type="danger"
                size="small"
                :icon="Delete"
                circle
                @click="removeItem(row.cartId)"
              />
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="cart-footer" v-if="cartList.length > 0">
        <div class="footer-left">
          <div class="total-info">
            <span class="total-label">合计金额</span>
            <div class="total-amount">
              <span class="amount">¥{{ totalAmount.toFixed(2) }}</span>
            </div>
          </div>
        </div>
        <div class="footer-right">
          <el-button type="primary" size="large" @click="checkout" :disabled="selectedItems.length === 0">
            去结算
          </el-button>
        </div>
      </div>
    </section>

    <Recommend
      :userId="loginUser.id"
      :topN="4"
      scene="cross_sell"
      :product-ids="cartProductIds"
      title="搭配购买推荐"
      kicker="购物车联动"
      description="根据当前购物车中的商品，为你补充相似产地、相近偏好或更适合一起购买的助农产品。"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { delCart, listCart, updateCart } from '@/api/assisting/cart.js'
import useUserStore from '@/store/modules/user.js'
import { useCartStore } from '@/store/modules/cart.js'
import Recommend from '@/components/Recommend/index.vue'

const router = useRouter()
const baseUrl = import.meta.env.VITE_APP_BASE_API
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
const selectedItems = ref([])
const cartStore = useCartStore()
const loading = ref(false)
const loginUser = useUserStore()

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  productsId: null,
  userId: loginUser.id,
  productsName: null,
  createUserName: null,
  toUserName: null
})

const cartList = ref([])
const total = ref(0)

const checkout = () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请先选择要结算的产品')
    return
  }
  cartStore.setCheckoutItems(selectedItems.value)
  router.push('/index/checkout')
}

const handleSelectionChange = selection => {
  selectedItems.value = selection
}

const handleQuantityChange = item => {
  const inventory = Number(item.inventory || 0)
  // 若库存不足，限制最大购买数量，避免提交后出现“已支付但库存为负”
  if (inventory > 0 && Number(item.quantity) > inventory) {
    item.quantity = inventory
    ElMessage.warning(`库存不足，已自动调整数量为 ${inventory}`)
    return
  }
  updateCart({
    cartId: item.cartId,
    quantity: item.quantity
  })
}

const removeItem = cartId => {
  ElMessageBox.confirm('确认删除该商品吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    delCart(cartId).then(() => {
      getList()
      ElMessage({ type: 'success', message: '删除成功' })
    })
  }).catch(() => {
    ElMessage({ type: 'info', message: '取消删除' })
  })
}

const totalAmount = computed(() => {
  return selectedItems.value.reduce((sum, item) => sum + Number(item.price) * Number(item.quantity), 0)
})

const cartProductIds = computed(() => cartList.value.map(item => item.productsId).filter(Boolean))

const getList = () => {
  loading.value = true
  listCart(queryParams.value).then(res => {
    total.value = res.total
    cartList.value = res.rows
  }).finally(() => {
    loading.value = false
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.cart-page {
  max-width: 1380px;
  margin: 0 auto;
  padding: 24px 20px 40px;
}

.cart-hero,
.cart-main {
  border: 1px solid rgba(101, 150, 84, 0.14);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 42px rgba(28, 69, 41, 0.08);
}

.cart-hero {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 20px;
  padding: 28px 30px;
  border-radius: 26px;
  background:
    radial-gradient(circle at top left, rgba(84, 156, 95, 0.12), transparent 28%),
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
  color: #1e3c2a;
  font-size: 34px;
}

.hero-stats {
  display: flex;
  gap: 14px;
}

.stat-card {
  min-width: 132px;
  padding: 16px 18px;
  border-radius: 18px;
  background: #fff;
  border: 1px solid rgba(106, 155, 88, 0.14);
}

.stat-card span {
  display: block;
  color: #778c80;
  font-size: 12px;
}

.stat-card strong {
  display: block;
  margin-top: 6px;
  color: #254633;
  font-size: 21px;
}

.cart-main {
  padding: 22px;
  border-radius: 26px;
}

.cart-header {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.cart-header h2 {
  margin: 0 0 6px;
  color: #1f3e2b;
  font-size: 28px;
}

.cart-tips span {
  margin: 0;
  color: #6c8074;
  font-size: 14px;
}

.cart-empty {
  padding: 50px 0;
}

.empty-state-card {
  padding: 18px;
  border-radius: 22px;
  border: 1px dashed rgba(105, 152, 86, 0.24);
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

.product-info-cell {
  display: flex;
  align-items: center;
  gap: 14px;
}

.product-image {
  width: 82px;
  height: 82px;
  border-radius: 16px;
  object-fit: cover;
  box-shadow: 0 10px 22px rgba(29, 72, 44, 0.08);
}

.product-name {
  margin-bottom: 8px;
  color: #213a2a;
  font-weight: 700;
  font-size: 15px;
}

.product-origin {
  color: #73867b;
  font-size: 13px;
  line-height: 1.8;
}

.product-price,
.product-subtotal {
  font-size: 16px;
  font-weight: 700;
}

.product-price {
  color: #365442;
}

.product-subtotal {
  color: #eb5d3f;
}

.cart-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 20px;
  padding: 20px 22px;
  border-radius: 20px;
  background: linear-gradient(135deg, #f8fbf7, #ffffff);
  border: 1px solid #e7eee8;
}

.total-label {
  color: #788b81;
  font-size: 13px;
}

.amount {
  color: #eb5d3f;
  font-size: 28px;
  font-weight: 800;
}

:deep(.cart-items .el-table) {
  border-radius: 18px;
  overflow: hidden;
}

:deep(.cart-items .el-table th.el-table__cell) {
  background: #f7faf7;
  color: #5e7668;
  font-weight: 700;
}

:deep(.cart-items .el-input-number) {
  width: 130px;
}

@media (max-width: 992px) {
  .cart-hero,
  .cart-header,
  .cart-footer {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-stats {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .cart-page {
    padding: 16px 14px 34px;
  }

  .cart-hero,
  .cart-main {
    border-radius: 20px;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-stats {
    flex-direction: column;
  }
}
</style>
