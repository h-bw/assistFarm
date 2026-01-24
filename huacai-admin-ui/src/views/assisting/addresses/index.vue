<template>
    <div class="app-container">
        <!-- 顶部搜索 -->
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
            <el-form-item label="收货人" prop="name">
                <el-input
                        v-model="queryParams.name"
                        placeholder="请输入收货人"
                        clearable
                        @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="是否默认地址" prop="isDefault">
                <el-input
                        v-model="queryParams.isDefault"
                        placeholder="请输入是否默认地址"
                        clearable
                        @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="创建人" prop="userName">
                <el-input
                        v-model="queryParams.userName"
                        placeholder="请输入创建人"
                        clearable
                        @keyup.enter="handleQuery"
                />
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
                        v-hasPermi="['assisting:addresses:add']"
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
                        v-hasPermi="['assisting:addresses:edit']"
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
                        v-hasPermi="['assisting:addresses:remove']"
                >删除
                </el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                        type="warning"
                        plain
                        icon="Download"
                        @click="handleExport"
                        v-hasPermi="['assisting:addresses:export']"
                >导出
                </el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <!-- 表格 -->
        <el-table @row-click="clickRow" ref="table" highlight-current-row
                  border v-loading="loading" :data="addressesList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center"/>
            <el-table-column label="序号" align="center" type="index" :index="indexMethod"/>
            <el-table-column label="收货人" align="center" prop="name"/>
            <el-table-column label="手机号码" align="center" prop="phone"/>
            <el-table-column label="详细地址" align="center" prop="detail"/>
            <el-table-column label="是否默认地址" align="center" prop="isDefault">
                <template #default="scope">
                    <div v-if="scope.row.isDefault === true">是</div>
                    <div v-else>否</div>
                </template>
            </el-table-column>
            <el-table-column label="创建人" align="center" prop="userName"/>
            <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
                               v-hasPermi="['assisting:addresses:edit']">修改
                    </el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                               v-hasPermi="['assisting:addresses:remove']">删除
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

        <!-- 添加或修改收货地址对话框 -->
        <vxe-modal :title="title" v-model="open" width="500px" show-maximize showFooter resize>
            <el-form ref="addressesRef" :model="form" :rules="rules" label-width="100px">
                <el-form-item label="收货人" prop="name">
                    <el-input v-model="form.name" placeholder="请输入收货人"/>
                </el-form-item>
                <el-form-item label="手机号码" prop="phone">
                    <el-input v-model="form.phone" placeholder="请输入手机号码"/>
                </el-form-item>
                <el-form-item label="是否默认地址" prop="isDefault">
                    <el-switch v-model="form.isDefault" active-text="是" inactive-text="否"/>
                </el-form-item>
                <el-form-item label="详细地址" prop="detail">
                    <el-input v-model="form.detail" type="textarea" placeholder="请输入内容"/>
                </el-form-item>
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

<script setup name="Addresses">
import {addAddresses, delAddresses, getAddresses, listAddresses, updateAddresses} from "@/api/assisting/addresses"
import {getToken} from "@/utils/auth.js";

const baseURL = import.meta.env.VITE_APP_BASE_API

const {proxy} = getCurrentInstance()

const addressesList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
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
        name: null,
        isDefault: null,
        userId: null,
        userName: null,
    },
    rules: {
        name: [
            {required: true, message: "收货人不能为空", trigger: "blur"}
        ],
        phone: [
            {required: true, message: "手机号码不能为空", trigger: "blur"}
        ],
        detail: [
            {required: true, message: "详细地址不能为空", trigger: "blur"}
        ],
    },
})

const {queryParams, form, rules} = toRefs(data)

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

/** 查询收货地址列表 */
const getList = () => {
    loading.value = true
    listAddresses(queryParams.value).then(response => {
        addressesList.value = response.rows
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
        addressesId: null,
        name: null,
        phone: null,
        detail: null,
        isDefault: null,
        userId: null,
        createTime: null
    }
    proxy.resetForm("addressesRef")
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
    ids.value = selection.map(item => item.addressesId)
    single.value = selection.length != 1
    multiple.value = !selection.length
}

/** 新增按钮操作 */
const handleAdd = () => {
    reset()
    open.value = true
    title.value = "添加收货地址"
}

/** 修改按钮操作 */
const handleUpdate = (row) => {
    reset()
    const _addressesId = row.addressesId || ids.value
    getAddresses(_addressesId).then(response => {
        form.value = response.data
        open.value = true
        title.value = "修改收货地址"
    })
}

/** 提交按钮 */
const submitForm = () => {
    proxy.$refs["addressesRef"].validate(valid => {
        if (valid) {
            if (form.value.addressesId != null) {
                updateAddresses(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功")
                    open.value = false
                    getList()
                })
            } else {
                addAddresses(form.value).then(response => {
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
    const _addressesIds = row.addressesId || ids.value
    proxy.$modal.confirm('是否确认删除该项数据？').then(function () {
        return delAddresses(_addressesIds)
    }).then(() => {
        getList()
        proxy.$modal.msgSuccess("删除成功")
    }).catch(() => {
    })
}

/** 导出按钮操作 */
const handleExport = () => {
    proxy.download('assisting/addresses/export', {
        ...queryParams.value
    }, `addresses_${new Date().getTime()}.xlsx`)
}

getList()
</script>
