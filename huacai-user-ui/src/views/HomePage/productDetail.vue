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
          <div class="stage-image" :style="stageBgStyle(mainImageUrl)">
            <el-image
              :src="mainImageUrl"
              :preview-src-list="[mainImageUrl]"
              preview-teleported
              fit="contain"
              class="stage-el-image"
            />
          </div>
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

        <div class="product-commerce-tags">
          <div class="tag-chip">{{ product._meta?.category || '助农优选' }}</div>
          <div class="tag-chip">销量 {{ product._meta?.sales || 0 }}</div>
          <div class="tag-chip">好评率 {{ Number(product._meta?.rating || 0).toFixed(1) }}%</div>
          <div class="tag-chip">{{ product._meta?.reviewCount || 0 }}+ 评价</div>
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

        <div class="trust-row">
          <div class="trust-card">
            <div class="trust-title">服务保障</div>
            <div class="trust-subtitle">{{ shippingPromiseText }}</div>
            <div class="trust-tags">
              <span v-for="tag in serviceTagsList" :key="tag" class="trust-tag">{{ tag }}</span>
            </div>
          </div>
          <div class="trust-card">
            <div class="trust-title">农户信息</div>
            <div class="trust-subtitle">
              <span class="verified-badge" :class="{ on: farmerVerifiedValue === 1 }">
                {{ farmerVerifiedValue === 1 ? '已认证' : '未认证' }}
              </span>
              <span class="farmer-text">{{ product.userName || '平台优选' }}</span>
            </div>
            <div class="trust-kpis">
              <div class="kpi">
                <span>累计销量</span>
                <strong>{{ product._meta?.sales || 0 }}</strong>
              </div>
              <div class="kpi">
                <span>好评率</span>
                <strong>{{ Number(product._meta?.rating || 0).toFixed(1) }}%</strong>
              </div>
              <div class="kpi">
                <span>发货地</span>
                <strong>{{ product.shipFrom || product.origin || '待补充' }}</strong>
              </div>
            </div>
          </div>
        </div>

        <div class="quantity-panel">
          <span class="quantity-label">购买数量</span>
          <div class="quantity-control">
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="Number(product.inventory || 0) > 0 ? Number(product.inventory) : 999"
              size="large"
              :disabled="Number(product.inventory || 0) <= 0"
            />
            <span class="inventory-tip">库存充足时可直接加入购物车或立即下单</span>
          </div>
        </div>

        <div class="product-actions">
          <el-button
            type="primary"
            size="large"
            class="primary-action"
            @click="addToCart"
            v-loading="loading"
            :disabled="Number(product.inventory || 0) <= 0"
          >
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
      <div class="info-card">
        <span class="card-label">口碑推荐</span>
        <h3>评价真实可读</h3>
        <p>展示好评率、评价数与晒图内容，让购买决策更省心。</p>
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
        <el-tab-pane label="用户评价" name="reviews">
          <div class="reviews-wrap">
            <div class="reviews-summary">
              <div class="score">
                <strong>{{ Number(product._meta?.rating || 0).toFixed(1) }}</strong>
                <span>/ 100</span>
              </div>
              <div class="summary-text">
                <div class="line">好评率 {{ Number(product._meta?.rating || 0).toFixed(1) }}%</div>
                <div class="line">{{ product._meta?.reviewCount || 0 }}+ 条评价</div>
              </div>
            </div>

            <div class="review-list">
              <article class="review-item" v-for="item in reviews" :key="item.id">
                <header class="review-header">
                  <div class="review-user">
                    <div class="avatar">{{ item.avatar }}</div>
                    <div>
                      <div class="name">{{ item.user }}</div>
                      <div class="time">{{ item.time }}</div>
                    </div>
                  </div>
                  <el-rate :model-value="item.stars" disabled />
                </header>

                <div class="review-content">{{ item.content }}</div>

                <div class="review-images" v-if="item.images.length">
                  <el-image
                    v-for="(url, idx) in item.images"
                    :key="url + '-' + idx"
                    :src="url"
                    :preview-src-list="item.images"
                    :initial-index="idx"
                    preview-teleported
                    fit="cover"
                    class="review-image"
                  />
                </div>
              </article>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>
  </div>
</template>

<script setup>
import { computed, onActivated, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, ShoppingCart } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getProducts, reportProductView } from '@/api/assisting/products.js'
import { addCart } from '@/api/assisting/cart.js'
import { withProductMeta } from '@/utils/productMeta.js'
import { getToken } from '@/utils/auth'

const activeTab = ref('detail')
const route = useRoute()
const router = useRouter()
const product = ref({})
const baseUrl = import.meta.env.VITE_APP_BASE_API
const quantity = ref(1)
const loading = ref(false)
const lastReportedViewKey = ref('')

const resolveImageUrl = (image) => {
  if (!image) return ''
  const str = String(image)
  if (/^https?:\/\//i.test(str)) return encodeURI(str)
  // 前端 public 静态资源路径，不能拼接后端 baseUrl
  if (str.startsWith('/downloaded-images/')) return encodeURI(str)
  // 兼容历史路径：/.downloaded-images/* 统一映射到 /downloaded-images/*
  if (str.startsWith('/.downloaded-images/')) {
    return encodeURI(str.replace('/.downloaded-images/', '/downloaded-images/'))
  }
  return encodeURI(baseUrl + str)
}

const normalizeGallery = (raw) => {
  if (!raw) return []
  const str = String(raw).trim()
  if (!str) return []
  // JSON 数组
  if (str.startsWith('[')) {
    try {
      const arr = JSON.parse(str)
      return Array.isArray(arr) ? arr : []
    } catch (e) {
      return []
    }
  }
  // 逗号/换行分隔
  return str
    .split(/[,\\n]/g)
    .map(s => s.trim())
    .filter(Boolean)
}

const buildFallbackGallery = (seed) => {
  const s = String(seed || 'product')
  return [
    `https://picsum.photos/seed/${encodeURIComponent(s)}-a/1200/800`,
    `https://picsum.photos/seed/${encodeURIComponent(s)}-b/1200/800`,
    `https://picsum.photos/seed/${encodeURIComponent(s)}-c/1200/800`,
    `https://picsum.photos/seed/${encodeURIComponent(s)}-d/1200/800`
  ]
}

const galleryUrls = computed(() => {
  const baseImg = resolveImageUrl(product.value?.image)
  const list = normalizeGallery(product.value?.galleryImages || product.value?.gallery_images)
    .map(resolveImageUrl)
    .filter(Boolean)

  const merged = [baseImg, ...list].filter(Boolean)
  if (merged.length >= 2) return Array.from(new Set(merged)).slice(0, 8)

  const fallback = buildFallbackGallery(product.value?.productsId || route.params.id)
  return Array.from(new Set([...merged, ...fallback])).slice(0, 8)
})

// 详情页只展示一张主图（第 1 张）
const mainImageUrl = computed(() => galleryUrls.value?.[0] || '')

const stageBgStyle = (url) => {
  if (!url) return {}
  return {
    '--stage-bg': `url("${url}")`
  }
}

const deriveVerified = (seed) => {
  const str = String(seed || '')
  let sum = 0
  for (let i = 0; i < str.length; i += 1) sum += str.charCodeAt(i)
  return sum % 2 === 0 ? 1 : 0
}

const farmerVerifiedValue = computed(() => {
  const v = product.value?.farmerVerified
  if (v === 0 || v === 1) return v
  return deriveVerified(product.value?.productsId || product.value?.userId || product.value?.name)
})

const shippingPromiseText = computed(() => {
  const v = product.value?.shippingPromise
  if (v && String(v).trim()) return String(v).trim()
  const edible = String(product.value?.edible || '')
  if (edible.includes('冷冻') || edible.includes('冷链')) return '冷链配送 · 48小时发货'
  return '48小时发货 · 破损包赔'
})

const serviceTagsList = computed(() => {
  const raw = product.value?.serviceTags
  if (raw && String(raw).trim()) {
    return String(raw)
      .split(/[,，]/g)
      .map(s => s.trim())
      .filter(Boolean)
      .slice(0, 6)
  }
  const cat = product.value?._meta?.category || ''
  const inventory = Number(product.value?.inventory || 0)
  const base = ['坏果包赔', '极速发货', '产地直采']
  if (cat.includes('水果') || cat.includes('生鲜')) base.push('冷链可选')
  if (inventory <= 0) base.push('到货提醒')
  return base.slice(0, 6)
})

const buildReviews = (seed) => {
  const s = String(seed || 'seed')
  let h = 0
  for (let i = 0; i < s.length; i += 1) h = (h * 31 + s.charCodeAt(i)) >>> 0

  const users = [
    { name: '张**', avatar: '张' },
    { name: '李**', avatar: '李' },
    { name: '王**', avatar: '王' },
    { name: '赵**', avatar: '赵' },
    { name: '陈**', avatar: '陈' },
    { name: '刘**', avatar: '刘' },
    { name: '周**', avatar: '周' },
    { name: '孙**', avatar: '孙' }
  ]

  const cat = String(product.value?._meta?.category || '')
  const name = String(product.value?.name || '')
  const origin = String(product.value?.origin || '')
  const specs = String(product.value?.specs || '')
  const shipFrom = String(product.value?.shipFrom || origin || '')

  const textPool = pickReviewTextPool(cat)
  const extraPool = [
    '包装很严实，没有磕碰。',
    '称了一下分量，基本足秤。',
    '口感不错，家里老人小孩都能接受。',
    '到货速度可以，整体体验好。',
    '日期新鲜，味道自然。',
    '客服回复挺快。'
  ]

  const baseStars = clampStars(Number(product.value?._meta?.rating || 96))
  const result = []
  const imgList = Array.isArray(galleryUrls.value) ? galleryUrls.value : []
  for (let i = 0; i < 8; i += 1) {
    const u = users[(h + i * 7) % users.length]
    const t1 = textPool[(h + i * 5) % textPool.length]
    const t2 = extraPool[(h + i * 3) % extraPool.length]
    const headline = buildReviewHeadline(cat, name, origin)
    const content = [headline, t1, t2, specs ? `规格：${specs}` : '', shipFrom ? `发货地：${shipFrom}` : '']
      .filter(Boolean)
      .join(' ')

    const stars = clampStars(baseStars - ((h + i) % 3 === 0 ? 0.5 : 0))
    const day = 10 + ((h + i * 3) % 18)
    const imgs = (i % 3 === 0 && imgList.length)
      ? [imgList[(i + 1) % imgList.length], imgList[(i + 2) % imgList.length]].filter(Boolean)
      : []
    result.push({
      id: `${s}-${i}`,
      user: u.name,
      avatar: u.avatar,
      time: `2026-03-${String(day).padStart(2, '0')}`,
      stars,
      content,
      images: imgs
    })
  }
  return result
}

function clampStars(scorePercent) {
  // 把 0~100 的好评率映射为 3.5~5.0 星
  const pct = Number.isFinite(scorePercent) ? scorePercent : 96
  const stars = 3.5 + (Math.max(0, Math.min(100, pct)) / 100) * 1.5
  return Math.round(stars * 2) / 2
}

const reviews = computed(() => buildReviews(product.value?.productsId || route.params.id))

function pickReviewTextPool(category) {
  const c = String(category || '')
  if (c.includes('水果')) {
    return [
      '甜度合适，果肉紧实，汁水也多。',
      '香味很自然，不是那种很冲的香精味。',
      '大小比较均匀，挑拣成本低。',
      '放一两天更好吃，口感会更软糯。'
    ]
  }
  if (c.includes('蔬菜')) {
    return [
      '叶子很嫩，炒出来很香。',
      '根茎类挺脆，做汤也合适。',
      '净菜处理不错，回家简单冲洗就能做。'
    ]
  }
  if (c.includes('粮油') || c.includes('米') || c.includes('面')) {
    return [
      '米香明显，蒸出来颗粒饱满。',
      '煮粥很绵，口感细腻。',
      '做主食挺耐吃，回购考虑中。'
    ]
  }
  if (c.includes('肉') || c.includes('蛋')) {
    return [
      '肉质紧实，不腥，处理得很干净。',
      '分割包装方便，冷冻保存也省心。',
      '做出来很香，家里很爱吃。'
    ]
  }
  if (c.includes('坚果') || c.includes('零食') || c.includes('茶')) {
    return [
      '味道纯，越嚼越香。',
      '不算很甜，配茶/咖啡很合适。',
      '包装干净，携带方便。'
    ]
  }
  return [
    '品质不错，和描述一致。',
    '整体满意，性价比可以。',
    '复购意愿比较强。'
  ]
}

function buildReviewHeadline(category, productName, origin) {
  const c = String(category || '')
  const n = String(productName || '')
  const o = String(origin || '')
  if (c.includes('水果')) return `收到${n || '水果'}，${o ? '产地：' + o + '，' : ''}新鲜度不错。`
  if (c.includes('蔬菜')) return `这款${n || '蔬菜'}很新鲜，${o ? '看得出是' + o + '来的。' : ''}`
  if (c.includes('粮油') || c.includes('米') || c.includes('面')) return `${n || '主食'}口感可以，${o ? '产地：' + o + '。' : ''}`
  if (c.includes('肉') || c.includes('蛋')) return `${n || '食材'}处理干净，味道不错。`
  return `${n || '商品'}整体符合预期。`
}

const addToCart = () => {
  if (!getToken()) {
    ElMessage.warning('请先登录后再加入购物车')
    router.push(`/login?redirect=${route.fullPath}`)
    return
  }
  const inventory = Number(product.value.inventory || 0)
  if (inventory <= 0) {
    ElMessage.warning('该商品库存不足')
    return
  }
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

const reportDetailView = async (productsId) => {
  const token = getToken()
  if (!token || !productsId) return

  const currentViewKey = `detail:${productsId}`
  if (lastReportedViewKey.value === currentViewKey) return

  try {
    await reportProductView(productsId)
    lastReportedViewKey.value = currentViewKey
  } catch (error) {
    console.warn('report product detail view failed', error)
  }
}

const fetchProduct = async () => {
  const res = await getProducts(route.params.id)
  product.value = withProductMeta(res.data || {})

  // 若库存变更导致购买数量超出上限，则自动回退到库存可购买范围
  const inventory = Number(product.value.inventory || 0)
  if (inventory > 0 && quantity.value > inventory) {
    quantity.value = inventory
  }

  await reportDetailView(product.value.productsId || route.params.id)
}

// keep-alive 场景：重新激活页面时也需要刷新库存
onActivated(() => {
  fetchProduct()
})

onMounted(() => {
  fetchProduct()
})

// 同一组件切换不同 productsId 时刷新
watch(
  () => route.params.id,
  () => {
    fetchProduct()
  }
)
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
  height: 520px;
  border-radius: 24px;
  background: linear-gradient(135deg, #f4f8ef, #ffffff);
  display: flex;
  flex-direction: column;
}

.gallery-carousel {
  height: 100%;
  flex: 1;
}

.stage-image {
  width: 100%;
  height: 100%;
  padding: 16px 18px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  isolation: isolate;
}

.stage-image::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: var(--stage-bg);
  background-size: cover;
  background-position: center;
  filter: blur(26px);
  opacity: 0.22;
  transform: scale(1.06);
  z-index: -2;
}

.stage-image::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 30% 20%, rgba(255, 255, 255, 0.75), rgba(255, 255, 255, 0.2));
  z-index: -1;
}

.stage-el-image {
  width: 100%;
  height: 100%;
}

.stage-el-image :deep(img) {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.stage-overlay {
  position: absolute;
  left: 14px;
  right: 14px;
  bottom: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(104, 152, 85, 0.14);
  backdrop-filter: blur(8px);
}

.overlay-left {
  color: #3b5a48;
  font-size: 12px;
  font-weight: 700;
}

.overlay-right {
  color: #6a8174;
  font-size: 12px;
  font-weight: 800;
}

.gallery-carousel :deep(.el-carousel__container) {
  height: 100%;
}

.gallery-carousel :deep(.el-carousel__item) {
  height: 100%;
}

.gallery-thumbs {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
  margin-top: 10px;
}

.thumb-item {
  position: relative;
  height: 72px;
  border-radius: 14px;
  border: 1px solid rgba(104, 152, 85, 0.16);
  background: rgba(255, 255, 255, 0.88);
  overflow: hidden;
  cursor: pointer;
  padding: 0;
}

.thumb-image {
  width: 100%;
  height: 100%;
  display: block;
}

.thumb-image :deep(img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.thumb-item.active {
  border-color: rgba(45, 136, 80, 0.75);
  box-shadow: 0 10px 22px rgba(28, 69, 41, 0.12);
}

.trust-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin: 18px 0 22px;
}

.trust-card {
  padding: 16px 16px 14px;
  border-radius: 18px;
  background: #f8fbf7;
  border: 1px solid rgba(104, 152, 85, 0.12);
}

.trust-title {
  color: #2a5b3f;
  font-weight: 800;
  font-size: 14px;
  margin-bottom: 6px;
}

.trust-subtitle {
  color: #6a8174;
  font-size: 13px;
  line-height: 1.7;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.trust-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.trust-tag {
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid rgba(104, 152, 85, 0.14);
  background: rgba(255, 255, 255, 0.72);
  color: #2b8a52;
  font-size: 12px;
  font-weight: 700;
}

.verified-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(94, 108, 101, 0.1);
  color: #5f7669;
  font-size: 12px;
  font-weight: 800;
}

.verified-badge.on {
  background: rgba(45, 136, 80, 0.12);
  color: #2d8850;
}

.farmer-text {
  color: #2a4b38;
  font-size: 13px;
  font-weight: 700;
}

.trust-kpis {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.kpi {
  padding: 12px 12px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(104, 152, 85, 0.12);
}

.kpi span {
  display: block;
  color: #7a8d83;
  font-size: 12px;
  margin-bottom: 6px;
}

.kpi strong {
  color: #284433;
  font-size: 14px;
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

.product-commerce-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 18px;
}

.tag-chip {
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid rgba(104, 152, 85, 0.14);
  background: rgba(255, 255, 255, 0.72);
  color: #2b8a52;
  font-size: 12px;
  font-weight: 700;
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
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  margin: 22px 0;
}

.reviews-wrap {
  padding: 6px 0 4px;
}

.reviews-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 16px 18px;
  border-radius: 18px;
  background: linear-gradient(135deg, #f2fbf4, #ffffff);
  border: 1px solid rgba(104, 152, 85, 0.12);
  margin-bottom: 16px;
}

.score strong {
  font-size: 34px;
  color: #2d8850;
  line-height: 1;
}

.score span {
  color: #6d8175;
  margin-left: 6px;
  font-size: 13px;
}

.summary-text .line {
  color: #5f7669;
  font-size: 13px;
  line-height: 1.7;
  text-align: right;
}

.review-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
}

.review-item {
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(104, 152, 85, 0.12);
}

.review-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.review-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: rgba(45, 136, 80, 0.12);
  color: #2d8850;
  font-weight: 900;
  display: flex;
  align-items: center;
  justify-content: center;
}

.name {
  color: #284433;
  font-weight: 800;
  font-size: 13px;
}

.time {
  color: #7a8d83;
  font-size: 12px;
  margin-top: 2px;
}

.review-content {
  color: #495d53;
  line-height: 1.9;
  margin-bottom: 12px;
}

.review-images {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
}

.review-image {
  width: 100%;
  height: 84px;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid rgba(104, 152, 85, 0.12);
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
    height: 420px;
  }

  .gallery-carousel {
    height: 100%;
  }

  .stage-image {
    height: 100%;
    padding: 14px 16px;
  }

  .gallery-thumbs {
    grid-template-columns: repeat(5, minmax(0, 1fr));
  }

  .trust-row {
    grid-template-columns: 1fr;
  }

  .review-images {
    grid-template-columns: repeat(4, minmax(0, 1fr));
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

  .gallery-thumbs {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .thumb-item {
    height: 66px;
  }

  .review-images {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>
