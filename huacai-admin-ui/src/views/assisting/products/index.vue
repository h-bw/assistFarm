<template>
    <div class="app-container">
        <!-- 顶部搜索 -->
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
            <el-form-item label="产品名称" prop="name">
                <el-input
                        v-model="queryParams.name"
                        placeholder="请输入产品名称"
                        clearable
                        @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="创建人用户名" prop="userName">
                <el-input
                        v-model="queryParams.userName"
                        placeholder="请输入创建人用户名"
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
                        v-hasPermi="['assisting:products:add']"
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
                        v-hasPermi="['assisting:products:edit']"
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
                        v-hasPermi="['assisting:products:remove']"
                >删除
                </el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                        type="warning"
                        plain
                        icon="Download"
                        @click="handleExport"
                        v-hasPermi="['assisting:products:export']"
                >导出
                </el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                        type="primary"
                        icon="Upload"
                        size="mini"
                        @click="handleImport"
                >导入
                </el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <!-- 表格 -->
        <el-table @row-click="clickRow" ref="table" highlight-current-row
                  border v-loading="loading" :data="productsList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center"/>
            <el-table-column label="序号" align="center" type="index" :index="indexMethod"/>
            <el-table-column label="产品名称" align="center" prop="name"/>
            <el-table-column label="简介" align="center" prop="subtitle"/>
            <el-table-column label="价格" align="center" prop="price"/>
            <el-table-column label="图片" align="center" prop="image" width="100">
                <template #default="scope">
                    <image-preview :src="scope.row.image" :width="50" :height="50"/>
                </template>
            </el-table-column>
            <el-table-column label="产地" align="center" prop="origin"/>
            <el-table-column label="发货地" align="center" prop="shipFrom"/>
            <el-table-column label="库存" align="center" prop="inventory"/>
            <el-table-column label="创建人用户名" align="center" prop="userName"/>
            <el-table-column label="详情" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button type="primary" @click="handleDetail(scope.row)"
                    >查看详情
                    </el-button>
                </template>
            </el-table-column>
            <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="success" icon="CirclePlus" @click="handleReplenish(scope.row)">
                        补货
                    </el-button>
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
                               v-hasPermi="['assisting:products:edit']">修改
                    </el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                               v-hasPermi="['assisting:products:remove']">删除
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

        <!-- 导入对话框 -->
        <vxe-modal :title="upload.title" v-model="upload.open" width="400px" showFooter show-zoom resize>
            <el-upload
                    ref="uploadRef"
                    :limit="1"
                    accept=".xlsx, .xls"
                    :headers="upload.headers"
                    :action="upload.url"
                    :data="{ updateSupport: upload.updateSupport }"
                    :disabled="upload.isUploading"
                    :on-progress="handleFileUploadProgress"
                    :on-success="handleFileSuccess"
                    :auto-upload="false"
                    drag
            >
                <i class="el-icon-upload"></i>
                <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
                <div class="el-upload__tip text-center" slot="tip">
                    <span>仅允许导入xls、xlsx格式文件。</span>
                    <el-link type="primary" :underline="false" style="font-size:12px;vertical-align: baseline;"
                             @click="importTemplate">下载模板
                    </el-link>
                </div>
            </el-upload>
            <div slot="footer" class="dialog-footer">
                <el-button type="primary" @click="submitFileForm">确 定</el-button>
                <el-button @click="upload.open = false">取 消</el-button>
            </div>
        </vxe-modal>

        <!-- 添加或修改农户产品对话框 -->
        <vxe-modal height="90vh" :title="title" v-model="open" width="500px" show-maximize showFooter resize>
            <el-form :disabled="isDetail" ref="productsRef" :model="form" :rules="rules" label-width="80px">
                <el-form-item label="产品名称" prop="name">
                    <el-input v-model="form.name" placeholder="请输入产品名称"/>
                </el-form-item>
                <el-form-item label="简介" prop="subtitle">
                    <el-input v-model="form.subtitle" placeholder="请输入简介"/>
                </el-form-item>
                <el-form-item label="价格" prop="price">
                    <el-input v-model="form.price" placeholder="请输入价格"/>
                </el-form-item>
                <el-form-item label="图片" prop="image">
                    <image-upload v-model="form.image"/>
                </el-form-item>
                <el-form-item label="产地" prop="origin">
                    <el-input v-model="form.origin" placeholder="请输入产地"/>
                </el-form-item>
                <el-form-item label="发货地" prop="shipFrom">
                    <el-input v-model="form.shipFrom" placeholder="请输入发货地"/>
                </el-form-item>
                <el-form-item label="规格" prop="specs">
                    <el-input v-model="form.specs" placeholder="请输入规格"/>
                </el-form-item>
                <el-form-item label="保质期" prop="expire">
                    <el-input v-model="form.expire" placeholder="请输入保质期"/>
                </el-form-item>
                <el-form-item label="储存方法" prop="storage">
                    <el-input v-model="form.storage" placeholder="请输入储存方法"/>
                </el-form-item>
                <el-form-item label="食用方法" prop="edible">
                    <el-input v-model="form.edible" placeholder="请输入食用方法"/>
                </el-form-item>
                <el-form-item label="详情" prop="detail">
                    <editor v-model="form.detail" :height="300"/>
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

<script setup name="Products">
import {listProducts, getProducts, delProducts, addProducts, updateProducts, replenish} from "@/api/assisting/products"
import {getToken} from "@/utils/auth.js";
import {ElMessage, ElMessageBox} from "element-plus";

const baseURL = import.meta.env.VITE_APP_BASE_API

const {proxy} = getCurrentInstance()

const productsList = ref([])
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
        userId: null,
        userName: null
    },
    rules: {
        name: [
            {required: true, message: "产品名称不能为空", trigger: "blur"}
        ],
        subtitle: [
            {required: true, message: "简介不能为空", trigger: "blur"}
        ],
        price: [
            {required: true, message: "价格不能为空", trigger: "blur"}
        ],
        image: [
            {required: true, message: "图片不能为空", trigger: "blur"}
        ],
        origin: [
            {required: true, message: "产地不能为空", trigger: "blur"}
        ],
        shipFrom: [
            {required: true, message: "发货地不能为空", trigger: "blur"}
        ],
        inventory: [
            {required: true, message: "库存不能为空", trigger: "blur"}
        ],
        specs: [
            {required: true, message: "规格不能为空", trigger: "blur"}
        ],
        expire: [
            {required: true, message: "保质期不能为空", trigger: "blur"}
        ],
        storage: [
            {required: true, message: "储存方法不能为空", trigger: "blur"}
        ],
        edible: [
            {required: true, message: "食用方法不能为空", trigger: "blur"}
        ],
        detail: [
            {required: true, message: "详情不能为空", trigger: "blur"}
        ],
        userId: [
            {required: true, message: "用户ID不能为空", trigger: "blur"}
        ],
    },
    // 导入参数
    upload: {
        // 是否显示弹出层（导入）
        open: false,
        // 弹出层标题（导入）
        title: "",
        // 是否禁用上传
        isUploading: false,
        // 设置上传的请求头部
        headers: {Authorization: "Bearer " + getToken()},
        // 上传的地址
        url: baseURL + "/assisting/products/importData"
    }
})

const {queryParams, form, rules, upload} = toRefs(data)

//补货按钮操作
const handleReplenish = (row) => {
    ElMessageBox.prompt('请输入补货数量', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern:
          /^[1-9]\d*$/,
        inputErrorMessage: '请输入有效的数字',
    })
      .then(({ value }) => {
          const item = {
              productsId: row.productsId,
              count: value
          }
          //调用API更新库存
          replenish(item).then(res => {
              //更新库存成功后刷新列表
              getList()
              ElMessage({type: 'success', message: `已为 ${row.name} 补货 ${value} 件`,})
          })
      })
      .catch(() => {
          ElMessage({type: 'info', message: '已取消补货',})
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

/** 查询农户产品列表 */
const getList = () => {
    loading.value = true
    listProducts(queryParams.value).then(response => {
        productsList.value = response.rows
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
        productsId: null,
        name: null,
        subtitle: null,
        price: null,
        image: null,
        origin: null,
        shipFrom: null,
        inventory: null,
        specs: null,
        expire: null,
        storage: null,
        edible: null,
        detail: null,
        userId: null,
        createTime: null
    }
    proxy.resetForm("productsRef")
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
    ids.value = selection.map(item => item.productsId)
    single.value = selection.length != 1
    multiple.value = !selection.length
}

/** 新增按钮操作 */
const handleAdd = () => {
    //关闭详情状态
    isDetail.value = false
    reset()
    open.value = true
    title.value = "添加农户产品"
}

/** 修改按钮操作 */
const handleUpdate = (row) => {
    //关闭详情状态
    isDetail.value = false
    reset()
    const _productsId = row.productsId || ids.value
    getProducts(_productsId).then(response => {
        form.value = response.data
        open.value = true
        title.value = "修改农户产品"
    })
}

//是否查看详情状态
const isDetail = ref(false)

//查看详情
const handleDetail = (row) => {
    //打开详情状态
    isDetail.value = true
    reset()
    const _productsId = row.productsId || ids.value
    getProducts(_productsId).then(response => {
        form.value = response.data
        open.value = true
        title.value = "查看产品详情"
    })
}

/** 提交按钮 */
const submitForm = () => {
    proxy.$refs["productsRef"].validate(valid => {
        if (valid) {
            if (form.value.productsId != null) {
                updateProducts(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功")
                    open.value = false
                    getList()
                })
            } else {
                addProducts(form.value).then(response => {
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
    const _productsIds = row.productsId || ids.value
    proxy.$modal.confirm('是否确认删除该项数据？').then(function () {
        return delProducts(_productsIds)
    }).then(() => {
        getList()
        proxy.$modal.msgSuccess("删除成功")
    }).catch(() => {
    })
}

/** 导出按钮操作 */
const handleExport = () => {
    proxy.download('assisting/products/export', {
        ...queryParams.value
    }, `products_${new Date().getTime()}.xlsx`)
}

/** 下载模板操作 */
const importTemplate = () => {
    proxy.download('assisting/products/importTemplate', {}, `template_${new Date().getTime()}.xlsx`)
}

/** 导入按钮操作 */
const handleImport = () => {
    upload.value.title = "农户产品导入";
    upload.value.open = true;
}

// 文件上传中处理
const handleFileUploadProgress = (event, file, fileList) => {
    upload.value.isUploading = true;
}

// 文件上传成功处理
const handleFileSuccess = (response, file, fileList) => {
    upload.value.open = false;
    upload.value.isUploading = false;
    proxy.$refs.uploadRef.clearFiles();
    proxy.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "导入结果", {dangerouslyUseHTMLString: true});
    getList();
}

// 提交上传文件
const submitFileForm = () => {
    proxy.$refs.uploadRef.submit();
}

getList()
</script>
