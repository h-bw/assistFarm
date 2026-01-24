<template>
  <!-- 页面主容器 -->
    <div class="checkout-container">
        <!-- 返回按钮 -->
        <div class="back-to-home">
            <el-button type="text" @click="router.go(-1)" class="back-button">
                <el-icon>
                    <ArrowLeft/>
                </el-icon>
                返回
            </el-button>
        </div>

        <!-- 结算步骤指示器 -->
        <div class="checkout-steps">
            <el-steps :active="activeStep" finish-status="success" align-center>
                <el-step title="确认订单"/>
                <el-step title="支付"/>
                <el-step title="完成"/>
            </el-steps>
        </div>

        <!-- 第一步: 确认订单 -->
        <div class="checkout-main" v-if="activeStep === 1">
            <!-- 收货地址 -->
            <div class="checkout-section">
                <!-- 有收货地址时显示 -->
                <div class="section-header">
                    <h3>收货地址</h3>
                    <el-button type="text" @click="insertAddress" v-if="addressesList.length !== 0">
                        + 新增地址
                    </el-button>
                </div>
                <!-- 无收货地址时显示 -->
                <div class="no-address-tip" v-if="addressesList.length === 0">
                    <el-icon>
                        <Warning/>
                    </el-icon>
                    <span>您还没有收货地址, 请先新增地址</span>
                    <el-button type="text" @click="insertAddress">
                        + 新增地址
                    </el-button>
                </div>
                <div class="address-list">
                    <div
                            class="address-item"
                            v-for="address in addressesList"
                            :key="address.addressesId"
                            :class="{ 'active': address.addressesId === selectedAddressId }"
                            @click="selectAddress(address)"
                    >
                        <div class="address-info">
                            <div class="address-name">
                                <span>{{ address.name }}</span>
                                <span class="phone">{{ address.phone }}</span>
                                <el-tag v-if="address.isDefault" size="small" type="danger">默认</el-tag>
                            </div>
                            <div class="address-detail">
                                {{ address.detail }}
                            </div>
                        </div>
                        <div class="address-actions">
                            <el-button type="text" @click.stop="updateAddress(address)">编辑</el-button>
                            <el-button type="text" @click.stop="deleteAddress(address.addressesId)">删除</el-button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 按农户分组后的产品订单信息 -->
            <div class="farmer-orders" v-for="farmerOrder in farmerOrders" :key="farmerOrder.ordersId">
                <div class="farmer-header">
                    <span class="farmer-name">{{ farmerOrder.farmerName }}农户的产品</span>
                </div>

                <!-- 产品信息 -->
                <div class="checkout-section">
                    <div class="product-list">
                        <div class="product-item" v-for="item in farmerOrder.ordersProductsList" :key="item.cartId">
                            <img :src="baseUrl + item.image" class="product-image" alt="">
                            <div class="product-info">
                                <div class="product-name">{{ item.productsName }}</div>
                                <div class="product-specs">
                                    <span>规格: {{ item.specs }}</span>
                                </div>
                                <div class="product-farmer">产地: {{ item.origin }}</div>
                            </div>
                            <div class="product-price">¥{{ item.price.toFixed(2) }}</div>
                            <div class="product-quantity">x{{ item.quantity }}</div>
                            <div class="product-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
                        </div>
                    </div>
                </div>

                <!-- 单个农户订单的结算信息 -->
                <div class="checkout-summary">
                    <div class="summary-item total">
                        <span>应付金额</span>
                        <span class="total-amount">¥{{ farmerOrder.totalAmount.toFixed(2) }}</span>
                    </div>
                </div>
            </div>

            <!-- 所有订单的总金额 -->
            <div class="total-orders-summary">
                <div class="summary-item total">
                    <span>共计 {{ farmerOrders.length }} 个订单</span>
                    <span class="total-amount">¥ {{ totalAllOrdersAmount.toFixed(2) }}</span>
                </div>
            </div>

            <!-- 提交订单按钮 -->
            <div class="checkout-actions">
                <el-button type="primary" size="large" @click="submitOrder"
                           :disabled="!selectedAddressId || addressesList.length === 0">
                    提交订单 ({{ farmerOrders.length }}个)
                </el-button>
            </div>
        </div>

        <!-- 第二步: 支付 -->
        <div class="checkout-main" v-if="activeStep === 2">
            <div class="payment-container">
                <div class="payment-info">
                    <div class="order-info">
                        <el-result icon="success" :title="`订单提交成功(来自${currentOrder.farmerName})`"
                                   sub-title="请尽快完成支付">
                            <template #extra>
                                <div class="order-details">
                                    <p>应付金额: <span class="amount">¥{{ currentOrder.totalAmount.toFixed(2) }}</span>
                                    </p>
                                </div>
                            </template>
                        </el-result>
                    </div>

                    <!-- 支付方式选择 -->
                    <div class="payment-methods">
                        <h3 class="payment-title">选择支付方式</h3>
                        <div class="method-list">
                            <!-- 微信支付 -->
                            <div
                                    class="method-item"
                                    :class="{'active': selectedMethod === 'wechat'}"
                                    @click="selectedMethod = 'wechat'"
                            >
                                <div class="method-icon">
                                    <SvgIcon style="font-size: 40px" icon-class="微信"/>
                                </div>
                                <div class="method-name">微信支付</div>
                                <div class="method-check">
                                    <el-icon v-if="selectedMethod === 'wechat'"><Select/></el-icon>
                                </div>
                            </div>
                            <!-- 支付宝支付 -->
                            <div
                                    class="method-item"
                                    :class="{'active': selectedMethod === 'alipay'}"
                                    @click="selectedMethod = 'alipay'"
                            >
                                <div class="method-icon">
                                    <SvgIcon style="font-size: 40px" icon-class="支付宝"/>
                                </div>
                                <div class="method-name">支付宝支付</div>
                                <div class="method-check">
                                    <el-icon v-if="selectedMethod === 'alipay'"><Select/></el-icon>
                                </div>
                            </div>
                            <!-- 银行卡支付 -->
                            <div
                                    class="method-item"
                                    :class="{'active': selectedMethod === 'bank'}"
                                    @click="selectedMethod = 'bank'"
                            >
                                <div class="method-icon">
                                    <SvgIcon style="font-size: 40px" icon-class="银行卡"/>
                                </div>
                                <div class="method-name">银行卡支付</div>
                                <div class="method-check">
                                    <el-icon v-if="selectedMethod === 'bank'"><Select/></el-icon>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="payment-actions">
                    <el-button type="primary" size="large" @click="confirmPayment">立即支付</el-button>
                </div>

            </div>
        </div>

        <!-- 第三步: 支付成功 -->
        <div class="checkout-main" v-if="activeStep === 3">
            <div class="success-container">
                <el-result icon="success" title="支付成功" sub-title="感谢您的购买">
                    <template #extra>
                        <div class="success-details">
                            <p>订单号: {{ orderIdList.map(item => item.msg).join(', ') }}</p>
                            <p>支付金额: ¥{{ currentOrder.totalAmount.toFixed(2) }}</p>
                        </div>
                        <div class="success-actions">
                            <el-button @click="router.push('/index/myOrder')">查看订单</el-button>
                        </div>
                    </template>
                </el-result>
            </div>
        </div>

        <!-- 前台收货地址表单组件 -->
        <AddressFormDialog ref="addressFormDialog" @ok="getMyAddressList"/>
    </div>
</template>

<script setup>
import {useCartStore} from "@/store/modules/cart.js";
import {useRouter} from "vue-router";
import {ArrowLeft, Select, Warning} from "@element-plus/icons-vue";
import useUserStore from "@/store/modules/user.js";
import {delAddresses, listAddresses} from "@/api/assisting/addresses.js";
import {ElLoading, ElMessage, ElMessageBox} from "element-plus";
import AddressFormDialog from "@/views/dialog/AddressFormDialog.vue";
import {addOrders, payment} from "@/api/assisting/orders.js";
import {delCart} from "@/api/assisting/cart.js";
import SvgIcon from "@/components/SvgIcon/index.vue";

//获取api路径
const baseUrl = import.meta.env.VITE_APP_BASE_API

//获取路由实例
const router = useRouter()

//支付方式
const selectedMethod = ref('wechat')

//确认支付方法
const confirmPayment = () => {
    ElMessageBox.confirm(
      `确认支付${currentOrder.value.totalAmount.toFixed(2)}元吗`,
      '提示',
      {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
      }
    )
      .then(() => {
          //打开加载状态
          const loading = ElLoading.service({
              lock: true,
              text: '支付中...',
              background: 'rgba(0, 0, 0, 0.7)',
          })
          //拿到单个或多个订单ID
          let ordersIds = orderIdList.value.map(item => item.msg);
          //调用支付api进行支付
          payment(ordersIds).then(res => {
              //提示支付成功
              ElMessage({type: 'success', message: '支付成功~',})
              //将步骤改为3
              activeStep.value = 3
              //关闭加载状态
              loading.close()
          })

      })
      .catch(() => {
          ElMessage({
              type: 'info',
              message: '取消支付',
          })
      })
}

//提交订单方法
const submitOrder = () => {
    if (!selectedAddressId.value) {
        ElMessage.warning('请选择收货地址')
        return
    }

    ElMessageBox.confirm(
        `确认提交${farmerOrders.value.length}个订单吗?`,
        '提示',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
        }
    )
        .then(async () => {
            try {
                //为每个农户订单创建订单
                const createOrderPromises = farmerOrders.value.map(farmerOrder => {
                    console.log(farmerOrder, '看看每一个订单的信息')
                    return addOrders({
                        productsUserId: farmerOrder.productsUserId,
                        farmerName: farmerOrder.farmerName,
                        totalPrice: farmerOrder.totalAmount,
                        ordersProductsList: farmerOrder.ordersProductsList,
                        name: selectedAddress.value.name,
                        phone: selectedAddress.value.phone,
                        address: selectedAddress.value.detail,
                    })
                })

                //拿到提交订单后返回的单个订单号或多个订单号
                orderIdList.value = await Promise.all(createOrderPromises)

                //提交订单后清理掉购物车中勾选的产品
                const cartsId = checkoutItems.value.map(item => item.cartId);
                await delCart(cartsId)

                // 设置当前订单信息
                currentOrder.value = {
                    farmerName: `${farmerOrders.value.length}个农户`,
                    totalAmount: totalAllOrdersAmount.value
                }

                //步骤变为2(支付步骤)
                activeStep.value = 2
            } catch (error) {
                ElMessage.error('提交订单失败:' + (error.message || '未知错误, 请联系系统管理员'))
            }
            ElMessage({type: 'success', message: '提交订单成功',})
        })
        .catch(() => {
            ElMessage({type: 'info', message: '取消提交',})
        })

}

//提交订单后返回的单个或多个订单列表
const orderIdList = ref([])

//当前正在支付的订单(单个或多个)
const currentOrder = ref({})

//计算所有订单的总金额
const totalAllOrdersAmount = computed(() => {
    return farmerOrders.value.reduce((total, order) => {
        return total + order.totalAmount
    }, 0)
})

//获取购物车 Store 的实例
const cartStore = useCartStore()

//前台收货地址表单组件的实例
const addressFormDialog = ref(null)

//新增地址方法
const insertAddress = () => {
    addressFormDialog.value.handleAdd()
}

//编辑地址方法
const updateAddress = (address) => {
    addressFormDialog.value.handleUpdate(address)
}

//步骤控制, 默认第一步
const activeStep = ref(1)

//删除地址方法
const deleteAddress = (addressesId) => {
    ElMessageBox.confirm(
        '是否确认删除该项地址?',
        '提示',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
        }
    )
        .then(() => {
            //调用api删除地址
            delAddresses(addressesId).then(res => {
                getMyAddressList()
                ElMessage({type: 'success', message: '删除成功',})
            })
        })
        .catch(() => {
            ElMessage({type: 'info', message: '取消删除',})
        })
}

/**
 * 创建结算产品的计算属性
 * 使用computed() 创建一个响应式的计算属性 checkoutItems
 * 它会自动跟踪 cartStore.checkoutItems 的变化
 * 当 cartStore 中的 checkoutItems 发生变化时, 这个计算属性会自动更新
 */
const checkoutItems = computed(() => cartStore.checkoutItems)

// 按农户分组的订单
const farmerOrders = ref([])

//按农户分组产品的方法
const groupItemsByFarmers = (items) => {
    const farmerMap = {}

    items.forEach(item => {
        if (!farmerMap[item.productsUserId]) {
            farmerMap[item.productsUserId] = {
                productsUserId: item.productsUserId,
                farmerName: item.toUserName,
                ordersProductsList: [],
                totalAmount: 0
            }
        }
        farmerMap[item.productsUserId].ordersProductsList.push(item)
        farmerMap[item.productsUserId].totalAmount += item.price * item.quantity
    })

    return Object.values(farmerMap)
}

//选择地址方法
const selectAddress = (address) => {
    selectedAddress.value = address
    selectedAddressId.value = address.addressesId
}

//当前登录用户的信息
const loginUser = useUserStore()

//地址列表查询参数
const queryParams = ref({
    pageNum: 1,
    pageSize: 10,
    name: null,
    isDefault: null,
    userId: loginUser.id,
    userName: null,
})

//地址列表数据
const addressesList = ref([])

//当前选中的地址ID
const selectedAddressId = ref(null)

//当前选中的地址信息
const selectedAddress = ref(null)

//查询自己的地址列表数据
const getMyAddressList = () => {
    //调用api查询数据
    listAddresses(queryParams.value).then(res => {
        //将查询到的自己的地址列表数据赋值给addressesList
        addressesList.value = res.rows

        //检查是否有地址数据, 如果有则设置默认选中地址, 如果没有则选中第一个, 如果一个地址都没有就不选中
        if (addressesList.value.length > 0 && !selectedAddressId.value) {
            selectedAddress.value = addressesList.value.find(item => item.isDefault)
            selectedAddressId.value = selectedAddress.value ? selectedAddress.value.addressesId : addressesList.value[0].addressesId
        }

    })
}

//组件加载时调用
onMounted(() => {
    //查询自己的地址列表
    getMyAddressList()

    //按农户分组产品
    farmerOrders.value = groupItemsByFarmers(checkoutItems.value)
    console.log(farmerOrders.value, '看看分组后的订单列表')
})
</script>

<style scoped>
/* 结算容器样式 */
.checkout-container {
    max-width: 1200px; /* 最大宽度 */
    margin: 0 auto; /* 水平居中 */
    padding: 20px; /* 内边距 */
}

/* 步骤指示器样式 */
.checkout-steps {
    margin-bottom: 30px; /* 底部外边距 */
    padding: 20px; /* 内边距 */
    background-color: #fff; /* 背景色 */
    border-radius: 8px; /* 圆角 */
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); /* 阴影效果 */
}

/* 返回首页按钮样式 */
.back-to-home {
    margin-bottom: 20px; /* 底部外边距 */
}

.back-button {
    padding: 0; /* 内边距清零 */
    font-size: 16px; /* 字体大小 */
    color: #666; /* 字体颜色 */
}

.back-button:hover {
    color: #3AAE6E; /* 悬停颜色 */
}

.back-button .el-icon {
    margin-right: 5px; /* 图标右边距 */
}

/* 主要内容区域样式 */
.checkout-main {
    background-color: #fff; /* 背景色 */
    padding: 20px; /* 内边距 */
    border-radius: 8px; /* 圆角 */
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); /* 阴影效果 */
}

/* 结算区域样式 */
.checkout-section {
    margin-bottom: 30px; /* 底部外边距 */
    padding-bottom: 20px; /* 底部内边距 */
    border-bottom: 1px solid #eee; /* 底部边框 */
}

/* 区域头部样式 */
.section-header {
    display: flex; /* 弹性布局 */
    justify-content: space-between; /* 两端对齐 */
    align-items: center; /* 垂直居中 */
    margin-bottom: 20px; /* 底部外边距 */
}

.section-header h3 {
    margin: 0; /* 外边距清零 */
    font-size: 18px; /* 字体大小 */
    color: #333; /* 字体颜色 */
}

/* 无地址提示样式 */
.no-address-tip {
    display: flex; /* 弹性布局 */
    align-items: center; /* 垂直居中 */
    padding: 12px 15px; /* 内边距 */
    background-color: #f5f5f5; /* 背景色 */
    border-radius: 4px; /* 圆角 */
    margin-bottom: 15px; /* 底部外边距 */
    font-size: 14px; /* 字体大小 */
    color: #666; /* 字体颜色 */
}

.no-address-tip .el-icon {
    margin-right: 8px; /* 图标右边距 */
    color: red; /* 图标颜色 */
}

.no-address-tip .el-button {
    margin-left: 10px; /* 按钮左边距 */
    padding: 0; /* 内边距清零 */
}

/* 地址列表样式 */
.address-list {
    display: flex; /* 弹性布局 */
    flex-wrap: wrap; /* 允许换行 */
    gap: 15px; /* 项间距 */
}

/* 地址项样式 */
.address-item {
    width: calc(50% - 8px); /* 宽度计算，两列布局 */
    padding: 15px; /* 内边距 */
    border: 1px solid #eee; /* 边框 */
    border-radius: 4px; /* 圆角 */
    cursor: pointer; /* 鼠标指针样式 */
    transition: all 0.3s; /* 过渡效果 */
    display: flex; /* 弹性布局 */
    justify-content: space-between; /* 两端对齐 */
}

.address-item:hover {
    border-color: #3AAE6E; /* 悬停边框颜色 */
}

.address-item.active {
    border-color: #3AAE6E; /* 激活状态边框颜色 */
    background-color: #f0f9eb; /* 激活状态背景色 */
}

.address-info {
    flex: 1; /* 弹性扩展 */
}

.address-name {
    font-weight: bold; /* 字体加粗 */
    margin-bottom: 8px; /* 底部外边距 */
    display: flex; /* 弹性布局 */
    align-items: center; /* 垂直居中 */
}

.address-name .phone {
    margin: 0 10px; /* 左右外边距 */
    color: #666; /* 字体颜色 */
}

.address-detail {
    color: #666; /* 字体颜色 */
    font-size: 14px; /* 字体大小 */
}

.address-actions {
    display: flex; /* 弹性布局 */
    align-items: center; /* 垂直居中 */
}

/* 农户订单样式 */
.farmer-orders {
    margin-bottom: 30px; /* 底部外边距 */
    border: 1px solid #eee; /* 边框 */
    border-radius: 8px; /* 圆角 */
    padding: 15px; /* 内边距 */
}

.farmer-header {
    padding: 10px 0; /* 上下内边距 */
    margin-bottom: 15px; /* 底部外边距 */
    border-bottom: 1px solid #eee; /* 底部边框 */
}

.farmer-name {
    font-size: 16px; /* 字体大小 */
    font-weight: bold; /* 字体加粗 */
    color: #333; /* 字体颜色 */
}

/* 商品列表样式 */
.product-list {
    border: 1px solid #eee; /* 边框 */
    border-radius: 4px; /* 圆角 */
}

/* 商品项样式 */
.product-item {
    display: flex; /* 弹性布局 */
    align-items: center; /* 垂直居中 */
    padding: 15px; /* 内边距 */
    border-bottom: 1px solid #eee; /* 底部边框 */
}

.product-item:last-child {
    border-bottom: none; /* 最后一项无底部边框 */
}

.product-image {
    width: 80px; /* 宽度 */
    height: 80px; /* 高度 */
    object-fit: cover; /* 图片填充方式 */
    margin-right: 15px; /* 右边距 */
    border-radius: 4px; /* 圆角 */
}

.product-info {
    flex: 1; /* 弹性扩展 */
    margin-right: 15px; /* 右边距 */
}

.product-name {
    font-size: 14px; /* 字体大小 */
    color: #333; /* 字体颜色 */
    margin-bottom: 5px; /* 底部外边距 */
}

.product-specs {
    font-size: 12px; /* 字体大小 */
    color: #999; /* 字体颜色 */
    margin-bottom: 5px; /* 底部外边距 */
}

.product-specs span {
    margin-right: 8px; /* 右边距 */
}

.product-farmer {
    font-size: 12px; /* 字体大小 */
    color: #666; /* 字体颜色 */
}

.product-price,
.product-quantity,
.product-subtotal {
    width: 100px; /* 宽度 */
    text-align: center; /* 文本居中 */
    font-size: 14px; /* 字体大小 */
    color: #333; /* 字体颜色 */
}

.product-subtotal {
    color: #f56c6c; /* 字体颜色 */
    font-weight: bold; /* 字体加粗 */
}

/* 结算摘要样式 */
.checkout-summary {
    margin-top: 20px; /* 顶部外边距 */
    padding: 15px; /* 内边距 */
    background-color: #f9f9f9; /* 背景色 */
    border-radius: 4px; /* 圆角 */
}

/* 总订单摘要样式 */
.total-orders-summary {
    margin-top: 30px; /* 顶部外边距 */
    padding: 20px; /* 内边距 */
    background-color: #f5f5f5; /* 背景色 */
    border-radius: 4px; /* 圆角 */
}

/* 摘要项样式 */
.summary-item {
    display: flex; /* 弹性布局 */
    justify-content: space-between; /* 两端对齐 */
    margin-bottom: 10px; /* 底部外边距 */
    font-size: 14px; /* 字体大小 */
}

.summary-item.total {
    margin-top: 10px; /* 顶部外边距 */
    padding-top: 10px; /* 顶部内边距 */
    border-top: 1px solid #eee; /* 顶部边框 */
    font-size: 16px; /* 字体大小 */
    color: #333; /* 字体颜色 */
}

.total-amount {
    color: #f56c6c; /* 字体颜色 */
    font-weight: bold; /* 字体加粗 */
    font-size: 18px; /* 字体大小 */
}

/* 结算操作按钮样式 */
.checkout-actions {
    margin-top: 30px; /* 顶部外边距 */
    text-align: right; /* 文本右对齐 */
}

/* 支付容器样式 */
.payment-container {
    padding: 20px; /* 内边距 */
}

.payment-info {
    margin-bottom: 30px; /* 底部外边距 */
}

.order-info {
    margin-bottom: 30px; /* 底部外边距 */
    text-align: center; /* 文本居中 */
}

.order-details {
    margin-top: 20px; /* 顶部外边距 */
    font-size: 14px; /* 字体大小 */
    color: #666; /* 字体颜色 */
}

.order-details p {
    margin: 5px 0; /* 上下外边距 */
}

.order-details .amount {
    color: #f56c6c; /* 字体颜色 */
    font-weight: bold; /* 字体加粗 */
    font-size: 18px; /* 字体大小 */
}

/* 支付方式样式 */
.payment-methods {
    margin-top: 30px; /* 顶部外边距 */
    padding: 20px; /* 内边距 */
    background-color: #fff; /* 背景色 */
    border-radius: 8px; /* 圆角 */
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); /* 阴影效果 */
}

.payment-title {
    margin: 0 0 20px 0; /* 外边距 */
    font-size: 16px; /* 字体大小 */
    color: #333; /* 字体颜色 */
}

.method-list {
    display: flex; /* 弹性布局 */
    flex-direction: column; /* 垂直方向排列 */
    gap: 15px; /* 项间距 */
}

.method-item {
    display: flex; /* 弹性布局 */
    align-items: center; /* 垂直居中 */
    padding: 15px; /* 内边距 */
    border: 1px solid #eee; /* 边框 */
    border-radius: 4px; /* 圆角 */
    cursor: pointer; /* 鼠标指针样式 */
    transition: all 0.3s; /* 过渡效果 */
}

.method-item:hover {
    border-color: #3AAE6E; /* 悬停边框颜色 */
}

.method-item.active {
    border-color: #3AAE6E; /* 激活状态边框颜色 */
    background-color: #f0f9eb; /* 激活状态背景色 */
}

.method-icon {
    width: 40px; /* 宽度 */
    height: 40px; /* 高度 */
    margin-right: 15px; /* 右边距 */
}

.method-icon img {
    width: 100%; /* 宽度100% */
    height: 100%; /* 高度100% */
    object-fit: contain; /* 图片填充方式 */
}

.method-name {
    flex: 1; /* 弹性扩展 */
    font-size: 14px; /* 字体大小 */
    color: #333; /* 字体颜色 */
}

.method-check {
    width: 20px; /* 宽度 */
    color: #3AAE6E; /* 颜色 */
}

.payment-actions {
    display: flex; /* 弹性布局 */
    justify-content: center; /* 水平居中 */
    margin-top: 30px; /* 顶部外边距 */
}

/* 成功页面样式 */
.success-container {
    padding: 50px 0; /* 上下内边距 */
    text-align: center; /* 文本居中 */
}

.success-details {
    margin-top: 20px; /* 顶部外边距 */
    font-size: 14px; /* 字体大小 */
    color: #666; /* 字体颜色 */
}

.success-details p {
    margin: 5px 0; /* 上下外边距 */
}

.success-actions {
    margin-top: 30px; /* 顶部外边距 */
}

/* 响应式设计 - 移动端适配 */
@media (max-width: 768px) {
    .address-item {
        width: 100%; /* 宽度100% */
    }

    .product-item {
        flex-wrap: wrap; /* 允许换行 */
    }

    .product-info {
        width: 100%; /* 宽度100% */
        margin-bottom: 10px; /* 底部外边距 */
    }

    .product-price,
    .product-quantity,
    .product-subtotal {
        width: auto; /* 自动宽度 */
        margin-right: 15px; /* 右边距 */
    }

    .payment-actions {
        flex-direction: column-reverse; /* 垂直反向排列 */
        gap: 15px; /* 项间距 */
    }

    .payment-actions .el-button {
        width: 100%; /* 宽度100% */
    }

    .method-item {
        padding: 10px; /* 内边距 */
    }

    .method-icon {
        width: 30px; /* 宽度 */
        height: 30px; /* 高度 */
        margin-right: 10px; /* 右边距 */
    }
}
</style>
