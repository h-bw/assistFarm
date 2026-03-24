<template>
  <div class="policies-page">
    <section class="hero-section">
      <div class="hero-copy">
        <span class="hero-badge">助农信息服务</span>
        <h1>助农政策查询与申报参考</h1>
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
                <p>优先确认政策对应的省、市、区县和实施对象。</p>
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
                <p>政策页面已整理部门、联系人和电话，便于演示展示。</p>
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
            <el-button type="success" plain @click="showDetail(featuredPolicy)">查看详情</el-button>
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
          <div>
            <h3>{{ activeCategoryLabel }}</h3>
            <p>为你整理适合助农商城展示的政策摘要与申报信息</p>
          </div>
          <span class="toolbar-count">共 {{ total }} 条</span>
        </div>

        <div class="policies-grid" v-loading="loading">
          <template v-if="policyCards.length > 0">
            <article
              v-for="policy in policyCards"
              :key="policy.policiesId"
              class="policy-card"
            >
              <div class="policy-card-top">
                <dict-tag :options="policies_category" :value="policy.category" />
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
                <el-button text type="success" @click="showDetail(policy)">查看详情</el-button>
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
      width="74%"
      height="90vh"
      show-maximize
      showFooter
      resize
    >
      <div class="detail-shell">
        <div class="detail-banner">
          <dict-tag :options="policies_category" :value="currentPolicy.category" />
          <h2>{{ currentPolicy.title }}</h2>
          <p>{{ currentPolicy.summary }}</p>
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
          <div class="detail-content" v-html="currentPolicy.content || '<p>暂无政策正文内容</p>'"></div>

          <aside class="detail-side">
            <div class="detail-side-card">
              <h4>联系方式</h4>
              <p><strong>负责部门：</strong>{{ currentPolicy.department || '待补充' }}</p>
              <p><strong>联系人：</strong>{{ currentPolicy.contactPerson || '待补充' }}</p>
              <p><strong>电话：</strong>{{ currentPolicy.phone || '待补充' }}</p>
              <p><strong>地址：</strong>{{ currentPolicy.address || '待补充' }}</p>
            </div>
            <div v-if="currentPolicy.sourceLink" class="detail-side-card source-card">
              <h4>政策来源</h4>
              <p>可跳转查看官方公开信息页面，便于答辩展示时说明内容来源。</p>
              <el-link :href="currentPolicy.sourceLink" target="_blank" type="success" :underline="false">
                查看政策原文
              </el-link>
            </div>
            <div class="detail-side-card tips-card">
              <h4>阅读建议</h4>
              <p>优先关注申报对象、补贴额度、申报流程和时限要求。</p>
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
import { computed, getCurrentInstance, onMounted, ref, watch } from 'vue'
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

watch(
  () => route.query,
  () => {
    initQueryFromRoute()
    getList()
  }
)

onMounted(() => {
  initQueryFromRoute()
  getList()
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

.toolbar-count {
  font-size: 14px;
  color: #628274;
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
  opacity: 0;
  transition: opacity 0.25s ease;
}

.policy-card:hover::before {
  opacity: 1;
}

.policy-card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
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
  font-size: 20px;
  line-height: 1.45;
  color: #183724;
  cursor: pointer;
}

.policy-card h3:hover {
  color: #2f8850;
}

.policy-summary {
  margin: 0 0 18px;
  color: #5a7365;
  line-height: 1.9;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.policy-actions {
  margin-top: 16px;
}

.empty-policies {
  grid-column: 1 / -1;
  padding: 60px 0;
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
  padding: 10px 8px 20px;
}

.detail-banner {
  padding: 8px 10px 24px;
  border-bottom: 1px solid #edf2ec;
}

.detail-banner h2 {
  margin: 12px 0 10px;
  font-size: 28px;
  line-height: 1.4;
  color: #163522;
}

.detail-banner p {
  margin: 0 0 18px;
  color: #587064;
  line-height: 1.9;
}

.detail-meta-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.meta-card {
  padding: 14px 16px;
  border-radius: 16px;
  background: #f7faf7;
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
  gap: 24px;
  margin-top: 24px;
}

.detail-content {
  min-width: 0;
}

.detail-content :deep(h3),
.detail-content :deep(h4) {
  color: #2f8350;
  margin: 20px 0 10px;
}

.detail-content :deep(p),
.detail-content :deep(li) {
  color: #3f5148;
  line-height: 1.9;
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
  gap: 16px;
  align-content: start;
}

.detail-side-card {
  padding: 18px;
  border-radius: 18px;
  background: #f8fbf7;
  border: 1px solid rgba(72, 129, 86, 0.12);
  box-shadow: 0 10px 24px rgba(30, 73, 46, 0.04);
}

.detail-side-card h4 {
  margin: 0 0 12px;
  font-size: 16px;
  color: #2b4936;
}

.detail-side-card p {
  margin: 8px 0;
  font-size: 14px;
  color: #60786c;
  line-height: 1.7;
}

.source-card :deep(.el-link) {
  margin-top: 6px;
  font-weight: 600;
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
  .list-toolbar {
    border-radius: 18px;
  }

  .hero-copy h1 {
    font-size: 28px;
  }

  .hero-search {
    grid-template-columns: 1fr;
  }

  .featured-top,
  .list-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .detail-meta-grid {
    grid-template-columns: 1fr;
  }
}
</style>
