<template>
  <div class="policies-page">
    <section class="hero-section">
      <div class="hero-copy">
        <span class="hero-badge">助农信息服务</span>
        <h1>助农政策查询</h1>
        <p>
          汇集补贴、营销、培训、金融等方向的助农政策信息，
          帮助用户快速了解政策重点、适用地区和申报联系渠道。
        </p>
        <div class="hero-search">
          <el-input
            v-model="queryParams.title"
            clearable
            placeholder="搜索政策标题、关键词"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="success" @click="handleSearch">搜索政策</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>
      <div class="hero-stats">
        <div class="stat-card">
          <span class="stat-label">当前结果</span>
          <strong>{{ total }}</strong>
        </div>
        <div class="stat-card">
          <span class="stat-label">政策分类</span>
          <strong>{{ categoryCount }}</strong>
        </div>
        <div class="stat-card">
          <span class="stat-label">涉及地区</span>
          <strong>{{ regionCount }}</strong>
        </div>
      </div>
    </section>

    <section class="policies-layout">
      <aside class="policies-sidebar">
        <div class="side-card">
          <div class="side-card-header">
            <h3>政策分类</h3>
            <span>{{ activeCategoryLabel }}</span>
          </div>
          <div class="category-list">
            <button
              v-for="item in categoryOptions"
              :key="item.value || 'all'"
              class="category-item"
              :class="{ active: queryParams.category === item.value }"
              @click="handleCategorySelect(item.value)"
            >
              {{ item.label }}
            </button>
          </div>
        </div>

        <div class="side-card guide-card">
          <div class="side-card-header">
            <h3>申报提示</h3>
          </div>
          <div class="guide-list">
            <div class="guide-item">
              <span class="guide-index">01</span>
              <div>
                <h4>先看适用地区</h4>
                <p>优先确认政策对应的省、市、区县和适用对象。</p>
              </div>
            </div>
            <div class="guide-item">
              <span class="guide-index">02</span>
              <div>
                <h4>再看申报条件</h4>
                <p>重点查看主体资格、规模要求、时间节点和材料清单。</p>
              </div>
            </div>
            <div class="guide-item">
              <span class="guide-index">03</span>
              <div>
                <h4>最后联系主管部门</h4>
                <p>优先查看负责部门、联系人和电话等关键信息。</p>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <main class="policies-main">
        <div v-if="featuredPolicy" class="featured-policy">
          <div class="featured-top">
            <div>
              <span class="featured-kicker">重点推荐</span>
              <h2>{{ featuredPolicy.title }}</h2>
            </div>
            <el-button class="featured-action" type="success" plain @click="showDetail(featuredPolicy)">查看详情</el-button>
          </div>
          <p class="featured-summary">{{ featuredPolicy.summary }}</p>
          <div class="featured-meta">
            <span>
              <el-icon><Calendar /></el-icon>
              {{ featuredPolicy.createTime }}
            </span>
            <span>
              <el-icon><Location /></el-icon>
              {{ featuredPolicy.region }}
            </span>
            <span>
              <el-icon><OfficeBuilding /></el-icon>
              {{ featuredPolicy.publisher }}
            </span>
          </div>
        </div>

        <div class="list-toolbar">
          <div class="toolbar-copy">
            <h3>{{ activeCategoryLabel }}</h3>
            <p>查看平台收录的政策摘要、适用地区与申报信息</p>
            <div v-if="hasActiveFilters" class="active-filters">
              <span v-if="queryParams.title" class="filter-chip">
                关键词：{{ queryParams.title }}
              </span>
              <span v-if="queryParams.category" class="filter-chip">
                分类：{{ activeCategoryLabel }}
              </span>
              <button type="button" class="clear-filter-btn" @click="handleReset">清空筛选</button>
            </div>
          </div>
          <span class="toolbar-count">共 {{ total }} 条</span>
        </div>

        <div class="policies-grid" v-loading="loading">
          <template v-if="policyCards.length > 0">
            <article
              v-for="policy in policyCards"
              :key="policy.policiesId"
              :class="['policy-card', `tone-${getPolicyTone(policy.category)}`]"
            >
              <div class="policy-card-top">
                <span :class="['category-pill', `tone-${getPolicyTone(policy.category)}`]">
                  <dict-tag :options="policies_category" :value="policy.category" />
                </span>
                <span class="region-tag">{{ policy.region }}</span>
              </div>
              <h3 @click="showDetail(policy)">{{ policy.title }}</h3>
              <p class="policy-summary">{{ policy.summary }}</p>
              <div class="policy-meta">
                <span>
                  <el-icon><OfficeBuilding /></el-icon>
                  {{ policy.publisher }}
                </span>
                <span>
                  <el-icon><Calendar /></el-icon>
                  {{ policy.createTime }}
                </span>
              </div>
              <div class="policy-actions">
                <el-button class="policy-action-btn" text type="success" @click="showDetail(policy)">查看详情</el-button>
              </div>
            </article>
          </template>

          <div v-else-if="!featuredPolicy" class="empty-policies">
            <div class="empty-state-card">
              <el-empty description="暂无相关政策内容">
                <template #description>
                  <div class="empty-state-text">
                    <strong>当前没有匹配的政策信息</strong>
                    <p>可以重置搜索条件，或回到全部分类查看平台整理的政策摘要。</p>
                  </div>
                </template>
                <el-button type="success" @click="handleReset">重置筛选</el-button>
              </el-empty>
            </div>
          </div>

          <div v-else class="single-policy-tip">
            <div class="single-policy-card">
              <strong>当前筛选结果仅展示 1 条重点政策</strong>
              <p>可以切换分类或清空关键词，继续查看平台收录的更多政策信息。</p>
              <el-button type="success" plain @click="handleReset">查看全部政策</el-button>
            </div>
          </div>
        </div>

        <div class="policies-pagination">
          <pagination
            v-show="total > 0"
            :total="total"
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            :pageSizes="[4, 6, 8, 10]"
            @pagination="getList"
          />
        </div>
      </main>
    </section>

    <vxe-modal
      v-model="open"
      :title="currentPolicy.title || '政策详情'"
      :width="modalWidth"
      :height="modalHeight"
      showFooter
      resize
    >
      <div class="detail-shell">
        <div class="detail-banner">
          <div class="detail-banner-top">
            <span :class="['category-pill', 'detail-category-pill', `tone-${getPolicyTone(currentPolicy.category)}`]">
              <dict-tag :options="policies_category" :value="currentPolicy.category" />
            </span>
            <el-link
              v-if="currentPolicy.sourceLink"
              :href="currentPolicy.sourceLink"
              target="_blank"
              type="success"
              :underline="false"
              class="detail-source-link"
            >
              查看政策原文
            </el-link>
          </div>
          <h2>{{ currentPolicy.title }}</h2>
          <p class="detail-summary">{{ currentPolicy.summary }}</p>
          <div class="detail-meta-grid">
            <div class="meta-card">
              <span>发布时间</span>
              <strong>{{ currentPolicy.createTime || '待补充' }}</strong>
            </div>
            <div class="meta-card">
              <span>适用地区</span>
              <strong>{{ currentPolicy.region || '待补充' }}</strong>
            </div>
            <div class="meta-card">
              <span>发布单位</span>
              <strong>{{ currentPolicy.publisher || '待补充' }}</strong>
            </div>
          </div>
        </div>

        <div class="detail-content-wrap">
          <section class="detail-article-card">
            <div class="detail-section-head">
              <div class="detail-section-label">政策正文</div>
              <span class="detail-section-note">建议重点关注申报对象、时间节点和材料要求</span>
            </div>
            <div class="detail-content" v-html="currentPolicy.content || '<p>暂无政策正文内容</p>'"></div>
          </section>

          <aside class="detail-side">
            <div class="detail-side-card detail-side-summary">
              <h4>配套信息</h4>
              <div class="detail-info-list">
                <div class="detail-info-item">
                  <span>负责部门</span>
                  <strong>{{ currentPolicy.department || '待补充' }}</strong>
                </div>
                <div class="detail-info-item">
                  <span>联系人</span>
                  <strong>{{ currentPolicy.contactPerson || '待补充' }}</strong>
                </div>
                <div class="detail-info-item">
                  <span>联系电话</span>
                  <strong>{{ currentPolicy.phone || '待补充' }}</strong>
                </div>
                <div class="detail-info-item">
                  <span>联系地址</span>
                  <strong>{{ currentPolicy.address || '待补充' }}</strong>
                </div>
              </div>
            </div>
          </aside>
        </div>
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
import { computed, getCurrentInstance, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Calendar, Location, OfficeBuilding, Search } from '@element-plus/icons-vue'
import { listPolicies } from '@/api/assisting/policies.js'

const route = useRoute()
const { proxy } = getCurrentInstance()
const { policies_category } = proxy.useDict('policies_category')

const open = ref(false)
const currentPolicy = ref({})
const loading = ref(false)
const total = ref(0)
const policiesList = ref([])
const viewportWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1440)

const queryParams = ref({
  pageNum: 1,
  pageSize: 6,
  title: '',
  category: ''
})

const categoryOptions = computed(() => [
  { label: '全部政策', value: '' },
  ...(policies_category.value || [])
])

const activeCategoryLabel = computed(() => {
  return categoryOptions.value.find(item => item.value === queryParams.value.category)?.label || '全部政策'
})

const featuredPolicy = computed(() => policiesList.value[0] || null)
const policyCards = computed(() => (featuredPolicy.value ? policiesList.value.slice(1) : []))
const categoryCount = computed(() => Math.max(categoryOptions.value.length - 1, 0))
const regionCount = computed(() => new Set(policiesList.value.map(item => item.region).filter(Boolean)).size)
const hasActiveFilters = computed(() => Boolean(queryParams.value.title || queryParams.value.category))
const modalWidth = computed(() => (viewportWidth.value <= 768 ? '94%' : viewportWidth.value <= 1200 ? '78%' : '64%'))
const modalHeight = computed(() => (viewportWidth.value <= 768 ? '88vh' : '82vh'))

const initQueryFromRoute = () => {
  queryParams.value.title = route.query.title || ''
  queryParams.value.category = route.query.category || ''
  queryParams.value.pageNum = 1
}

const getList = () => {
  loading.value = true
  listPolicies(queryParams.value)
    .then(res => {
      policiesList.value = res.rows || []
      total.value = res.total || 0
    })
    .finally(() => {
      loading.value = false
    })
}

const showDetail = policy => {
  currentPolicy.value = policy
  open.value = true
}

const handleCategorySelect = value => {
  queryParams.value.category = value
  queryParams.value.pageNum = 1
  getList()
}

const handleSearch = () => {
  queryParams.value.pageNum = 1
  getList()
}

const handleReset = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 6,
    title: '',
    category: ''
  }
  getList()
}

const getPolicyTone = category => {
  const toneMap = {
    营销帮扶: 'marketing',
    技术支持: 'tech',
    教育培训: 'training',
    补贴政策: 'subsidy',
    金融支持: 'finance'
  }
  return toneMap[category] || 'default'
}

const syncViewportWidth = () => {
  viewportWidth.value = window.innerWidth
}

watch(
  () => route.query,
  () => {
    initQueryFromRoute()
    getList()
  }
)

onMounted(() => {
  window.addEventListener('resize', syncViewportWidth)
  initQueryFromRoute()
  getList()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', syncViewportWidth)
})
</script>

<style scoped>
.policies-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(71, 146, 88, 0.08), transparent 30%),
    linear-gradient(180deg, #f6fbf4 0%, #f7f8fa 45%, #ffffff 100%);
  padding: 28px 0 36px;
}

.hero-section,
.policies-layout {
  max-width: 1450px;
  margin: 0 auto;
  width: 100%;
  padding: 0 20px;
}

.hero-section {
  display: grid;
  grid-template-columns: 1.6fr 0.9fr;
  gap: 22px;
  margin-bottom: 24px;
}

.hero-copy,
.hero-stats,
.side-card,
.featured-policy,
.policy-card,
.list-toolbar {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(92, 150, 104, 0.14);
  box-shadow: 0 16px 50px rgba(32, 74, 44, 0.08);
  border-radius: 24px;
}

.hero-copy {
  padding: 32px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(48, 140, 82, 0.1);
  color: #2f7f49;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 16px;
}

.hero-copy h1 {
  margin: 0 0 12px;
  font-size: 38px;
  line-height: 1.2;
  color: #183824;
}

.hero-copy p {
  margin: 0;
  color: #567161;
  line-height: 1.8;
  max-width: 720px;
}

.hero-search {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 12px;
  margin-top: 24px;
}

.hero-stats {
  display: grid;
  gap: 14px;
  padding: 22px;
}

.stat-card {
  padding: 18px 20px;
  border-radius: 18px;
  background: linear-gradient(135deg, #ffffff 0%, #f2fbf2 100%);
  border: 1px solid rgba(54, 131, 72, 0.1);
}

.stat-card strong {
  display: block;
  margin-top: 6px;
  font-size: 28px;
  color: #234e33;
}

.stat-label {
  color: #6a8375;
  font-size: 13px;
}

.policies-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 22px;
}

.policies-sidebar {
  display: grid;
  gap: 18px;
  align-content: start;
  position: sticky;
  top: 118px;
  height: fit-content;
}

.side-card {
  padding: 20px;
}

.side-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.side-card-header h3 {
  margin: 0;
  font-size: 18px;
  color: #254b35;
}

.side-card-header span {
  font-size: 12px;
  color: #7b9285;
}

.category-list {
  display: grid;
  gap: 10px;
}

.category-item {
  width: 100%;
  border: 1px solid rgba(56, 131, 75, 0.14);
  background: #f7fbf7;
  color: #355846;
  border-radius: 14px;
  padding: 12px 14px;
  text-align: left;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.category-item:hover,
.category-item.active {
  background: linear-gradient(135deg, #3d9c60 0%, #2d7f47 100%);
  color: #fff;
  transform: translateY(-1px);
}

.guide-list {
  display: grid;
  gap: 14px;
}

.guide-item {
  display: grid;
  grid-template-columns: 40px 1fr;
  gap: 12px;
  align-items: start;
}

.guide-index {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #eff8ef;
  color: #34804a;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.guide-item h4 {
  margin: 0 0 4px;
  font-size: 15px;
  color: #2a4735;
}

.guide-item p {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: #6a8175;
}

.policies-main {
  display: grid;
  gap: 18px;
}

.featured-policy {
  padding: 24px 26px;
  background: linear-gradient(135deg, rgba(241, 251, 242, 0.95) 0%, rgba(255, 255, 255, 0.96) 100%);
  position: relative;
  overflow: hidden;
}

.featured-policy::after {
  content: "";
  position: absolute;
  right: -60px;
  top: -60px;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(80, 166, 99, 0.16), transparent 68%);
}

.featured-top {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: start;
}

.featured-action {
  min-width: 96px;
}

.featured-kicker {
  display: inline-block;
  margin-bottom: 10px;
  color: #2f8450;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.featured-top h2 {
  margin: 0;
  font-size: 28px;
  line-height: 1.35;
  color: #173524;
}

.featured-summary {
  margin: 16px 0;
  line-height: 1.9;
  color: #4b6758;
}

.featured-meta,
.policy-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  color: #6a8478;
  font-size: 13px;
}

.featured-meta span,
.policy-meta span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.list-toolbar {
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar-copy {
  min-width: 0;
}

.list-toolbar h3 {
  margin: 0 0 4px;
  font-size: 22px;
  color: #1f3f2b;
}

.list-toolbar p {
  margin: 0;
  color: #6b8476;
  font-size: 14px;
}

.active-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}

.filter-chip,
.clear-filter-btn {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 12px;
}

.filter-chip {
  background: rgba(61, 140, 84, 0.1);
  color: #2e7e4d;
  border: 1px solid rgba(61, 140, 84, 0.14);
}

.clear-filter-btn {
  border: 1px dashed rgba(61, 140, 84, 0.3);
  background: #fff;
  color: #2f7f49;
  cursor: pointer;
  transition: all 0.2s ease;
}

.clear-filter-btn:hover {
  border-color: rgba(47, 127, 73, 0.46);
  background: #f7fbf7;
}

.toolbar-count {
  font-size: 14px;
  color: #628274;
  display: inline-flex;
  align-items: center;
  padding: 9px 14px;
  border-radius: 999px;
  background: rgba(243, 248, 244, 0.95);
  border: 1px solid rgba(92, 150, 104, 0.12);
}

.policies-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
  min-height: 260px;
}

.policy-card {
  padding: 22px;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  position: relative;
  overflow: hidden;
  border-color: rgba(92, 150, 104, 0.12);
}

.policy-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 55px rgba(35, 68, 45, 0.12);
}

.policy-card::before {
  content: "";
  position: absolute;
  inset: 0 0 auto 0;
  height: 4px;
  background: linear-gradient(90deg, #5aa36e, #d4b15a);
  opacity: 0.9;
  transition: opacity 0.25s ease, height 0.25s ease;
}

.policy-card:hover::before {
  opacity: 1;
  height: 5px;
}

.policy-card.tone-marketing::before {
  background: linear-gradient(90deg, #469b67, #7bc27a);
}

.policy-card.tone-tech::before {
  background: linear-gradient(90deg, #4c91d9, #7cb5ec);
}

.policy-card.tone-training::before {
  background: linear-gradient(90deg, #8f63d7, #b290f0);
}

.policy-card.tone-subsidy::before {
  background: linear-gradient(90deg, #e09d45, #f0c16b);
}

.policy-card.tone-finance::before {
  background: linear-gradient(90deg, #2e8f8c, #59b7ae);
}

.policy-card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.category-pill {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  border: 1px solid transparent;
}

.category-pill :deep(*) {
  color: inherit !important;
  font-size: inherit !important;
  font-weight: inherit !important;
  line-height: 1 !important;
}

.category-pill.tone-default,
.category-pill.tone-marketing {
  background: rgba(67, 153, 101, 0.12);
  color: #2e7e4d;
  border-color: rgba(67, 153, 101, 0.16);
}

.category-pill.tone-tech {
  background: rgba(76, 145, 217, 0.12);
  color: #356ea9;
  border-color: rgba(76, 145, 217, 0.18);
}

.category-pill.tone-training {
  background: rgba(143, 99, 215, 0.12);
  color: #6f48b1;
  border-color: rgba(143, 99, 215, 0.18);
}

.category-pill.tone-subsidy {
  background: rgba(224, 157, 69, 0.14);
  color: #a76d21;
  border-color: rgba(224, 157, 69, 0.2);
}

.category-pill.tone-finance {
  background: rgba(46, 143, 140, 0.12);
  color: #2c7f7c;
  border-color: rgba(46, 143, 140, 0.18);
}

.region-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: #f4f7f5;
  font-size: 12px;
  color: #688478;
}

.policy-card h3 {
  margin: 0 0 12px;
  font-size: 19px;
  line-height: 1.5;
  color: #183724;
  cursor: pointer;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.policy-card h3:hover {
  color: #2f8850;
}

.policy-summary {
  margin: 0 0 18px;
  color: #5a7365;
  line-height: 1.8;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.policy-actions {
  margin-top: 16px;
}

.policy-action-btn {
  padding: 0;
  font-weight: 600;
  color: #2f8850;
}

.policy-action-btn:hover {
  color: #256e40;
}

.empty-policies {
  grid-column: 1 / -1;
  padding: 60px 0;
}

.single-policy-tip {
  grid-column: 1 / -1;
}

.single-policy-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.76);
  border: 1px dashed rgba(91, 149, 98, 0.24);
}

.single-policy-card strong {
  color: #264532;
  font-size: 16px;
}

.single-policy-card p {
  flex: 1;
  margin: 0;
  color: #6e8277;
  line-height: 1.8;
}

.empty-state-card {
  padding: 14px;
  border-radius: 22px;
  border: 1px dashed rgba(91, 149, 98, 0.24);
  background: rgba(255, 255, 255, 0.72);
}

.empty-state-text strong {
  display: block;
  margin-bottom: 6px;
  color: #264532;
  font-size: 18px;
}

.empty-state-text p {
  margin: 0;
  color: #6e8277;
  line-height: 1.8;
}

.policies-pagination {
  display: flex;
  justify-content: center;
  padding: 8px 0 16px;
}

.detail-shell {
  padding: 4px 4px 14px;
}

.detail-banner {
  padding: 18px 20px 20px;
  border-radius: 22px;
  background: linear-gradient(180deg, rgba(246, 251, 247, 0.96), rgba(255, 255, 255, 0.99));
  border: 1px solid rgba(92, 150, 104, 0.12);
  box-shadow: 0 14px 36px rgba(32, 74, 44, 0.07);
}

.detail-banner-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.detail-source-link {
  display: inline-flex;
  align-items: center;
  padding: 7px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(60, 136, 80, 0.14);
  box-shadow: 0 8px 20px rgba(30, 73, 46, 0.05);
}

.detail-category-pill {
  min-height: 34px;
  padding: 0 14px;
}

.detail-banner h2 {
  margin: 12px 0 8px;
  font-size: 26px;
  line-height: 1.4;
  color: #163522;
}

.detail-summary {
  margin: 0 0 16px;
  color: #587064;
  line-height: 1.8;
}

.detail-meta-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.meta-card {
  padding: 13px 15px;
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(72, 129, 86, 0.12);
}

.meta-card span {
  display: block;
  color: #789082;
  font-size: 12px;
  margin-bottom: 6px;
}

.meta-card strong {
  color: #284534;
  font-size: 14px;
  line-height: 1.6;
}

.detail-content-wrap {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 18px;
  margin-top: 18px;
}

.detail-article-card {
  padding: 22px 24px 24px;
  border-radius: 20px;
  background: linear-gradient(180deg, #ffffff 0%, #fcfefd 100%);
  border: 1px solid rgba(72, 129, 86, 0.12);
  box-shadow: 0 12px 28px rgba(30, 73, 46, 0.05);
}

.detail-section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}

.detail-section-label {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  background: #eef8f0;
  color: #2d8450;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
}

.detail-section-note {
  font-size: 12px;
  color: #7a9083;
}

.detail-content {
  min-width: 0;
  font-size: 14px;
  color: #3f5148;
}

.detail-content :deep(h3),
.detail-content :deep(h4) {
  color: #2f8350;
  margin: 12px 0 8px;
  line-height: 1.5;
}

.detail-content :deep(h3) {
  font-size: 20px;
}

.detail-content :deep(h4) {
  font-size: 17px;
}

.detail-content :deep(p),
.detail-content :deep(li) {
  color: #3f5148;
  line-height: 1.75;
  margin: 0;
}

.detail-content :deep(p + p) {
  margin-top: 6px;
}

.detail-content :deep(ul),
.detail-content :deep(ol) {
  margin: 8px 0 10px;
  padding-left: 22px;
}

.detail-content :deep(ul li),
.detail-content :deep(ol li) {
  margin: 5px 0;
  padding-left: 2px;
}

.detail-content :deep(ul li::marker),
.detail-content :deep(ol li::marker) {
  color: #46885b;
}

.detail-content :deep(h3 + p),
.detail-content :deep(h4 + p),
.detail-content :deep(h3 + ul),
.detail-content :deep(h4 + ul),
.detail-content :deep(h3 + ol),
.detail-content :deep(h4 + ol) {
  margin-top: 4px;
}

.detail-content :deep(strong) {
  color: #244332;
  font-weight: 700;
}

.detail-content :deep(blockquote) {
  margin: 12px 0;
  padding: 10px 14px;
  border-left: 4px solid rgba(55, 133, 76, 0.35);
  background: #f7fbf8;
  color: #587064;
  border-radius: 0 14px 14px 0;
}

.detail-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}

.detail-content :deep(th),
.detail-content :deep(td) {
  border: 1px solid #e6ece6;
  padding: 10px 12px;
}

.detail-side {
  display: grid;
  gap: 12px;
  align-content: start;
}

.detail-side-card {
  padding: 18px;
  border-radius: 20px;
  background: #f8fbf7;
  border: 1px solid rgba(72, 129, 86, 0.12);
  box-shadow: 0 10px 24px rgba(30, 73, 46, 0.04);
}

.detail-side-card h4 {
  margin: 0 0 12px;
  font-size: 16px;
  color: #2b4936;
}

.detail-info-list {
  display: grid;
  gap: 10px;
}

.detail-info-item {
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(72, 129, 86, 0.08);
}

.detail-info-item span {
  display: block;
  margin-bottom: 4px;
  font-size: 12px;
  color: #7a9083;
}

.detail-info-item strong {
  display: block;
  color: #2b4936;
  line-height: 1.7;
  word-break: break-all;
}

.dialog-footer {
  text-align: center;
}

@media (max-width: 1200px) {
  .hero-section,
  .policies-layout,
  .detail-content-wrap {
    grid-template-columns: 1fr;
  }

  .policies-sidebar {
    position: static;
  }

  .policies-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .hero-copy,
  .hero-stats,
  .side-card,
  .featured-policy,
  .policy-card,
  .list-toolbar,
  .detail-article-card,
  .detail-side-card,
  .detail-banner {
    border-radius: 18px;
  }

  .hero-copy h1 {
    font-size: 28px;
  }

  .hero-search {
    grid-template-columns: 1fr;
  }

  .featured-top,
  .list-toolbar,
  .detail-section-head,
  .single-policy-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .detail-meta-grid {
    grid-template-columns: 1fr;
  }
}
</style>



