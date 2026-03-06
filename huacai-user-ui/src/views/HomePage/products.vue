<template>
  <div class="products-container">
    <!-- 推荐板块放在筛选区域上方 -->
    <Recommend :userId="userId" :topN="6" />

    <!-- 筛选区域 -->
    <div class="filter-section">
      <el-row :gutter="24">
        <el-col :span="20">
          <el-input
              v-model="queryParams.name"
              placeholder="请输入关键词进行查询"
              clearable
              @keyup.enter="handleQuery"
          />
        </el-col>
        <el-col :span="4">
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 产品列表展示区域 -->
    <div class="product-list" v-loading="loading">
      <el-row :gutter="24">
        <el-col :span="6" v-for="product in productsList" :key="product.productsId">
          <div class="product-card" @click="goToProductDetail(product.productsId)">
            <div class="product-image">
              <img :src="baseUrl + product.image" alt="">
              <div class="product-actions">
                <el-button type="primary" size="small" round @click.stop="addToCart(product)">
                  加入购物车
                </el-button>
              </div>
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-origin">产地: {{ product.origin }}</p>
              <div class="product-price">
                <span class="current-price">¥{{ product.price }}</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 分页组件 -->
    <div class="pagination">
      <pagination
          v-show="total>0"
          :total="total"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
          :page-sizes="[8,16,32]"
      />
    </div>
  </div>
</template>


<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { selectList } from "@/api/assisting/products.js"
import { addCart } from "@/api/assisting/cart.js"
import { ElMessage } from "element-plus"
import useUserStore from '@/store/modules/user'  // 导入用户 store
import Recommend from '@/components/Recommend/index.vue'  // 导入推荐组件

const userStore = useUserStore()
const userId = computed(() => userStore.id)  // 获取用户ID

const baseUrl = import.meta.env.VITE_APP_BASE_API
const router = useRouter()

// 其余代码保持不变
const goToProductDetail = (productsId) => {
  router.push(`/index/productDetail/${productsId}`)
}

const queryParams = ref({
  pageNum: 1,
  pageSize: 8,
  name: null,
  userId: null,
  userName: null
})

const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryParams.value.name = null
  handleQuery()
}

const addToCart = (product) => {
  loading.value = true
  const item = {
    productsId: product.productsId,
    quantity: 1
  }
  addCart(item).then(res => {
    ElMessage.success('已添加到购物车')
    loading.value = false
  })
}

const loading = ref(false)
const productsList = ref([])
const total = ref(0)

const getList = () => {
  loading.value = true
  selectList(queryParams.value).then(res => {
    total.value = res.total
    productsList.value = res.rows
    loading.value = false
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
/*
 * 产品列表容器样式
 * 设置最大宽度、居中、内边距
 */
.products-container {
    max-width: 1450px; /* 最大宽度限制 */
    margin: 0 auto; /* 水平居中 */
    padding: 20px; /* 内边距 */
}

/*
 * 筛选区域样式
 * 背景色、圆角、阴影、下边距等
 */
.filter-section {
    max-width: 1450px; /* 最大宽度 */
    background-color: #fff; /* 白色背景 */
    padding: 20px; /* 内边距 */
    border-radius: 8px; /* 圆角 */
    margin-bottom: 20px; /* 下边距 */
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); /* 阴影效果 */
}

/* 产品列表区域下边距 */
.product-list {
    margin-bottom: 30px;
}

/*
 * 产品卡片样式
 * 背景色、圆角、溢出隐藏、阴影、过渡效果、指针样式
 */
.product-card {
    background-color: #fff; /* 白色背景 */
    border-radius: 8px; /* 圆角 */
    overflow: hidden; /* 溢出隐藏 */
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); /* 阴影 */
    transition: all 0.3s; /* 过渡效果 */
    cursor: pointer; /* 手型指针 */
    margin-bottom: 20px; /* 下边距 */
}

/* 产品卡片悬停效果 */
.product-card:hover {
    transform: translateY(-5px); /* 上移5px */
    box-shadow: 0 5px 15px 0 rgba(0, 0, 0, 0.2); /* 阴影增强 */
}

/* 产品图片区域样式 */
.product-image {
    position: relative; /* 相对定位 */
    height: 200px; /* 固定高度 */
    overflow: hidden; /* 溢出隐藏 */
}

/* 产品图片样式 */
.product-image img {
    width: 100%; /* 宽度100% */
    height: 100%; /* 高度100% */
    object-fit: cover; /* 覆盖填充 */
    transition: transform 0.5s; /* 缩放过渡 */
}

/* 产品卡片悬停时图片放大效果 */
.product-card:hover .product-image img {
    transform: scale(1.05); /* 放大5% */
}

/* 产品操作按钮区域样式 */
.product-actions {
    position: absolute; /* 绝对定位 */
    bottom: -50px; /* 初始位置在可视区域下方 */
    left: 0;
    right: 0;
    display: flex;
    justify-content: center; /* 水平居中 */
    padding: 10px;
    background: rgba(255, 255, 255, 0.9); /* 半透明白色背景 */
    transition: bottom 0.3s; /* 底部位置过渡 */
}

/* 产品卡片悬停时显示操作按钮 */
.product-card:hover .product-actions {
    bottom: 0; /* 移动到可视区域 */
}

/* 产品信息区域内边距 */
.product-info {
    padding: 15px;
}

/* 产品名称样式 */
.product-name {
    margin: 0 0 8px 0; /* 外边距 */
    font-size: 16px; /* 字体大小 */
    color: #333; /* 字体颜色 */
    white-space: nowrap; /* 不换行 */
    overflow: hidden; /* 溢出隐藏 */
    text-overflow: ellipsis; /* 溢出显示省略号 */
}

/* 产品产地样式 */
.product-origin {
    margin: 0 0 5px 0;
    font-size: 12px;
    color: #999; /* 灰色文字 */
}

/* 产品价格区域下边距 */
.product-price {
    margin-bottom: 10px;
}

/* 当前价格样式 */
.current-price {
    font-size: 18px;
    font-weight: bold;
    color: #f56c6c; /* 红色价格 */
}

/* 分页区域样式 */
.pagination {
    display: flex;
    justify-content: center; /* 水平居中 */
    margin: 30px 0; /* 上下边距 */
}

/* 响应式设计 - 移动端适配 */
@media (max-width: 768px) {
    /* 小屏幕下筛选区域的列项增加下边距 */
    .filter-section .el-col {
        margin-bottom: 15px;
    }

    /* 小屏幕下产品卡片占满整行 */
    .product-list .el-col {
        width: 100%;
    }
}
</style>
