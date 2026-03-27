<template>
  <div class="home-content">
    <div class="main-content">
      <section class="banner-section">
        <el-carousel
          height="500px"
          :interval="5000"
          arrow="always"
          indicator-position="outside"
        >
          <el-carousel-item v-for="item in bannerList" :key="item.bannerId">
            <div class="banner-item" :style="{ backgroundImage: `url(${item.image})` }">
              <div class="banner-overlay" />
              <div class="banner-content">
                <span class="banner-badge">助农扶贫商城</span>
                <h2 class="banner-title">{{ item.title }}</h2>
                <p class="banner-description">{{ item.description }}</p>
                <div class="banner-actions">
                  <el-button class="banner-btn banner-btn-primary" type="primary" size="large" @click.stop="goToProducts">
                    选购助农产品
                  </el-button>
                  <el-button class="banner-btn banner-btn-secondary" plain size="large" @click.stop="goToPolicies">
                    了解扶持政策
                  </el-button>
                </div>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </section>

      <section class="value-strip">
        <div class="value-card">
          <span class="value-label">源头助农</span>
          <strong>优选产地与农户资源</strong>
          <p>围绕特色产地和农户主体组织商品展示，突出助农商城的真实来源与平台可信度。</p>
        </div>
        <div class="value-card">
          <span class="value-label">政策联动</span>
          <strong>同步展示扶持政策信息</strong>
          <p>将商品展示与助农政策信息联动呈现，便于用户了解补贴、培训、营销等支持方向。</p>
        </div>
        <div class="value-card">
          <span class="value-label">交易闭环</span>
          <strong>覆盖浏览下单与订单跟踪</strong>
          <p>围绕商品浏览、订单支付与订单管理构建完整流程，提升商城功能完整性与使用体验。</p>
        </div>
      </section>

      <section class="section product-section">
        <div class="product-header">
          <div>
            <p class="section-kicker">精选好货</p>
            <h2 class="section-title">助农产品</h2>
            <p class="section-subtitle">
              精选优质农产品，帮助优质产地与消费者更高效地建立连接。
            </p>
          </div>
          <el-link class="product-link" type="primary" :underline="false" @click="goToProducts">
            查看更多
            <el-icon><ArrowRight /></el-icon>
          </el-link>
        </div>

        <div class="product-panel">
          <div class="product-list">
            <el-row :gutter="20">
              <el-col :span="6" v-for="item in productsList" :key="item.productsId">
                <div class="product-card" @click="goToProductDetail(item.productsId)">
                  <div class="product-image">
                    <img :src="resolveImageUrl(item.image)" alt="">
                    <div class="product-tag">产地直连</div>
                  </div>
                  <div class="product-info">
                    <h3 class="product-name">{{ item.name }}</h3>
                    <p class="product-origin">产地：{{ item.origin || '优质农产基地' }}</p>
                    <div class="product-meta">
                      <div class="product-price">
                        <span class="current-price">¥{{ item.price }}</span>
                      </div>
                      <span class="product-action">查看详情</span>
                    </div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </div>
      </section>

      <section class="section policy-section">
        <div class="section-header">
          <div>
            <p class="section-kicker">政策速览</p>
            <h2 class="section-title">助农政策与扶持信息</h2>
            <p class="section-subtitle">
              汇总电商助农、农产品流通、补贴扶持等常见政策方向，提升平台展示的专业度与实用性。
            </p>
          </div>
          <el-link type="primary" :underline="false" @click="goToPolicies">
            进入政策中心
            <el-icon><ArrowRight /></el-icon>
          </el-link>
        </div>

        <div class="policy-preview" v-if="policiesList.length">
          <div class="policy-featured" @click="goToPolicy(policiesList[0])">
            <div class="featured-topline">
              <span class="featured-badge">{{ policiesList[0].category }}</span>
              <span class="featured-region">{{ policiesList[0].region }}</span>
            </div>
            <h3 class="featured-title">{{ policiesList[0].title }}</h3>
            <p class="featured-summary">{{ policiesList[0].summary }}</p>
            <div class="featured-meta">
              <span>{{ policiesList[0].publisher }}</span>
              <span>{{ formatDate(policiesList[0].createTime) }}</span>
            </div>
          </div>

          <div class="policy-side-list">
            <div
              v-for="item in policiesList.slice(1)"
              :key="item.policiesId"
              class="policy-mini-card"
              @click="goToPolicy(item)"
            >
              <div class="policy-mini-top">
                <span class="policy-mini-tag">{{ item.category }}</span>
                <span class="policy-mini-date">{{ formatDate(item.createTime) }}</span>
              </div>
              <h4 class="policy-mini-title">{{ item.title }}</h4>
              <p class="policy-mini-summary">{{ item.summary }}</p>
            </div>
          </div>
        </div>

        <div v-else class="policy-empty">
          <div class="empty-state-card">
            <el-empty description="暂未查询到政策内容">
              <template #description>
                <div class="empty-state-text">
                  <strong>政策内容暂未加载</strong>
                  <p>可以先浏览助农产品，或稍后进入政策中心查看更新内容。</p>
                </div>
              </template>
              <el-button type="primary" @click="goToProducts">浏览产品</el-button>
            </el-empty>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import { listBanner } from '@/api/assisting/banner.js'
import { selectList } from '@/api/assisting/products.js'
import { listPolicies } from '@/api/assisting/policies.js'

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

const bannerList = ref([])
const productsList = ref([])
const policiesList = ref([])

const bannerQuery = ref({
  pageNum: 1,
  pageSize: 5
})

const productsQuery = ref({
  pageNum: 1,
  pageSize: 4
})

const policiesQuery = ref({
  pageNum: 1,
  pageSize: 3
})

const bannerEnhancements = [
  {
    title: '优质农产直连餐桌',
    description: '依托助农电商平台促进产销衔接，让更多优质农产品从田间地头走向千家万户。'
  },
  {
    title: '消费帮扶助力乡村振兴',
    description: '围绕消费帮扶与乡村振兴协同发力，拓宽特色农产品销路，带动农户稳定增收。'
  },
  {
    title: '数商兴农激活乡村活力',
    description: '结合电商推广、品牌展示与线上交易服务，提升农产品流通效率，增强助农服务能力。'
  }
]

const decorateBanner = (item, index) => {
  const enhancement = bannerEnhancements[index]
  if (!enhancement) {
    return {
      ...item,
      image: baseUrl + item.image
    }
  }

  return {
    ...item,
    title: enhancement.title,
    description: enhancement.description,
    image: baseUrl + item.image
  }
}

const goToProducts = () => {
  router.push('/index/products')
}

const goToProductDetail = (productsId) => {
  router.push(`/index/productDetail/${productsId}`)
}

const goToPolicies = () => {
  router.push('/index/policies')
}

const goToPolicy = (policy) => {
  router.push({
    path: '/index/policies',
    query: {
      title: policy.title,
      category: policy.category
    }
  })
}

const formatDate = (value) => {
  if (!value) return '待更新'
  return String(value).slice(0, 10)
}

const getList = () => {
  listBanner(bannerQuery.value).then((res) => {
    bannerList.value = (res.rows || []).map((item, index) => decorateBanner(item, index))
  })

  selectList(productsQuery.value).then((res) => {
    productsList.value = (res.rows || []).map(item => ({
      ...item,
      image: resolveImageUrl(item.image)
    }))
  })

  listPolicies(policiesQuery.value).then((res) => {
    policiesList.value = res.rows
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.main-content {
  width: 100%;
  max-width: 1450px;
  margin: 0 auto;
  padding: 24px 20px 60px;
}

.banner-section {
  margin-bottom: 56px;
}

:deep(.banner-section .el-carousel__container) {
  border-radius: 22px;
}

:deep(.banner-section .el-carousel__arrow) {
  width: 42px;
  height: 42px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(6px);
}

:deep(.banner-section .el-carousel__indicator-button) {
  width: 24px;
  height: 6px;
  border-radius: 999px;
}

.banner-item {
  position: relative;
  height: 100%;
  overflow: hidden;
  border-radius: 18px;
  background-position: center;
  background-size: cover;
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, rgba(12, 43, 25, 0.82) 0%, rgba(12, 43, 25, 0.45) 42%, rgba(12, 43, 25, 0.1) 100%),
    linear-gradient(180deg, rgba(255, 255, 255, 0) 0%, rgba(12, 43, 25, 0.14) 100%);
}

.banner-content {
  position: absolute;
  left: 70px;
  bottom: 74px;
  z-index: 2;
  max-width: 620px;
  color: #fff;
}

.banner-badge {
  display: inline-flex;
  margin-bottom: 18px;
  padding: 7px 14px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.24);
  background: rgba(255, 255, 255, 0.14);
  font-size: 13px;
  letter-spacing: 1px;
}

.banner-title {
  margin: 0 0 18px;
  font-size: 44px;
  line-height: 1.18;
  font-weight: 700;
}

.banner-description {
  margin: 0 0 28px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 17px;
  line-height: 1.8;
}

.banner-actions {
  display: flex;
  gap: 12px;
}

.banner-btn {
  min-width: 156px;
  height: 46px;
  border-radius: 999px;
  font-weight: 600;
  letter-spacing: 0.02em;
  box-shadow: 0 14px 28px rgba(15, 43, 27, 0.16);
}

.banner-btn-primary {
  border-color: rgba(255, 255, 255, 0.18);
}

.banner-btn-secondary {
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.28);
  backdrop-filter: blur(6px);
}

.banner-btn-secondary:hover,
.banner-btn-secondary:focus {
  color: #173924;
  background: rgba(255, 255, 255, 0.96);
  border-color: rgba(255, 255, 255, 0.96);
}

.value-strip {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  margin: -24px 0 56px;
}

.value-card {
  padding: 24px 24px 22px;
  border: 1px solid rgba(103, 150, 85, 0.14);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 18px 40px rgba(26, 69, 42, 0.08);
  backdrop-filter: blur(8px);
}

.value-label {
  display: inline-flex;
  margin-bottom: 12px;
  padding: 6px 12px;
  border-radius: 999px;
  background: #eef8f0;
  color: #2d8450;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.value-card strong {
  display: block;
  margin-bottom: 10px;
  color: #1e3c2a;
  font-size: 22px;
}

.value-card p {
  margin: 0;
  color: #667a6d;
  font-size: 14px;
  line-height: 1.8;
}

.section {
  margin-bottom: 56px;
}

.product-section,
.policy-section {
  position: relative;
  overflow: hidden;
}

.product-section {
  padding-top: 10px;
}

.section-header {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 28px;
}

.section-kicker {
  margin: 0 0 8px;
  color: #2b8a52;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 1px;
}

.section-title {
  margin: 0 0 10px;
  color: #213547;
  font-size: 32px;
  line-height: 1.2;
}

.section-subtitle {
  margin: 0;
  max-width: 680px;
  color: #627079;
  font-size: 15px;
  line-height: 1.8;
}

.product-list {
  margin-top: 0;
}

.product-header {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: -16px;
  padding: 0 20px;
}

.product-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 10px 24px rgba(18, 56, 35, 0.08);
}

.product-panel {
  padding: 50px 22px 18px;
  border: 1px solid #e6efe7;
  border-radius: 28px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(248, 252, 249, 0.98)),
    radial-gradient(circle at top left, rgba(70, 160, 102, 0.06), transparent 34%);
  box-shadow: 0 18px 42px rgba(24, 73, 43, 0.06);
}

.product-section::before,
.policy-section::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: 28px;
  pointer-events: none;
}

.product-section::before {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.62), rgba(255, 255, 255, 0.14));
}

.product-card {
  margin-bottom: 12px;
  overflow: hidden;
  border: 1px solid #e7efe8;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 16px 36px rgba(18, 56, 35, 0.06);
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 22px 40px rgba(18, 56, 35, 0.12);
}

.product-image {
  position: relative;
  height: 220px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.product-card:hover .product-image img {
  transform: scale(1.06);
}

.product-tag {
  position: absolute;
  top: 16px;
  left: 16px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(22, 119, 72, 0.9);
  color: #fff;
  font-size: 12px;
}

.product-info {
  padding: 18px 18px 20px;
}

.product-name {
  margin: 0 0 10px;
  color: #213547;
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-origin {
  margin: 0 0 16px;
  color: #74818a;
  font-size: 13px;
}

.product-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.current-price {
  color: #e25b3d;
  font-size: 21px;
  font-weight: 700;
}

.product-action {
  color: #2b8a52;
  font-size: 13px;
  font-weight: 600;
}

.policy-section {
  padding: 34px;
  border: 1px solid #e6efe7;
  border-radius: 24px;
  background:
    radial-gradient(circle at top left, rgba(70, 160, 102, 0.12), transparent 30%),
    linear-gradient(180deg, #f8fdf9 0%, #ffffff 100%);
  box-shadow: 0 18px 42px rgba(24, 73, 43, 0.06);
}

.policy-preview {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 20px;
}

.policy-featured {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 260px;
  padding: 26px;
  border-radius: 22px;
  background: linear-gradient(145deg, #1f5c38 0%, #2f7d4e 70%, #4ea56d 100%);
  color: #fff;
  box-shadow: 0 22px 40px rgba(22, 87, 50, 0.18);
  cursor: pointer;
}

.featured-topline,
.featured-meta,
.policy-mini-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.featured-badge,
.policy-mini-tag {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
}

.featured-badge {
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(255, 255, 255, 0.16);
}

.featured-region,
.featured-meta {
  color: rgba(255, 255, 255, 0.82);
  font-size: 13px;
}

.featured-title {
  margin: 20px 0 14px;
  font-size: 28px;
  line-height: 1.35;
}

.featured-summary {
  margin: 0;
  color: rgba(255, 255, 255, 0.92);
  line-height: 1.85;
}

.policy-side-list {
  display: grid;
  gap: 16px;
}

.policy-mini-card {
  padding: 20px 22px;
  border: 1px solid #dfebdf;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 14px 24px rgba(27, 78, 47, 0.06);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.policy-mini-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 28px rgba(27, 78, 47, 0.1);
}

.policy-mini-tag {
  background: #edf7f0;
  color: #287548;
  font-weight: 600;
}

.policy-mini-date {
  color: #7b8790;
  font-size: 12px;
}

.policy-mini-title {
  margin: 14px 0 10px;
  color: #213547;
  font-size: 18px;
  line-height: 1.45;
}

.policy-mini-summary {
  margin: 0;
  color: #627079;
  font-size: 14px;
  line-height: 1.8;
}

.policy-empty {
  padding: 12px 0;
}

.empty-state-card {
  padding: 12px;
  border: 1px dashed rgba(92, 153, 88, 0.22);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.72);
}

.empty-state-text strong {
  display: block;
  margin-bottom: 6px;
  color: #254431;
  font-size: 18px;
}

.empty-state-text p {
  margin: 0;
  color: #6d8177;
  line-height: 1.8;
}

@media (max-width: 1200px) {
  .banner-content {
    left: 48px;
    bottom: 60px;
    max-width: 540px;
  }

  .banner-title {
    font-size: 38px;
  }
}

@media (max-width: 992px) {
  .value-strip {
    grid-template-columns: 1fr;
    margin-top: 0;
  }

  .policy-preview {
    grid-template-columns: 1fr;
  }

  .product-header,
  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .main-content {
    padding: 16px 14px 40px;
  }

  .value-card {
    padding: 20px 18px;
  }

  .banner-content {
    left: 24px;
    right: 24px;
    bottom: 34px;
    max-width: none;
  }

  .banner-title {
    font-size: 30px;
  }

  .banner-description {
    font-size: 14px;
  }

  .banner-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .policy-section {
    padding: 22px 18px;
  }

  .product-header {
    margin-bottom: 12px;
    padding: 0;
  }

  .product-panel {
    padding: 22px 16px 12px;
  }
}
</style>





