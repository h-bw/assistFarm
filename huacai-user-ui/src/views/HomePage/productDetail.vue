<template>
  <!-- 主容器 -->
    <div class="product-detail-container">
        <!-- 返回按钮区域 -->
        <div class="back-to-home">
            <el-button type="text" @click="router.go(-1)" class="back-button">
                <el-icon>
                    <ArrowLeft/>
                </el-icon>
                返回
            </el-button>
        </div>

        <!-- 商品主内容区域 -->
        <div class="product-main">
            <!-- 商品图片展示区 -->
            <div class="product-gallery">
                <!-- 商品图片-->
                <img :src="baseUrl + product.image" alt="" class="product-image">
            </div>

            <!-- 商品信息-->
            <div class="product-info">
                <!-- 商品名称-->
                <h1 class="product-title">{{ product.name }}</h1>
                <!-- 商品副标题-->
                <div class="product-subtitle">{{ product.subtitle }}</div>

                <!-- 价格区域-->
                <div class="product-price-section">
                    <div class="price-row">
                        <span class="price-label">价格: </span>
                        <span class="current-price">¥{{ product.price }}</span>
                    </div>
                </div>

                <!-- 商品来源信息-->
                <div class="product-source">
                    <div class="source-item">
                        <span class="label">产地:</span>
                        <span class="value">{{ product.origin }}</span>
                    </div>
                    <div class="source-item">
                        <span class="label">发货地:</span>
                        <span class="value">{{ product.shipFrom }}</span>
                    </div>
                    <div class="source-item">
                        <span class="label">规格:</span>
                        <span class="value">{{ product.specs }}</span>
                    </div>
                </div>

                <!-- 数量选择器-->
                <div class="product-quantity">
                    <span class="label">数量: </span>
                    <el-input-number
                            v-model="quantity"
                            :min="1"
                            :max="999"
                            size="large"
                    />
                    <!-- 库存显示-->
                    <span class="inventory">库存 {{ product.inventory }} 件</span>
                </div>

                <!-- 操作按钮区域-->
                <div class="product-actions">
                    <!-- 加入购物车按钮-->
                    <el-button
                            type="danger"
                            size="large"
                            @click="addToCart"
                            v-loading="loading"
                    >
                        <el-icon>
                            <ShoppingCart/>
                        </el-icon>
                        加入购物车
                    </el-button>
                </div>
            </div>
        </div>

        <!-- 商品详情选项卡-->
        <div class="product-tabs">
            <el-tabs v-model="activeTab">
                <!-- 商品详情标签页-->
                <el-tab-pane label="商品详情" name="detail">
                    <div class="product-detail-content" v-html="product.detail"/>
                </el-tab-pane>
                <!-- 规格参数标签页-->
                <el-tab-pane label="规格参数" name="specs">
                    <div class="product-specs-content">
                        <!-- 规格参数表格-->
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
            </el-tabs>
        </div>

    </div>
</template>

<script setup>
import {useRoute, useRouter} from "vue-router";
import {ArrowLeft, ShoppingCart} from "@element-plus/icons-vue";
import {getProducts} from "@/api/assisting/products.js";
import {addCart} from "@/api/assisting/cart.js";
import {ElMessage} from "element-plus";

//当前激活的标签页, 默认为 detail(商品详情)
const activeTab = ref('detail')

//获取当前路由
const route = useRoute()

//获取当前路由实例
const router = useRouter()

//商品数据
const product = ref({})

//基础api URL
const baseUrl = import.meta.env.VITE_APP_BASE_API

//购买数量, 默认为1
const quantity = ref(1)

//加载状态
const loading = ref(false)

//加入购物车方法
const addToCart = () => {
    //打开加载状态
    loading.value = true
    //购物新增购物车信息所需要的数据
    const item = {
        productsId: route.params.id,
        quantity: quantity.value
    }
    //调用api添加购物车信息
    addCart(item).then(res => {
        //显示成功消息提醒
        ElMessage.success(`已添加 ${quantity.value} 件 ${product.value.name} 到购物车`)
        //关闭加载状态
        loading.value = false
    })
}

//组件挂载时执行的函数
onMounted(() => {
    console.log('加载产品ID:' + route.params.id + '的详情')
    // 根据路由参数中的产品ID获取产品详情
    getProducts(route.params.id).then(res => {
        //将查询到的产品详情信息赋值给product
        product.value = res.data
    })
})
</script>

<style scoped>
/* 主容器样式 */
.product-detail-container {
    max-width: 1450px; /* 最大宽度 */
    margin: 0 auto; /* 水平居中 */
    padding: 20px; /* 内边距 */
}

/* 返回按钮区域样式 */
.back-to-home {
    margin-bottom: 20px; /* 下边距 */
}

/* 返回按钮样式 */
.back-button {
    padding: 0; /* 无内边距 */
    font-size: 16px; /* 字体大小 */
    color: #666; /* 字体颜色 */
}

/* 返回按钮悬停效果 */
.back-button:hover {
    color: #3AAE6E; /* 悬停颜色 */
}

/* 返回按钮图标样式 */
.back-button .el-icon {
    margin-right: 5px; /* 图标右边距 */
}

/* 商品主区域样式 - 使用Flex布局 */
.product-main {
    display: flex; /* Flex布局 */
    margin-bottom: 30px; /* 下边距 */
    background-color: #fff; /* 白色背景 */
    padding: 20px; /* 内边距 */
    border-radius: 8px; /* 圆角 */
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); /* 阴影效果 */
}

/* 商品图片区域样式 */
.product-gallery {
    width: 55%; /* 宽度占比 */
    padding-right: 20px; /* 右边距 */
}

/* 商品图片样式 */
.product-image {
    width: 100%; /* 宽度100% */
    height: 100%; /* 高度100% */
    object-fit: contain; /* 保持图片比例 */
}

/* 商品信息区域样式 */
.product-info {
    width: 50%; /* 宽度占比 */
}

/* 商品标题样式 */
.product-title {
    font-size: 24px; /* 字体大小 */
    margin: 0 0 10px 0; /* 外边距 */
    color: #333; /* 字体颜色 */
}

/* 商品副标题样式 */
.product-subtitle {
    font-size: 14px; /* 字体大小 */
    color: #999; /* 字体颜色 */
    margin-bottom: 15px; /* 下边距 */
}

/* 价格区域样式 */
.product-price-section {
    background-color: #f8f8f8; /* 浅灰色背景 */
    padding: 15px; /* 内边距 */
    border-radius: 4px; /* 圆角 */
    margin-bottom: 20px; /* 下边距 */
}

/* 价格行样式 */
.price-row {
    display: flex; /* Flex布局 */
    align-items: center; /* 垂直居中 */
    margin-bottom: 8px; /* 下边距 */
}

/* 价格标签样式 */
.price-label {
    color: #666; /* 字体颜色 */
    font-size: 14px; /* 字体大小 */
}

/* 当前价格样式 */
.current-price {
    font-size: 28px; /* 字体大小 */
    font-weight: bold; /* 加粗 */
    color: #f56c6c; /* 红色 */
    margin: 0 10px; /* 外边距 */
}

/* 商品来源信息区域样式 */
.product-source {
    margin-bottom: 25px; /* 下边距 */
    padding-bottom: 15px; /* 下内边距 */
    border-bottom: 1px solid #eee; /* 底部边框 */
}

/* 单个来源信息项样式 */
.source-item {
    margin-bottom: 8px; /* 下边距 */
    font-size: 14px; /* 字体大小 */
}

/* 来源信息标签样式 */
.source-item .label {
    color: #666; /* 字体颜色 */
}

/* 来源信息值样式 */
.source-item .value {
    color: #333; /* 字体颜色 */
}

/* 数量选择器区域样式 */
.product-quantity {
    display: flex; /* Flex布局 */
    align-items: center; /* 垂直居中 */
    margin-bottom: 25px; /* 下边距 */
}

/* 数量标签样式 */
.product-quantity .label {
    margin-right: 15px; /* 右边距 */
    font-size: 14px; /* 字体大小 */
    color: #666; /* 字体颜色 */
}

/* 库存显示样式 */
.product-quantity .inventory {
    margin-left: 15px; /* 左边距 */
    font-size: 14px; /* 字体大小 */
    color: #666; /* 字体颜色 */
}

/* 操作按钮区域样式 */
.product-actions {
    display: flex; /* Flex布局 */
    gap: 15px; /* 按钮间距 */
    margin-bottom: 25px; /* 下边距 */
}

/* 操作按钮样式 */
.product-actions .el-button {
    flex: 1; /* 平均分配空间 */
}

/* 商品选项卡区域样式 */
.product-tabs {
    background-color: #fff; /* 白色背景 */
    padding: 20px; /* 内边距 */
    border-radius: 8px; /* 圆角 */
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); /* 阴影效果 */
}

/* 商品详情内容区域样式 */
.product-detail-content {
    padding: 20px 0; /* 上下内边距 */
}

/* 商品详情图片样式 */
.product-detail-content img {
    max-width: 100%; /* 最大宽度 */
    margin: 10px 0; /* 上下外边距 */
}

/* 规格参数内容区域样式 */
.product-specs-content {
    padding: 20px 0; /* 上下内边距 */
}

/* 规格参数表格样式 */
.product-specs-content table {
    width: 100%; /* 宽度100% */
    border-collapse: collapse; /* 边框合并 */
}

/* 表格单元格样式 */
.product-specs-content th,
.product-specs-content td {
    padding: 12px 15px; /* 内边距 */
    border: 1px solid #eee; /* 边框 */
    text-align: left; /* 文本左对齐 */
}

/* 表头样式 */
.product-specs-content th {
    width: 150px; /* 固定宽度 */
    background-color: #f9f9f9; /* 背景色 */
    color: #666; /* 字体颜色 */
}

/* 响应式设计 - 992px以下屏幕 */
@media (max-width: 992px) {
    /* 商品主区域改为垂直布局 */
    .product-main {
        flex-direction: column;
    }

    /* 图片和信息区域宽度100% */
    .product-gallery,
    .product-info {
        width: 100%;
    }

    /* 图片区域调整 */
    .product-gallery {
        padding-right: 0;
        margin-bottom: 30px;
    }

    /* 操作按钮换行 */
    .product-actions {
        flex-wrap: wrap;
    }

    /* 按钮宽度调整 */
    .product-actions .el-button {
        flex: 0 0 calc(50% - 8px);
    }

    /* 最后一个按钮全宽 */
    .product-actions .el-button:last-child {
        width: 100%;
        margin-top: 10px;
    }
}

/* 响应式设计 - 768px以下屏幕 */
@media (max-width: 768px) {
    /* 故事图片调整为全宽 */
    .story-images img {
        width: 100%;
        height: auto;
    }
}
</style>
