<template>
    <div class="app-container">
        <!-- 顶部搜索 -->
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
            <el-form-item label="订单号" prop="ordersId">
                <el-input
                        v-model="queryParams.ordersId"
                        placeholder="请输入订单号"
                        clearable
                        @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="订单状态" prop="status">
                <el-select style="width: 200px;" v-model="queryParams.status" placeholder="请选择订单状态" clearable>
                    <el-option
                            v-for="dict in order_status"
                            :key="dict.value"
                            :label="dict.label"
                            :value="dict.value"
                    />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
        </el-form>

        <!-- 顶部按钮 -->
        <el-row :gutter="10" class="mb8">
            <el-col :span="1.5">
                <el-button
                        type="primary"
                        plain
                        icon="Plus"
                        @click="handleAdd"
                        v-hasPermi="['assisting:orders:add']"
                >新增
                </el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                        type="success"
                        plain
                        icon="Edit"
                        :disabled="single"
                        @click="handleUpdate"
                        v-hasPermi="['assisting:orders:edit']"
                >修改
                </el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                        type="danger"
                        plain
                        icon="Delete"
                        :disabled="multiple"
                        @click="handleDelete"
                        v-hasPermi="['assisting:orders:remove']"
                >删除
                </el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                        type="warning"
                        plain
                        icon="Download"
                        @click="handleExport"
                        v-hasPermi="['assisting:orders:export']"
                >导出
                </el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <!-- 表格 -->
        <el-table @row-click="clickRow" ref="table" highlight-current-row
                  border v-loading="loading" :data="ordersList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center"/>
            <el-table-column label="序号" align="center" type="index" :index="indexMethod"/>
            <el-table-column label="订单号" align="center" prop="ordersId" width="200"/>
            <el-table-column label="所属农户名称" align="center" prop="farmersName"/>
            <el-table-column label="发货时间" align="center" prop="shippingTime" width="180">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.shippingTime, '{y}-{m}-{d}') }}</span>
                </template>
            </el-table-column>
            <el-table-column label="完成时间" align="center" prop="completeTime" width="180">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.completeTime, '{y}-{m}-{d}') }}</span>
                </template>
            </el-table-column>
            <el-table-column label="商品总价" align="center" prop="totalPrice"/>
            <el-table-column label="订单状态" align="center" prop="status">
                <template #default="scope">
                    <dict-tag :options="order_status" :value="scope.row.status"/>
                </template>
            </el-table-column>
            <el-table-column label="下单用户" align="center" prop="userName"/>
            <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button v-if="scope.row.status === '待发货'" link type="success"
                               icon="Van" @click="send(scope.row)"
                               >发货
                    </el-button>
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
                               v-hasPermi="['assisting:orders:edit']">修改
                    </el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                               v-hasPermi="['assisting:orders:remove']">删除
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <!-- 分页组件 -->
        <pagination
                v-show="total>0"
                :total="total"
                v-model:page="queryParams.pageNum"
                v-model:limit="queryParams.pageSize"
                @pagination="getList"
        />

        <!-- 添加或修改订单对话框 -->
        <vxe-modal :title="title" v-model="open" width="500px" show-maximize showFooter resize>
            <el-form ref="ordersRef" :model="form" :rules="rules" label-width="80px">
                <el-form-item label="所属农户UserID" prop="productsUserId">
                    <el-input v-model="form.productsUserId" placeholder="请输入所属农户UserID"/>
                </el-form-item>
                <el-form-item label="发货时间" prop="shippingTime">
                    <el-date-picker clearable
                                    v-model="form.shippingTime"
                                    type="date"
                                    value-format="YYYY-MM-DD"
                                    placeholder="请选择发货时间">
                    </el-date-picker>
                </el-form-item>
                <el-form-item label="完成时间" prop="completeTime">
                    <el-date-picker clearable
                                    v-model="form.completeTime"
                                    type="date"
                                    value-format="YYYY-MM-DD"
                                    placeholder="请选择完成时间">
                    </el-date-picker>
                </el-form-item>
                <el-form-item label="商品总价" prop="totalPrice">
                    <el-input v-model="form.totalPrice" placeholder="请输入商品总价"/>
                </el-form-item>
                <el-form-item label="收货人" prop="name">
                    <el-input v-model="form.name" placeholder="请输入收货人"/>
                </el-form-item>
                <el-form-item label="手机号码" prop="phone">
                    <el-input v-model="form.phone" placeholder="请输入手机号码"/>
                </el-form-item>
                <el-form-item label="收货地址" prop="address">
                    <el-input v-model="form.address" type="textarea" placeholder="请输入内容"/>
                </el-form-item>
                <el-divider content-position="center">订单产品信息</el-divider>
                <el-row :gutter="10" class="mb8">
                    <el-col :span="1.5">
                        <el-button type="primary" icon="Plus" @click="handleAddOrdersProducts">添加</el-button>
                    </el-col>
                    <el-col :span="1.5">
                        <el-button type="danger" icon="Delete" @click="handleDeleteOrdersProducts">删除</el-button>
                    </el-col>
                </el-row>
                <el-table :data="ordersProductsList" :row-class-name="rowOrdersProductsIndex"
                          @selection-change="handleOrdersProductsSelectionChange" ref="ordersProducts">
                    <el-table-column type="selection" width="50" align="center"/>
                    <el-table-column label="序号" align="center" prop="index" width="50"/>
                    <el-table-column label="产品ID" prop="productsId" width="150">
                        <template #default="scope">
                            <el-input v-model="scope.row.productsId" placeholder="请输入产品ID"/>
                        </template>
                    </el-table-column>
                    <el-table-column label="产品名称" prop="productsName" width="150">
                        <template #default="scope">
                            <el-input v-model="scope.row.productsName" placeholder="请输入产品名称"/>
                        </template>
                    </el-table-column>
                    <el-table-column label="产品图片" align="center" prop="image" width="100">
                        <template #default="scope">
                            <image-preview :src="scope.row.image" :width="50" :height="50"/>
                        </template>
                    </el-table-column>
                    <el-table-column label="规格" prop="specs" width="150">
                        <template #default="scope">
                            <el-input v-model="scope.row.specs" placeholder="请输入规格"/>
                        </template>
                    </el-table-column>
                    <el-table-column label="产地" prop="origin" width="150">
                        <template #default="scope">
                            <el-input v-model="scope.row.origin" placeholder="请输入产地"/>
                        </template>
                    </el-table-column>
                    <el-table-column label="价格" prop="price" width="150">
                        <template #default="scope">
                            <el-input v-model="scope.row.price" placeholder="请输入价格"/>
                        </template>
                    </el-table-column>
                    <el-table-column label="数量" prop="quantity" width="150">
                        <template #default="scope">
                            <el-input v-model="scope.row.quantity" placeholder="请输入数量"/>
                        </template>
                    </el-table-column>
                </el-table>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button type="primary" @click="submitForm">确 定</el-button>
                    <el-button @click="cancel">取 消</el-button>
                </div>
            </template>
        </vxe-modal>
    </div>
</template>

<script setup name="Orders">
import {listOrders, getOrders, delOrders, addOrders, updateOrders, sendOutGoods} from "@/api/assisting/orders"
import {getToken} from "@/utils/auth.js";

const baseURL = import.meta.env.VITE_APP_BASE_API

const {proxy} = getCurrentInstance()
const {order_status} = proxy.useDict('order_status')

const ordersList = ref([])
const ordersProductsList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const checkedOrdersProducts = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const selectedRow = ref(null)

const data = reactive({
    form: {},
    queryParams: {
        pageNum: 1,
        pageSize: 10,
        ordersId: null,
        status: null,
        userId: null
    },
    rules: {
        productsUserId: [
            {required: true, message: "所属农户UserID不能为空", trigger: "blur"}
        ],
        totalPrice: [
            {required: true, message: "商品总价不能为空", trigger: "blur"}
        ],
        name: [
            {required: true, message: "收货人不能为空", trigger: "blur"}
        ],
        phone: [
            {required: true, message: "手机号码不能为空", trigger: "blur"}
        ],
        address: [
            {required: true, message: "收货地址不能为空", trigger: "blur"}
        ],
        status: [
            {required: true, message: "订单状态不能为空", trigger: "change"}
        ],
        userId: [
            {required: true, message: "用户ID不能为空", trigger: "blur"}
        ]
    }
})

const {queryParams, form, rules} = toRefs(data)

//发货
const send = (row) => {
    sendOutGoods(row).then(res => {
        proxy.$modal.msgSuccess("发货成功")
        getList()
    })
}

//点击行 获取行
const clickRow = (row) => {
    selectedRow.value = row; // 更新选中的行
    const table = proxy.$refs.table;
    // 清除所有已选中的行
    table.clearSelection();
    // 选中当前点击的行
    table.toggleRowSelection(row, true);
}

/** 自定义序号 */
const indexMethod = (index) => {
    let pageNum = queryParams.value.pageNum - 1;
    if ((pageNum !== -1 && pageNum !== 0)) {
        return (index + 1) + (pageNum * queryParams.value.pageSize);
    } else {
        return (index + 1)
    }
}

/** 查询订单列表 */
const getList = () => {
    loading.value = true
    listOrders(queryParams.value).then(response => {
        ordersList.value = response.rows
        total.value = response.total
        loading.value = false
    })
}

// 取消按钮
const cancel = () => {
    open.value = false
    reset()
}

// 表单重置
const reset = () => {
    form.value = {
        ordersId: null,
        productsUserId: null,
        createTime: null,
        shippingTime: null,
        completeTime: null,
        totalPrice: null,
        name: null,
        phone: null,
        address: null,
        status: null,
        userId: null
    }
    ordersProductsList.value = []
    proxy.resetForm("ordersRef")
}

/** 搜索按钮操作 */
const handleQuery = () => {
    queryParams.value.pageNum = 1
    getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
    proxy.resetForm("queryRef")
    handleQuery()
}

// 多选框选中数据
const handleSelectionChange = (selection) => {
    ids.value = selection.map(item => item.ordersId)
    single.value = selection.length != 1
    multiple.value = !selection.length
}

/** 新增按钮操作 */
const handleAdd = () => {
    reset()
    open.value = true
    title.value = "添加订单"
}

/** 修改按钮操作 */
const handleUpdate = (row) => {
    reset()
    const _ordersId = row.ordersId || ids.value
    getOrders(_ordersId).then(response => {
        form.value = response.data
        ordersProductsList.value = response.data.ordersProductsList
        open.value = true
        title.value = "修改订单"
    })
}

/** 提交按钮 */
const submitForm = () => {
    proxy.$refs["ordersRef"].validate(valid => {
        if (valid) {
            form.value.ordersProductsList = ordersProductsList.value
            if (form.value.ordersId != null) {
                updateOrders(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功")
                    open.value = false
                    getList()
                })
            } else {
                addOrders(form.value).then(response => {
                    proxy.$modal.msgSuccess("新增成功")
                    open.value = false
                    getList()
                })
            }
        }
    })
}

/** 删除按钮操作 */
const handleDelete = (row) => {
    const _ordersIds = row.ordersId || ids.value
    proxy.$modal.confirm('是否确认删除该项数据？').then(function () {
        return delOrders(_ordersIds)
    }).then(() => {
        getList()
        proxy.$modal.msgSuccess("删除成功")
    }).catch(() => {
    })
}

/** 订单产品序号 */
const rowOrdersProductsIndex = ({row, rowIndex}) => {
    row.index = rowIndex + 1
}

/** 订单产品添加按钮操作 */
const handleAddOrdersProducts = () => {
    let obj = {}
    obj.productsId = ""
    obj.productsName = ""
    obj.image = ""
    obj.specs = ""
    obj.origin = ""
    obj.price = ""
    obj.quantity = ""
    ordersProductsList.value.push(obj)
}

/** 订单产品删除按钮操作 */
const handleDeleteOrdersProducts = () => {
    if (checkedOrdersProducts.value.length == 0) {
        proxy.$modal.msgError("请先选择要删除的订单产品数据")
    } else {
        const ordersProductss = ordersProductsList.value
        const checkedOrdersProductss = checkedOrdersProducts.value
        ordersProductsList.value = ordersProductss.filter(function (item) {
            return checkedOrdersProductss.indexOf(item.index) == -1
        })
    }
}

/** 复选框选中数据 */
const handleOrdersProductsSelectionChange = (selection) => {
    checkedOrdersProducts.value = selection.map(item => item.index)
}

/** 导出按钮操作 */
const handleExport = () => {
    proxy.download('assisting/orders/export', {
        ...queryParams.value
    }, `orders_${new Date().getTime()}.xlsx`)
}

getList()
</script>
