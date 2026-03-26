<template>
  <div class="product-detail-page">
    <div class="back-wrap">
      <el-button text @click="router.go(-1)" class="back-button">
        <el-icon><ArrowLeft /></el-icon>
        返回上一页
      </el-button>
    </div>

    <section class="product-main-card">
      <div class="product-gallery">
        <div class="image-stage">
          <img :src="baseUrl + product.image" alt="" class="product-image" />
          <div class="image-badge">{{ product.origin || '优选产地' }}</div>
        </div>
      </div>

      <div class="product-info">
        <p class="product-kicker">助农优选</p>
        <h1 class="product-title">{{ product.name }}</h1>
        <p class="product-subtitle">{{ product.subtitle || '来自优质产地的精选农产品，新鲜直达，品质更安心。' }}</p>

        <div class="price-panel">
          <span class="price-label">商城价</span>
          <div class="price-main">
            <strong class="current-price">￥{{ product.price }}</strong>
            <span class="inventory-text">库存 {{ product.inventory || 0 }} 件</span>
          </div>
        </div>

        <div class="product-facts">
          <div class="fact-item">
            <span>产地</span>
            <strong>{{ product.origin || '待补充' }}</strong>
          </div>
          <div class="fact-item">
            <span>发货地</span>
            <strong>{{ product.shipFrom || '待补充' }}</strong>
          </div>
          <div class="fact-item">
            <span>规格</span>
            <strong>{{ product.specs || '待补充' }}</strong>
          </div>
        </div>

        <div class="quantity-panel">
          <span class="quantity-label">购买数量</span>
          <div class="quantity-control">
            <el-input-number v-model="quantity" :min="1" :max="999" size="large" />
            <span class="inventory-tip">库存充足时可直接加入购物车或立即下单</span>
          </div>
        </div>

        <div class="product-actions">
          <el-button type="primary" size="large" class="primary-action" @click="addToCart" v-loading="loading">
            <el-icon><ShoppingCart /></el-icon>
            加入购物车
          </el-button>
          <el-button size="large" @click="router.push('/index/products')">继续逛逛</el-button>
        </div>
      </div>
    </section>

    <section class="detail-extra-grid">
      <div class="info-card">
        <span class="card-label">商品亮点</span>
        <h3>产地清晰，信息完整</h3>
        <p>集中展示价格、产地、规格与库存等信息，方便用户快速了解商品情况。</p>
      </div>
      <div class="info-card">
        <span class="card-label">平台服务</span>
        <h3>购买流程顺畅衔接</h3>
        <p>用户可从商品详情直接加入购物车，继续完成结算、支付与订单查询等后续操作。</p>
      </div>
    </section>

    <section class="product-tabs-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="商品详情" name="detail">
          <div class="product-detail-content" v-html="product.detail || '<p>暂无商品详情内容</p>'" />
        </el-tab-pane>
        <el-tab-pane label="规格参数" name="specs">
          <div class="product-specs-content">
            <table>
              <tr>
                <th>产品名称</th>
                <td>{{ product.name }}</td>
              </tr>
              <tr>
                <th>产地</th>
                <td>{{ product.origin }}</td>
              </tr>
              <tr>
                <th>规格</th>
                <td>{{ product.specs }}</td>
              </tr>
              <tr>
                <th>保质期</th>
                <td>{{ product.expire }}</td>
              </tr>
              <tr>
                <th>储存方法</th>
                <td>{{ product.storage }}</td>
              </tr>
              <tr>
                <th>食用方法</th>
                <td>{{ product.edible }}</td>
              </tr>
            </table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, ShoppingCart } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getProducts } from '@/api/assisting/products.js'
import { addCart } from '@/api/assisting/cart.js'

const activeTab = ref('detail')
const route = useRoute()
const router = useRouter()
const product = ref({})
const baseUrl = import.meta.env.VITE_APP_BASE_API
const quantity = ref(1)
const loading = ref(false)

const addToCart = () => {
  loading.value = true
  const item = {
    productsId: route.params.id,
    quantity: quantity.value
  }
  addCart(item).then(() => {
    ElMessage.success(`已添加 ${quantity.value} 件 ${product.value.name} 到购物车`)
  }).finally(() => {
    loading.value = false
  })
}

onMounted(() => {
  getProducts(route.params.id).then(res => {
    product.value = res.data || {}
  })
})
</script>

<style scoped>
.product-detail-page {
  max-width: 1380px;
  margin: 0 auto;
  padding: 24px 20px 40px;
}

.back-wrap {
  margin-bottom: 16px;
}

.back-button {
  color: #5b7163;
  font-size: 15px;
  font-weight: 600;
}

.back-button:hover {
  color: #2d8450;
}

.product-main-card,
.product-tabs-card,
.info-card {
  border: 1px solid rgba(104, 152, 85, 0.14);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 42px rgba(28, 69, 41, 0.08);
}

.product-main-card {
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  gap: 26px;
  padding: 26px;
  border-radius: 28px;
  background:
    radial-gradient(circle at top left, rgba(102, 168, 103, 0.12), transparent 28%),
    linear-gradient(180deg, rgba(249, 253, 248, 0.96), rgba(255, 255, 255, 0.98));
}

.image-stage {
  position: relative;
  overflow: hidden;
  height: 100%;
  min-height: 520px;
  border-radius: 24px;
  background: linear-gradient(135deg, #f4f8ef, #ffffff);
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 28px;
}

.image-badge {
  position: absolute;
  left: 18px;
  top: 18px;
  padding: 7px 14px;
  border-radius: 999px;
  background: rgba(30, 118, 65, 0.9);
  color: #fff;
  font-size: 12px;
  letter-spacing: 0.06em;
}

.product-kicker {
  margin: 0 0 8px;
  color: #2d8850;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.product-title {
  margin: 0 0 12px;
  color: #1f3c2a;
  font-size: 36px;
  line-height: 1.2;
}

.product-subtitle {
  margin: 0 0 20px;
  color: #667c6f;
  line-height: 1.9;
}

.price-panel {
  margin-bottom: 18px;
  padding: 18px 20px;
  border-radius: 22px;
  background: linear-gradient(135deg, #fff5ef, #fffefe);
  border: 1px solid rgba(233, 118, 76, 0.12);
}

.price-label {
  display: block;
  margin-bottom: 10px;
  color: #847269;
  font-size: 13px;
}

.price-main {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 12px;
}

.current-price {
  color: #eb5d3f;
  font-size: 38px;
  line-height: 1;
}

.inventory-text {
  color: #6d8175;
  font-size: 14px;
}

.product-facts {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.fact-item {
  padding: 16px;
  border-radius: 18px;
  background: #f8fbf7;
  border: 1px solid rgba(104, 152, 85, 0.12);
}

.fact-item span {
  display: block;
  margin-bottom: 6px;
  color: #7a8d83;
  font-size: 12px;
}

.fact-item strong {
  color: #284433;
  font-size: 15px;
}

.quantity-panel {
  margin-bottom: 22px;
}

.quantity-label {
  display: block;
  margin-bottom: 12px;
  color: #5f7669;
  font-size: 14px;
  font-weight: 600;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 16px;
}

.inventory-tip {
  color: #74867c;
  font-size: 13px;
}

.product-actions {
  display: flex;
  gap: 14px;
}

.primary-action {
  min-width: 180px;
}

.detail-extra-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
  margin: 22px 0;
}

.info-card {
  padding: 22px;
  border-radius: 22px;
}

.card-label {
  display: inline-flex;
  margin-bottom: 12px;
  padding: 6px 12px;
  border-radius: 999px;
  background: #eef7ef;
  color: #2f8450;
  font-size: 12px;
  font-weight: 700;
}

.info-card h3 {
  margin: 0 0 10px;
  color: #1f3d2a;
  font-size: 22px;
}

.info-card p {
  margin: 0;
  color: #647a6d;
  line-height: 1.8;
}

.product-tabs-card {
  padding: 22px 24px;
  border-radius: 26px;
}

.product-detail-content,
.product-specs-content {
  padding: 10px 0 4px;
}

.product-detail-content :deep(img) {
  max-width: 100%;
  margin: 12px 0;
  border-radius: 18px;
}

.product-detail-content :deep(p),
.product-detail-content :deep(li) {
  color: #495d53;
  line-height: 1.95;
}

.product-specs-content table {
  width: 100%;
  border-collapse: collapse;
  overflow: hidden;
  border-radius: 18px;
  border-style: hidden;
  box-shadow: 0 0 0 1px #e8eee9;
}

.product-specs-content th,
.product-specs-content td {
  padding: 14px 16px;
  border: 1px solid #e8eee9;
  text-align: left;
}

.product-specs-content th {
  width: 180px;
  background: #f8fbf7;
  color: #63796d;
  font-weight: 600;
}

@media (max-width: 1100px) {
  .product-main-card,
  .detail-extra-grid {
    grid-template-columns: 1fr;
  }

  .image-stage {
    min-height: 420px;
  }
}

@media (max-width: 768px) {
  .product-detail-page {
    padding: 16px 14px 34px;
  }

  .product-main-card,
  .product-tabs-card,
  .info-card {
    border-radius: 20px;
  }

  .product-main-card {
    padding: 18px;
  }

  .product-title {
    font-size: 28px;
  }

  .price-main,
  .quantity-control,
  .product-actions,
  .product-facts {
    flex-direction: column;
    align-items: flex-start;
  }

  .product-facts {
    display: grid;
    grid-template-columns: 1fr;
    width: 100%;
  }

  .product-actions {
    width: 100%;
  }

  .product-actions .el-button,
  .primary-action {
    width: 100%;
  }
}
</style>
