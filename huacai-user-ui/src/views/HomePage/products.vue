<template>
  <div class="products-container">
    <Recommend
      :userId="userId"
      :topN="6"
      scene="default"
      title="猜你喜欢"
      kicker="个性推荐"
      description="结合你的购买偏好、相似商品关系和助农业务特征，优先展示更可能感兴趣的农产品。"
    />

    <section class="filter-section">
      <div class="filter-copy">
        <p class="filter-kicker">商品筛选</p>
        <h2>筛选助农产品</h2>
      </div>

      <el-row :gutter="18" class="filter-form">
        <el-col :xl="8" :lg="8" :md="12" :sm="24" :xs="24">
          <el-input
            v-model="queryParams.name"
            placeholder="搜索商品名称或关键词"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-col>
        <el-col :xl="5" :lg="5" :md="12" :sm="12" :xs="24">
          <el-select
            v-model="queryParams.origin"
            placeholder="选择产地"
            clearable
            style="width: 100%"
            @change="handleQuery"
          >
            <el-option
              v-for="item in originOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-col>
        <el-col :xl="5" :lg="5" :md="12" :sm="12" :xs="24">
          <el-select
            v-model="priceRange"
            placeholder="选择价格区间"
            clearable
            style="width: 100%"
            @change="handlePriceRangeChange"
          >
            <el-option label="50元以下" value="0-50" />
            <el-option label="50-100元" value="50-100" />
            <el-option label="100-200元" value="100-200" />
            <el-option label="200元以上" value="200+" />
          </el-select>
        </el-col>
        <el-col :xl="6" :lg="6" :md="24" :sm="24" :xs="24" class="filter-actions">
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-col>
      </el-row>
    </section>

    <div class="active-filters" v-if="queryParams.name || queryParams.origin || priceRange">
      <span class="active-label">当前筛选：</span>
      <el-tag v-if="queryParams.name" closable @close="clearFilter('name')">关键词：{{ queryParams.name }}</el-tag>
      <el-tag v-if="queryParams.origin" closable @close="clearFilter('origin')">产地：{{ queryParams.origin }}</el-tag>
      <el-tag v-if="priceRange" closable @close="clearFilter('price')">价格：{{ priceRangeLabel }}</el-tag>
    </div>

    <div class="product-list" v-loading="loading">
      <el-row :gutter="24">
        <el-col :xl="6" :lg="8" :md="8" :sm="12" :xs="24" v-for="product in productsList" :key="product.productsId">
          <div class="product-card" @click="goToProductDetail(product.productsId)">
            <div class="product-image">
              <img :src="baseUrl + product.image" alt="" />
              <div class="product-actions">
                <el-button type="primary" size="small" round @click.stop="addToCart(product)">
                  加入购物车
                </el-button>
              </div>
              <div class="product-origin-badge">{{ product.origin }}</div>
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-origin">发货地：{{ product.shipFrom || product.origin }}</p>
              <div class="product-meta">
                <span class="product-specs">{{ product.specs }}</span>
                <span class="product-owner">农户：{{ product.userName || '平台优选' }}</span>
              </div>
              <div class="product-price-row">
                <span class="current-price">￥{{ product.price }}</span>
                <span class="product-link">查看详情</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <div v-if="!loading && productsList.length === 0" class="empty-box">
        <div class="empty-state-card">
          <el-empty description="未查询到符合条件的商品">
            <template #description>
              <div class="empty-state-text">
                <strong>没有找到匹配商品</strong>
                <p>可以尝试重置筛选条件，重新浏览更多助农产品。</p>
              </div>
            </template>
            <el-button type="primary" @click="resetQuery">重置筛选</el-button>
          </el-empty>
        </div>
      </div>
    </div>

    <div class="pagination">
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :page-sizes="[8, 16, 32]"
        @pagination="getList"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { selectList } from '@/api/assisting/products.js'
import { addCart } from '@/api/assisting/cart.js'
import useUserStore from '@/store/modules/user'
import Recommend from '@/components/Recommend/index.vue'

const userStore = useUserStore()
const userId = computed(() => userStore.id)
const baseUrl = import.meta.env.VITE_APP_BASE_API
const router = useRouter()

const loading = ref(false)
const total = ref(0)
const productsList = ref([])
const originOptions = ref([])
const priceRange = ref('')

const queryParams = ref({
  pageNum: 1,
  pageSize: 8,
  name: null,
  origin: null,
  minPrice: null,
  maxPrice: null,
  userId: null,
  userName: null
})

const priceRangeLabel = computed(() => {
  const mapping = {
    '0-50': '50元以下',
    '50-100': '50-100元',
    '100-200': '100-200元',
    '200+': '200元以上'
  }
  return mapping[priceRange.value] || ''
})

const goToProductDetail = productsId => {
  router.push(`/index/productDetail/${productsId}`)
}

const applyPriceRange = value => {
  if (!value) {
    queryParams.value.minPrice = null
    queryParams.value.maxPrice = null
    return
  }
  if (value === '200+') {
    queryParams.value.minPrice = 200
    queryParams.value.maxPrice = null
    return
  }
  const [min, max] = value.split('-')
  queryParams.value.minPrice = Number(min)
  queryParams.value.maxPrice = Number(max)
}

const handlePriceRangeChange = value => {
  applyPriceRange(value)
  handleQuery()
}

const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 8,
    name: null,
    origin: null,
    minPrice: null,
    maxPrice: null,
    userId: null,
    userName: null
  }
  priceRange.value = ''
  getList()
}

const clearFilter = type => {
  if (type === 'name') {
    queryParams.value.name = null
  }
  if (type === 'origin') {
    queryParams.value.origin = null
  }
  if (type === 'price') {
    priceRange.value = ''
    queryParams.value.minPrice = null
    queryParams.value.maxPrice = null
  }
  handleQuery()
}

const addToCart = product => {
  loading.value = true
  addCart({
    productsId: product.productsId,
    quantity: 1
  }).then(() => {
    ElMessage.success('已添加到购物车')
  }).finally(() => {
    loading.value = false
  })
}

const getOriginOptions = () => {
  selectList({
    pageNum: 1,
    pageSize: 100,
    name: null,
    origin: null,
    minPrice: null,
    maxPrice: null
  }).then(res => {
    originOptions.value = [...new Set((res.rows || []).map(item => item.origin).filter(Boolean))]
  })
}

const getList = () => {
  loading.value = true
  selectList(queryParams.value).then(res => {
    total.value = res.total
    productsList.value = res.rows
  }).finally(() => {
    loading.value = false
  })
}

onMounted(() => {
  getOriginOptions()
  getList()
})
</script>

<style scoped>
.products-container {
  max-width: 1450px;
  margin: 0 auto;
  padding: 20px;
}

.filter-section {
  margin-bottom: 20px;
  padding: 26px;
  border: 1px solid #e7efe8;
  border-radius: 22px;
  background:
    radial-gradient(circle at top left, rgba(63, 149, 90, 0.12), transparent 28%),
    linear-gradient(180deg, #f8fdf8 0%, #ffffff 100%);
  box-shadow: 0 14px 34px rgba(21, 67, 40, 0.07);
}

:deep(.filter-form .el-input__wrapper),
:deep(.filter-form .el-select__wrapper) {
  min-height: 46px;
  border-radius: 14px;
  box-shadow: 0 0 0 1px rgba(122, 167, 96, 0.12) inset;
}

:deep(.filter-form .el-input__wrapper.is-focus),
:deep(.filter-form .el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px rgba(57, 138, 82, 0.26) inset;
}

.filter-copy {
  margin-bottom: 18px;
}

.filter-kicker {
  margin: 0 0 8px;
  color: #2b8a52;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 1px;
}

.filter-copy h2 {
  margin: 0 0 10px;
  font-size: 30px;
  color: #1f3f2b;
}

.filter-form {
  align-items: center;
}

.filter-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.active-filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.66);
  border: 1px solid rgba(123, 165, 97, 0.12);
}

.active-label {
  color: #60796b;
  font-size: 14px;
}

.product-list {
  margin-bottom: 24px;
  padding: 10px 0 6px;
}

.product-card {
  overflow: hidden;
  margin-bottom: 22px;
  border: 1px solid #e9efea;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 10px 28px rgba(26, 70, 43, 0.08);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 18px 34px rgba(26, 70, 43, 0.14);
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
  transform: scale(1.05);
}

.product-actions {
  position: absolute;
  bottom: -54px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  padding: 12px;
  background: rgba(255, 255, 255, 0.92);
  transition: bottom 0.25s ease;
}

.product-card:hover .product-actions {
  bottom: 0;
}

.product-origin-badge {
  position: absolute;
  top: 14px;
  left: 14px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(24, 114, 65, 0.92);
  color: #fff;
  font-size: 12px;
}

.product-info {
  padding: 18px;
}

.product-card::after {
  content: "";
  display: block;
  height: 4px;
  background: linear-gradient(90deg, #7db761, #d7b35a);
  opacity: 0;
  transition: opacity 0.25s ease;
}

.product-card:hover::after {
  opacity: 1;
}

.product-name {
  margin: 0 0 10px;
  font-size: 18px;
  color: #213547;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-origin {
  margin: 0 0 14px;
  font-size: 13px;
  color: #789081;
}

.product-meta,
.product-price-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.product-meta {
  margin-bottom: 14px;
  gap: 10px;
  font-size: 12px;
  color: #6d8478;
}

.product-specs,
.product-owner {
  display: inline-flex;
}

.current-price {
  font-size: 22px;
  font-weight: 700;
  color: #ef5b3f;
}

.product-link {
  color: #2a7d49;
  font-size: 13px;
  font-weight: 600;
}

.empty-box {
  padding: 50px 0;
}

.empty-state-card {
  padding: 14px;
  border-radius: 22px;
  border: 1px dashed rgba(109, 157, 86, 0.24);
  background: rgba(255, 255, 255, 0.78);
}

.empty-state-text strong {
  display: block;
  margin-bottom: 6px;
  color: #274533;
  font-size: 18px;
}

.empty-state-text p {
  margin: 0;
  color: #6d8177;
  line-height: 1.8;
}

.pagination {
  display: flex;
  justify-content: center;
  margin: 28px 0;
}

:deep(.pagination-container .el-pagination.is-background .btn-prev),
:deep(.pagination-container .el-pagination.is-background .btn-next),
:deep(.pagination-container .el-pagination.is-background .el-pager li) {
  border-radius: 12px;
}

@media (max-width: 992px) {
  .filter-actions {
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .products-container {
    padding: 14px;
  }

  .filter-section {
    padding: 20px 16px;
  }

  .filter-copy h2 {
    font-size: 24px;
  }

  .filter-actions {
    flex-wrap: wrap;
  }
}
</style>
