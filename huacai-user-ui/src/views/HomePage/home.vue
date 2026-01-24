<template>
  <!-- 首页容器 -->
    <div class="home-content">
        <!-- 主要内容区 -->
        <div class="main-content">
            <div class="banner-section">
                <!-- 轮播图 -->
                <el-carousel
                        height="500px"
                        :interval="5000"
                        arrow="always"
                        indicator-position="outside"
                >
                    <!-- 轮播图项 -->
                    <el-carousel-item v-for="item in bannerList" :key="item.bannerId">
                        <!-- 轮播图背景和内容 -->
                        <div class="banner-item" :style="{ backgroundImage: `url(${item.image})` }">
                            <!-- 遮罩层, 用于增强文字可读性 -->
                            <div class="banner-overlay"/>
                            <!-- 轮播图文字内容 -->
                            <div class="banner-content">
                                <h2 class="banner-title">{{ item.title }}</h2>
                                <p class="banner-description">{{ item.description }}</p>
                            </div>
                        </div>
                    </el-carousel-item>
                </el-carousel>
            </div>

            <!-- 扶贫产品展示区域 -->
            <div class="section product-recommend">
                <!-- 区域标题和操作 -->
                <div class="section-header">
                    <h2 class="section-title">扶贫产品</h2>
                    <p class="section-subtitle">精选优质扶贫产品, 助力乡村振兴</p>
                    <!-- 查看更多链接 -->
                    <el-link type="primary" :underline="false" @click="goToProducts">
                        查看更多
                        <el-icon><ArrowRight/></el-icon>
                    </el-link>
                </div>
                <!-- 产品列表 -->
                <div class="product-list">
                    <el-row :gutter="20">
                        <el-col :span="6" v-for="item in productsList" :key="item.productsId">
                            <!-- 产品卡片, 点击后跳转到详情页 -->
                            <div class="product-card" @click="goToProductDetail(item.productsId)">
                                <!-- 产品图片 -->
                                <div class="product-image">
                                    <img :src="baseUrl + item.image" alt="">
                                </div>
                                <!-- 产品信息 -->
                                <div class="product-info">
                                    <h3 class="product-name">{{ item.name }}</h3>
                                    <p class="product-origin">产地: {{ item.origin }}</p>
                                    <div class="product-meta">
                                        <!-- 产品价格 -->
                                        <div class="product-price">
                                            <span class="current-price">¥{{ item.price }}</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </el-col>
                    </el-row>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import {listBanner} from "@/api/assisting/banner.js";
import {useRouter} from "vue-router";
import {ArrowRight} from "@element-plus/icons-vue";
import {selectList} from "@/api/assisting/products.js";

//初始化路由
const router = useRouter()

//跳转到扶贫产品页面
const goToProducts = () => {
    router.push('/index/products')
}

//跳转到产品详情页面
const goToProductDetail = (productsId) => {
    router.push(`/index/productDetail/${productsId}`)
}

//api基础地址
const baseUrl = import.meta.env.VITE_APP_BASE_API

// 轮播图列表数据
const bannerList = ref([])

//扶贫产品列表数据
const productsList = ref([])

//轮播图查询参数
const bannerQuery = ref({
    pageNum: 1,
    pageSize: 5,
})

//产品列表查询参数
const productsQuery = ref({
    pageNum: 1,
    pageSize: 4,
})

//查询列表数据
const getList = () => {
    listBanner(bannerQuery.value).then(res => {
        bannerList.value = res.rows
        bannerList.value.forEach(item => {
            item.image = baseUrl + item.image
        })
    })

    selectList(productsQuery.value).then(res => {
        productsList.value = res.rows
    })
}

//组件加载时执行方法
onMounted(() => {
    getList()
})
</script>

<style scoped>
/* 首页容器样式 */
.home-container {
    min-height: 100vh; /* 最小高度为视口高度 */
    display: flex;
    flex-direction: column; /* 垂直方向布局 */
}

/* 主要内容区域样式 */
.main-content {
    flex: 1; /* 占据剩余空间 */
    max-width: 1450px; /* 最大宽度 */
    margin: 0 auto; /* 水平居中 */
    padding: 20px; /* 内边距 */
    width: 100%; /* 宽度100% */
}

/* 轮播图区域样式 */
.banner-section {
    margin-bottom: 60px; /* 底部外边距 */
}

/* 轮播图单项样式 */
.banner-item {
    height: 100%; /* 高度100% */
    background-size: cover; /* 背景图覆盖整个元素 */
    background-position: center; /* 背景图居中 */
    position: relative; /* 相对定位，为子元素定位做准备 */
    border-radius: 12px; /* 圆角 */
    overflow: hidden; /* 溢出隐藏 */
}

/* 轮播图遮罩层样式 */
.banner-overlay {
    position: absolute; /* 绝对定位 */
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    /* 渐变遮罩，增强文字可读性 */
    background: linear-gradient(90deg, rgba(0, 0, 0, 0.7) 0%, rgba(0, 0, 0, 0.3) 50%, rgba(0, 0, 0, 0) 100%);
}

/* 轮播图内容区域样式 */
.banner-content {
    position: absolute; /* 绝对定位 */
    bottom: 100px; /* 距离底部100px */
    left: 80px; /* 距离左侧80px */
    color: #fff; /* 文字颜色白色 */
    max-width: 600px; /* 最大宽度 */
    z-index: 1; /* 层级，确保在遮罩层之上 */
}

/* 轮播图标题样式 */
.banner-title {
    font-size: 42px; /* 字体大小 */
    font-weight: 700; /* 字体粗细 */
    margin-bottom: 20px; /* 底部外边距 */
    line-height: 1.2; /* 行高 */
}

/* 轮播图描述样式 */
.banner-description {
    font-size: 18px; /* 字体大小 */
    margin-bottom: 30px; /* 底部外边距 */
    line-height: 1.6; /* 行高 */
}

/* 轮播图按钮容器样式 */
.banner-actions .el-button {
    margin-right: 15px; /* 右侧外边距 */
    padding: 12px 30px; /* 内边距 */
}

/* 产品推荐区域样式 */
.product-recommend {
    margin-bottom: 70px; /* 底部外边距 */
}

/* 产品列表样式 */
.product-list {
    margin-top: 40px; /* 顶部外边距 */
}

/* 产品卡片样式 */
.product-card {
    background-color: #fff; /* 背景色白色 */
    border-radius: 12px; /* 圆角 */
    overflow: hidden; /* 溢出隐藏 */
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); /* 阴影效果 */
    transition: all 0.3s ease; /* 过渡效果 */
    cursor: pointer; /* 鼠标指针样式 */
    margin-bottom: 10px; /* 底部外边距 */
}

/* 产品卡片悬停效果 */
.product-card:hover {
    transform: translateY(-8px); /* 向上移动 */
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12); /* 增强阴影效果 */
}

/* 产品图片容器样式 */
.product-image {
    position: relative; /* 相对定位 */
    height: 220px; /* 固定高度 */
    overflow: hidden; /* 溢出隐藏 */
}

/* 产品图片样式 */
.product-image img {
    width: 100%; /* 宽度100% */
    height: 100%; /* 高度100% */
    object-fit: cover; /* 覆盖整个容器 */
    transition: transform 0.5s ease; /* 过渡效果 */
}

/* 产品卡片悬停时图片放大效果 */
.product-card:hover .product-image img {
    transform: scale(1.08); /* 放大图片 */
}

/* 产品信息区域样式 */
.product-info {
    padding: 20px; /* 内边距 */
}

/* 产品名称样式 */
.product-name {
    margin: 0 0 10px 0; /* 外边距 */
    font-size: 18px; /* 字体大小 */
    font-weight: 600; /* 字体粗细 */
    color: #333; /* 字体颜色 */
    white-space: nowrap; /* 不换行 */
    overflow: hidden; /* 溢出隐藏 */
    text-overflow: ellipsis; /* 文本溢出显示省略号 */
}

/* 产品产地样式 */
.product-origin {
    margin: 0 0 15px 0; /* 外边距 */
    font-size: 13px; /* 字体大小 */
    color: #999; /* 字体颜色 */
}

/* 产品元信息容器样式 */
.product-meta {
    display: flex; /* 弹性布局 */
    justify-content: space-between; /* 两端对齐 */
    align-items: center; /* 垂直居中 */
}

/* 产品价格容器样式 */
.product-price {
    display: flex;
    flex-direction: column; /* 垂直方向布局 */
}

/* 当前价格样式 */
.current-price {
    font-size: 20px; /* 字体大小 */
    font-weight: 700; /* 字体粗细 */
    color: #f56c6c; /* 价格颜色（红色系） */
}

/* 深度选择器，修改Element Plus评分组件样式 */
:deep(.el-rate__icon) {
    font-size: 14px; /* 图标大小 */
}

/* 深度选择器，修改Element Plus评分文本样式 */
:deep(.el-rate__text) {
    font-size: 12px; /* 字体大小 */
    margin-left: 5px; /* 左侧外边距 */
}

/* 故事图片样式 */
.story-image img {
    width: 100%; /* 宽度100% */
    border-radius: 12px; /* 圆角 */
}

/* 故事内容区域标题样式 */
.story-content .section-title {
    margin-bottom: 20px; /* 底部外边距 */
    color: #333; /* 字体颜色 */
}

/* 评论作者信息样式 */
.review-author h4 {
    margin: 0 0 5px 0; /* 外边距 */
    font-size: 18px; /* 字体大小 */
    color: #333; /* 字体颜色 */
}

.review-author p {
    margin: 0; /* 外边距清零 */
    font-size: 14px; /* 字体大小 */
    color: #999; /* 字体颜色 */
}

/* 通用区域样式 */
.section {
    margin-bottom: 50px; /* 底部外边距 */
}

/* 区域头部样式 */
.section-header {
    text-align: center; /* 文本居中 */
    margin-bottom: 40px; /* 底部外边距 */
}

/* 区域标题样式 */
.section-title {
    font-size: 32px; /* 字体大小 */
    font-weight: 700; /* 字体粗细 */
    color: #333; /* 字体颜色 */
    margin: 0 0 15px 0; /* 外边距 */
}

/* 区域副标题样式 */
.section-subtitle {
    font-size: 16px; /* 字体大小 */
    color: #666; /* 字体颜色 */
    margin: 0; /* 外边距清零 */
}

/* 响应式设计 - 屏幕宽度小于1200px */
@media (max-width: 1200px) {
    .main-content {
        padding: 15px; /* 调整内边距 */
    }

    .banner-content {
        left: 50px; /* 调整左侧距离 */
        bottom: 80px; /* 调整底部距离 */
        max-width: 500px; /* 调整最大宽度 */
    }

    .banner-title {
        font-size: 36px; /* 调整字体大小 */
    }
}

/* 响应式设计 - 屏幕宽度小于992px */
@media (max-width: 992px) {
    .banner-content {
        left: 30px; /* 调整左侧距离 */
        bottom: 60px; /* 调整底部距离 */
        max-width: 400px; /* 调整最大宽度 */
    }

    .banner-title {
        font-size: 32px; /* 调整字体大小 */
    }

    .banner-description {
        font-size: 16px; /* 调整字体大小 */
    }
}

/* 响应式设计 - 屏幕宽度小于768px */
@media (max-width: 768px) {
    .banner-content {
        left: 20px; /* 调整左侧距离 */
        bottom: 40px; /* 调整底部距离 */
        max-width: 300px; /* 调整最大宽度 */
    }

    .banner-title {
        font-size: 28px; /* 调整字体大小 */
    }

    .banner-description {
        font-size: 14px; /* 调整字体大小 */
    }

    .banner-actions .el-button {
        display: block; /* 块级显示 */
        width: 80%; /* 宽度80% */
        margin: 10px auto; /* 外边距自动居中 */
    }

    .section-title {
        font-size: 28px; /* 调整字体大小 */
    }

    .product-card {
        margin-bottom: 20px; /* 调整底部外边距 */
    }
}
</style>
