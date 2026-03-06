<!-- src/components/Recommend/index.vue -->
<template>
  <div class="recommend-section" v-if="recommendedItems.length > 0">
    <h3 class="recommend-title">为您推荐</h3>
    <el-row :gutter="24">
      <el-col :span="6" v-for="item in recommendedItems" :key="item.productsId">
        <div class="product-card" @click="goToDetail(item.productsId)">
          <div class="product-image">
            <img :src="baseUrl + item.image" alt="">
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ item.name }}</h3>
            <p class="product-origin">产地: {{ item.origin }}</p>
            <div class="product-price">
              <span class="current-price">¥{{ item.price }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getRecommendations, getProductListByIds } from '@/api/assisting/recommend'

const baseUrl = import.meta.env.VITE_APP_BASE_API
const router = useRouter()

// 定义 props
const props = defineProps({
  userId: {
    type: Number,
    required: true
  },
  topN: {
    type: Number,
    default: 6
  }
})

const recommendedItems = ref([])
const loading = ref(false)

const fetchRecommendations = async () => {
  if (!props.userId) return
  loading.value = true
  try {
    const res = await getRecommendations(props.userId, props.topN)
    const ids = res.data
    if (ids && ids.length > 0) {
      const productRes = await getProductListByIds(ids)
      recommendedItems.value = productRes.data
    }
  } catch (error) {
    console.error('获取推荐失败', error)
  } finally {
    loading.value = false
  }
}

const goToDetail = (id) => {
  router.push(`/index/productDetail/${id}`)
}

onMounted(() => {
  fetchRecommendations()
})
</script>

<style scoped>
.recommend-section {
  max-width: 1450px;
  margin: 0 auto 30px auto;
  padding: 0 20px;
}
.recommend-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}
.product-card {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  cursor: pointer;
  margin-bottom: 20px;
}
.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px 0 rgba(0, 0, 0, 0.2);
}
.product-image {
  height: 200px;
  overflow: hidden;
}
.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}
.product-card:hover .product-image img {
  transform: scale(1.05);
}
.product-info {
  padding: 15px;
}
.product-name {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.product-origin {
  margin: 0 0 5px 0;
  font-size: 12px;
  color: #999;
}
.product-price {
  margin-bottom: 10px;
}
.current-price {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}
</style>