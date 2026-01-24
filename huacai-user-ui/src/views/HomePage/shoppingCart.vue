<template>
  <!-- 购物车容器 -->
    <div class="cart-container">
        <!-- 购物车主题部分 -->
        <div class="cart-main" v-loading="loading">
            <!-- 购物车头部区域 -->
            <div class="cart-header">
                <h2>我的购物车</h2>
                <div class="cart-tips">
                    <!-- 显示购物车的商品总数 -->
                    <span>共 {{ total }} 件产品</span>
                </div>
            </div>

            <!-- 购物车为空的提示 -->
            <div class="cart-empty" v-if="cartList.length === 0">
                <el-empty description="购物车空空如也~">
                    <el-button type="primary" @click="router.push('/index/home')">去逛逛</el-button>
                </el-empty>
            </div>

            <!-- 购物车商品列表 (当有商品时显示) -->
            <div class="cart-items" v-else>
                <el-table :data="cartList" style="width: 100%;" border @selection-change="handleSelectionChange">
                    <!-- 选择列(复选框) -->
                    <el-table-column type="selection" width="55" align="center"/>

                    <!-- 商品信息列 -->
                    <el-table-column label="商品信息" width="400">
                        <template #default="{ row }">
                            <div class="product-info-cell">
                                <!-- 商品图片 -->
                                <img :src="baseUrl + row.image" class="product-image" alt="">
                                <div class="product-details">
                                    <!-- 商品名称 -->
                                    <div class="product-name">{{ row.productsName }}</div>
                                    <!-- 商品规格 -->
                                    <div class="product-origin">规格: {{ row.specs }}</div>
                                    <!-- 商品产地 -->
                                    <div class="product-origin">产地: {{ row.origin }}</div>
                                    <!-- 农户信息 -->
                                    <div class="product-origin">来自农户: {{ row.toUserName }}</div>
                                </div>
                            </div>
                        </template>
                    </el-table-column>

                    <!-- 单价列 -->
                    <el-table-column label="单价" width="120" align="center">
                        <template #default="{ row }">
                            <div class="product-price">¥{{ row.price.toFixed(2) }}</div>
                        </template>
                    </el-table-column>

                    <!-- 数量列(带数量选择器) -->
                    <el-table-column label="数量" width="300" align="center">
                        <template #default="{ row }">
                            <el-input-number
                                    v-model="row.quantity"
                                    :min="1"
                                    :max="999"
                                    size="large"
                                    @change="handleQuantityChange(row)"
                            />
                        </template>
                    </el-table-column>

                    <!-- 小计列(单价 * 数量) -->
                    <el-table-column label="小计" align="center">
                        <template #default="{ row }">
                            <div class="product-subtotal">¥{{ (row.price * row.quantity).toFixed(2) }}</div>
                        </template>
                    </el-table-column>

                    <!-- 操作列(删除按钮) -->
                    <el-table-column label="操作" width="80" align="center">
                        <template #default="{ row }">
                            <el-button
                                    type="danger"
                                    size="small"
                                    :icon="Delete"
                                    circle
                                    @click="removeItem(row.cartId)"
                            />
                        </template>
                    </el-table-column>
                </el-table>
            </div>

            <!-- 购物车底部结算栏 (当有产品时显示) -->
            <div class="cart-footer" v-if="cartList.length > 0">
                <div class="footer-left">
                    <div class="total-info">
                        <!-- 合计金额 -->
                        <div class="total-amount">
                            合计: <span class="amount">¥{{ totalAmount.toFixed(2) }}</span>
                        </div>
                    </div>
                </div>
                <div class="footer-right">
                    <!-- 结算按钮 (当选中商品时才可点击)-->
                    <el-button
                            type="primary"
                            size="large"
                            @click="checkout"
                            :disabled="selectedItems.length === 0"
                    >
                        去结算
                    </el-button>
                </div>
            </div>

        </div>
    </div>
</template>

<script setup>
import {delCart, listCart, updateCart} from "@/api/assisting/cart.js";
import {useRouter} from "vue-router";
import {Delete} from "@element-plus/icons-vue";
import {ElMessage, ElMessageBox} from "element-plus";
import useUserStore from "@/store/modules/user.js";
import {useCartStore} from "@/store/modules/cart.js";

//获取路由实例
const router = useRouter()

//获取基础URL(从环境变量)
const baseUrl = import.meta.env.VITE_APP_BASE_API

//选中的商品数组
const selectedItems = ref([])

//获取购物车 Store 的实例
const cartStore = useCartStore()

//去结算的方法
const checkout = () => {
    if (selectedItems.value.length === 0) {
        ElMessage.warning('请先选择要结算的产品')
        return
    }

    //将选中的商品存储到状态管理
    cartStore.setCheckoutItems(selectedItems.value)

    // 跳转到结算页面
    router.push('/index/checkout')
}

//处理表格选择变化事件
const handleSelectionChange = (selection) => {
    selectedItems.value = selection
}

//处理商品数量变化的事件
const handleQuantityChange = (item) => {
    const form = {
        cartId: item.cartId,
        quantity: item.quantity
    }
    //调用api更新购物车中的商品数量
    updateCart(form)
}

//从购物车中删除单个商品
const removeItem = (cartId) => {
    //显示确认对话框
    ElMessageBox.confirm(
        '确认删除该商品吗?',
        '提示',
        {confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning',}
    )
        .then(() => {
            //用户确认后调用删除api
            delCart(cartId).then(res => {
                //删除成功后刷新列表
                getList()
                ElMessage({type: 'success', message: '删除成功',})
            })
        })
        .catch(() => {
            ElMessage({type: 'info', message: '取消删除',})
        })
}

//计算总金额 ((选中商品的价格 * 数量)的总和)
const totalAmount = computed(() => {
    return selectedItems.value.reduce((total, item) => total + (item.price * item.quantity), 0)
})

//加载状态
const loading = ref(false)

//获取当前登录用户的信息
const loginUser = useUserStore()

//查询参数
const queryParams = ref({
    pageNum: 1,
    pageSize: 10,
    productsId: null,
    userId: loginUser.id,
    productsName: null,
    createUserName: null,
    toUserName: null,
})

//购物车数据
const cartList = ref([])

//数据综述
const total = ref(0)

//获取购物车列表数据的方法
const getList = () => {
    //打开加载状态
    loading.value = true
    //调用api接口查询购物车列表数据
    listCart(queryParams.value).then(res => {
        //将数据总数赋值给total
        total.value = res.total
        //将查询到的列表数据赋值给cartList
        cartList.value = res.rows
        //关闭加载状态
        loading.value = false
    })
}

//组件加载时自动获取购物策划列表
onMounted(() => {
    getList()
})
</script>

<style scoped>
/* 购物车容器样式 */
.cart-container {
    max-width: 1450px; /* 最大宽度 */
    margin: 0 auto; /* 水平居中 */
    padding: 20px; /* 内边距 */
}

/* 购物车主体样式 */
.cart-main {
    background-color: #fff; /* 白色背景 */
    padding: 20px; /* 内边距 */
    border-radius: 8px; /* 圆角 */
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); /* 阴影效果 */
}

/* 购物车头部样式 */
.cart-header {
    display: flex; /* 弹性布局 */
    justify-content: space-between; /* 两端对齐 */
    align-items: center; /* 垂直居中 */
    padding-bottom: 15px; /* 底部内边距 */
    border-bottom: 1px solid #eee; /* 底部边框 */
    margin-bottom: 20px; /* 底部外边距 */
}

/* 购物车标题样式 */
.cart-header h2 {
    margin: 0; /* 清除默认外边距 */
    font-size: 22px; /* 字体大小 */
    color: #333; /* 字体颜色 */
}

/* 购物车提示信息样式 */
.cart-tips {
    display: flex; /* 弹性布局 */
    align-items: center; /* 垂直居中 */
}

/* 购物车提示文字样式 */
.cart-tips span {
    margin-right: 20px; /* 右侧外边距 */
    font-size: 14px; /* 字体大小 */
    color: #666; /* 字体颜色 */
}

/* 购物车为空时的样式 */
.cart-empty {
    padding: 50px 0; /* 上下内边距 */
    text-align: center; /* 文字居中 */
}

/* 购物车商品列表样式 */
.cart-items {
    margin-bottom: 30px; /* 底部外边距 */
}

/* 商品信息单元格样式 */
.product-info-cell {
    display: flex; /* 弹性布局 */
    align-items: center; /* 垂直居中 */
}

/* 商品图片样式 */
.product-image {
    width: 80px; /* 宽度 */
    height: 80px; /* 高度 */
    object-fit: cover; /* 图片填充方式 */
    margin-right: 15px; /* 右侧外边距 */
    border-radius: 4px; /* 圆角 */
}

/* 商品详情样式 */
.product-details {
    flex: 1; /* 占据剩余空间 */
}

/* 商品名称样式 */
.product-name {
    font-size: 14px; /* 字体大小 */
    color: #333; /* 字体颜色 */
    margin-bottom: 5px; /* 底部外边距 */
    display: -webkit-box; /* 设置显示方式 */
    -webkit-line-clamp: 2; /* 限制显示行数 */
    -webkit-box-orient: vertical; /* 垂直方向 */
    overflow: hidden; /* 溢出隐藏 */
}

/* 商品来源信息样式 */
.product-origin {
    font-size: 12px; /* 字体大小 */
    color: #666; /* 字体颜色 */
}

/* 商品价格样式 */
.product-price {
    font-size: 16px; /* 字体大小 */
    color: #f56c6c; /* 红色 */
    font-weight: bold; /* 加粗 */
}

/* 商品小计样式 */
.product-subtotal {
    font-size: 16px; /* 字体大小 */
    color: #f56c6c; /* 红色 */
    font-weight: bold; /* 加粗 */
}

/* 购物车底部样式 */
.cart-footer {
    display: flex; /* 弹性布局 */
    justify-content: space-between; /* 两端对齐 */
    align-items: center; /* 垂直居中 */
    padding: 15px 20px; /* 内边距 */
    background-color: #f9f9f9; /* 浅灰色背景 */
    border-radius: 4px; /* 圆角 */
    margin-bottom: 30px; /* 底部外边距 */
}

/* 底部左侧区域样式 */
.footer-left {
    display: flex; /* 弹性布局 */
    align-items: center; /* 垂直居中 */
}

/* 底部右侧区域样式 */
.footer-right {
    display: flex; /* 弹性布局 */
    align-items: center; /* 垂直居中 */
}

/* 合计信息样式 */
.total-info {
    margin-right: 30px; /* 右侧外边距 */
    text-align: right; /* 文字右对齐 */
}

/* 合计金额样式 */
.total-amount {
    font-size: 18px; /* 字体大小 */
    color: #333; /* 字体颜色 */
    margin-bottom: 5px; /* 底部外边距 */
}

/* 金额数字样式 */
.total-amount .amount {
    color: #f56c6c; /* 红色 */
    font-weight: bold; /* 加粗 */
}

/* 响应式设计 - 小于992px时的样式 */
@media (max-width: 992px) {
    .cart-footer {
        flex-direction: column; /* 垂直排列 */
        align-items: flex-start; /* 左对齐 */
    }

    .footer-right {
        width: 100%; /* 全宽 */
        justify-content: space-between; /* 两端对齐 */
        margin-top: 15px; /* 顶部外边距 */
    }
}

/* 响应式设计 - 小于768px时的样式 */
@media (max-width: 768px) {
    /* 商品信息单元格改为垂直排列 */
    .product-info-cell {
        flex-direction: column; /* 垂直排列 */
        align-items: flex-start; /* 左对齐 */
    }

    /* 调整商品图片间距 */
    .product-image {
        margin-right: 0; /* 清除右侧外边距 */
        margin-bottom: 10px; /* 底部外边距 */
    }
}
</style>
