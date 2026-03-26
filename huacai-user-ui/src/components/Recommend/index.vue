<template>
  <section v-if="recommendedItems.length > 0" class="recommend-section">
    <div class="recommend-header">
      <div>
        <p class="recommend-kicker">{{ kicker }}</p>
        <h3 class="recommend-title">{{ title }}</h3>
        <p v-if="description" class="recommend-description">{{ description }}</p>
      </div>
      <span class="recommend-count">共 {{ recommendedItems.length }} 条</span>
    </div>

    <el-row :gutter="24">
      <el-col :xl="6" :lg="8" :md="8" :sm="12" :xs="24" v-for="item in recommendedItems" :key="item.productsId">
        <div class="product-card" @click="goToDetail(item.productsId)">
          <div class="product-image">
            <img :src="baseUrl + item.image" alt="">
            <div class="product-tag">{{ sceneTag }}</div>
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ item.name }}</h3>
            <p class="product-origin">产地：{{ item.origin || '优质助农基地' }}</p>
            <div class="product-meta">
              <span>{{ item.specs || '规格待补充' }}</span>
              <span>{{ item.userName || '平台精选' }}</span>
            </div>
            <p v-if="item.recommendReason" class="recommend-reason">{{ item.recommendReason }}</p>
            <div class="product-price-row">
              <span class="current-price">楼{{ item.price }}</span>
              <span class="product-link">查看详情</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getRecommendations, getProductListByIds, mergeRecommendationProducts } from '@/api/assisting/recommend'

const baseUrl = import.meta.env.VITE_APP_BASE_API
const router = useRouter()

const props = defineProps({
  userId: {
    type: Number,
    required: true
  },
  topN: {
    type: Number,
    default: 6
  },
  scene: {
    type: String,
    default: 'default'
  },
  title: {
    type: String,
    default: '为您推荐'
  },
  kicker: {
    type: String,
    default: '个性推荐'
  },
  description: {
    type: String,
    default: ''
  },
  productIds: {
    type: Array,
    default: () => []
  }
})

const recommendedItems = ref([])
const loading = ref(false)

const sceneTag = computed(() => {
  const mapping = {
    default: '个性推荐',
    buy_again: '再次购买',
    cross_sell: '搭配购买'
  }
  return mapping[props.scene] || '推荐商品'
})

const normalizedProductIds = computed(() => {
  return (props.productIds || []).filter(Boolean)
})

const fetchRecommendations = async () => {
  if (!props.userId) {
    recommendedItems.value = []
    return
  }

  loading.value = true
  try {
    const res = await getRecommendations(props.userId, props.topN, {
      scene: props.scene,
      productIds: normalizedProductIds.value
    })
    const items = res.data
    if (items && items.length > 0) {
      const productRes = await getProductListByIds(items.map(item => item.productsId))
      recommendedItems.value = mergeRecommendationProducts(items, productRes.data || [])
    } else {
      recommendedItems.value = []
    }
  } catch (error) {
    console.error('获取推荐失败', error)
    recommendedItems.value = []
  } finally {
    loading.value = false
  }
}

const goToDetail = id => {
  router.push(`/index/productDetail/${id}`)
}

watch(
  () => [props.userId, props.topN, props.scene, normalizedProductIds.value.join(',')],
  () => {
    fetchRecommendations()
  }
)

onMounted(() => {
  fetchRecommendations()
})
</script>

<style scoped>
.recommend-section {
  margin: 0 auto 30px;
  padding: 24px;
  border: 1px solid #e7efe8;
  border-radius: 24px;
  background:
    radial-gradient(circle at top left, rgba(73, 155, 98, 0.12), transparent 26%),
    linear-gradient(180deg, #f8fdf9 0%, #ffffff 100%);
  box-shadow: 0 16px 38px rgba(26, 70, 43, 0.07);
}

.recommend-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.recommend-kicker {
  margin: 0 0 8px;
  color: #2d8650;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.recommend-title {
  margin: 0 0 8px;
  color: #1f3f2b;
  font-size: 28px;
}

.recommend-description {
  margin: 0;
  color: #688074;
  font-size: 14px;
  line-height: 1.8;
}

.recommend-count {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(109, 157, 86, 0.16);
  color: #5f7a6b;
  font-size: 13px;
}

.product-card {
  overflow: hidden;
  margin-bottom: 8px;
  border: 1px solid #e8efea;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(26, 70, 43, 0.08);
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 18px 34px rgba(26, 70, 43, 0.14);
}

.product-image {
  position: relative;
  height: 210px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.product-tag {
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

.product-name {
  margin: 0 0 10px;
  color: #213547;
  font-size: 18px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-origin {
  margin: 0 0 12px;
  color: #72867b;
  font-size: 13px;
}

.product-meta,
.product-price-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.product-meta {
  margin-bottom: 14px;
  gap: 12px;
  color: #6f8479;
  font-size: 12px;
}

.recommend-reason {
  margin: 0 0 14px;
  padding: 8px 10px;
  border-radius: 12px;
  background: rgba(46, 133, 78, 0.08);
  color: #417056;
  font-size: 12px;
  line-height: 1.7;
}

.current-price {
  color: #ef5b3f;
  font-size: 22px;
  font-weight: 700;
}

.product-link {
  color: #2a7d49;
  font-size: 13px;
  font-weight: 600;
}

@media (max-width: 992px) {
  .recommend-header {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .recommend-section {
    padding: 20px 16px;
    border-radius: 20px;
  }

  .recommend-title {
    font-size: 24px;
  }
}
</style>
