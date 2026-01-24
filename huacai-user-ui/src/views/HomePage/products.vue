<template>
    <!-- 产品列表容器 -->
    <div class="products-container">
        <!-- 筛选区域 -->
        <div class="filter-section">
            <el-row :gutter="24">
                <!-- 搜索输入框占20列 -->
                <el-col :span="20">
                    <el-input
                            v-model="queryParams.name"
                            placeholder="请输入关键词进行查询"
                            clearable
                            @keyup.enter="handleQuery"
                    />
                </el-col>
                <!-- 按钮区域占4列 -->
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
                    <!-- 产品卡片, 点击跳转到详情页 -->
                    <div class="product-card" @click="goToProductDetail(product.productsId)">
                        <!-- 产品图片区域 -->
                        <div class="product-image">
                            <img :src="baseUrl + product.image" alt="">
                            <!-- 产品操作按钮(加入购物车) -->
                            <div class="product-actions">
                                <el-button v-loading="loading" type="primary" size="small"
                                           round @click.stop="addToCart(product)">
                                    加入购物车
                                </el-button>
                            </div>
                        </div>
                        <!-- 产品信息区域 -->
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
import {selectList} from "@/api/assisting/products.js";
import {addCart} from "@/api/assisting/cart.js";
import {ElMessage} from "element-plus";
import {useRouter} from "vue-router";

//获取基础URL(从环境变量)
const baseUrl = import.meta.env.VITE_APP_BASE_API

//获取路由实例
const router = useRouter()

//跳转到产品详情页面
const goToProductDetail = (productsId) => {
    router.push(`/index/productDetail/${productsId}`)
}

//查询参数
const queryParams = ref({
    pageNum: 1,
    pageSize: 8,
    name: null,
    userId: null,
    userName: null
})

/** 搜索按钮操作 */
const handleQuery = () => {
    queryParams.value.pageNum = 1
    getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
    queryParams.value.name = null
    handleQuery()
}

//添加产品到购物车
const addToCart = (product) => {
    //打开加载状态
    loading.value = true
    const item = {
        productsId: product.productsId,
        quantity: 1 //默认数量为1
    }
    //调用api添加购物车信息
    addCart(item).then(res => {
        //成功提示
        ElMessage.success('已添加到购物车')
        //关闭加载状态
        loading.value = false
    })
}

//加载状态
const loading = ref(false)

//产品列表数据
const productsList = ref([])

//产品总数
const total = ref(0)

//获取产品列表数据
const getList = () => {
    //显示加载状态
    loading.value = true
    //调用api查询数据
    selectList(queryParams.value).then(res => {
        //将产品总数赋值给total
        total.value = res.total
        //将查询到的列表数据赋值给productsList
        productsList.value = res.rows
        //关闭加载状态
        loading.value = false
    })
}

//组件加载时自动获取产品列表
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
