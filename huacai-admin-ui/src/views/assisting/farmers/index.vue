<template>
    <div class="app-container">
        <!-- 顶部搜索 -->
        <el-form :model="queryParams" ref="queryRef" v-show="showSearch" label-width="120px">
            <el-row :gutter="24" class="mb8">
                <el-col :span="6">
                    <el-form-item label="农户类型" prop="farmerType">
                        <el-select style="width: 100%;" v-model="queryParams.farmerType" placeholder="请选择农户类型"
                                   clearable>
                            <el-option
                                    v-for="dict in famer_type"
                                    :key="dict.value"
                                    :label="dict.label"
                                    :value="dict.value"
                            />
                        </el-select>
                    </el-form-item>
                </el-col>
                <el-col :span="6">
                    <el-form-item label="真实姓名/合作社" prop="realName">
                        <el-input
                                v-model="queryParams.realName"
                                placeholder="请输入真实姓名/合作社名称"
                                clearable
                                @keyup.enter="handleQuery"
                        />
                    </el-form-item>
                </el-col>
                <el-col :span="6">
                    <el-form-item label="认证状态" prop="authStatus">
                        <el-select style="width: 100%;" v-model="queryParams.authStatus" placeholder="请选择认证状态"
                                   clearable>
                            <el-option
                                    v-for="dict in auth_status"
                                    :key="dict.value"
                                    :label="dict.label"
                                    :value="dict.value"
                            />
                        </el-select>
                    </el-form-item>
                </el-col>
                <el-col :span="6">
                    <el-form-item>
                        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>

        <!-- 顶部按钮 -->
        <el-row :gutter="10" class="mb8">
            <el-col :span="1.5">
                <el-button
                        type="primary"
                        plain
                        icon="Plus"
                        @click="handleAdd"
                        v-hasPermi="['assisting:farmers:add']"
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
                        v-hasPermi="['assisting:farmers:edit']"
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
                        v-hasPermi="['assisting:farmers:remove']"
                >删除
                </el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                        type="warning"
                        plain
                        icon="Download"
                        @click="handleExport"
                        v-hasPermi="['assisting:farmers:export']"
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
                  border v-loading="loading" :data="farmersList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center"/>
            <el-table-column label="序号" align="center" type="index" :index="indexMethod"/>
            <el-table-column label="农户类型" align="center" prop="farmerType">
                <template #default="scope">
                    <dict-tag :options="famer_type" :value="scope.row.farmerType"/>
                </template>
            </el-table-column>
            <el-table-column label="真实姓名/合作社名称" align="center" prop="realName"/>
            <el-table-column label="身份证号" align="center" prop="idCard"/>
            <el-table-column label="电话号码" align="center" prop="phone"/>
            <el-table-column label="详细地址" align="center" prop="address"/>
            <el-table-column label="种植/养殖规模" align="center" prop="scale"/>
            <el-table-column label="认证状态" align="center" prop="authStatus">
                <template #default="scope">
                    <dict-tag :options="auth_status" :value="scope.row.authStatus"/>
                </template>
            </el-table-column>
            <el-table-column label="申请人用户名" align="center" prop="userName"/>
            <el-table-column label="详情" align="center">
                <template #default="scope">
                    <el-button type="primary" @click="handleDetail(scope.row)">查看详情</el-button>
                </template>
            </el-table-column>
            <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="230">
                <template #default="scope">
                    <el-button v-if="scope.row.authStatus === '待认证'" link
                               type="success" icon="Select" @click="handleAgree(scope.row)"
                    >通过认证
                    </el-button>
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
                               v-hasPermi="['assisting:farmers:edit']">修改
                    </el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                               v-hasPermi="['assisting:farmers:remove']">删除
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

        <!-- 添加或修改农户对话框 -->
        <vxe-modal height="80vh" :title="title" v-model="open" width="50%" show-maximize showFooter resize>
            <el-form :disabled="isDetail" ref="farmersRef" :model="form" :rules="rules" label-width="80px">
                <el-form-item label="农户类型" prop="farmerType">
                    <el-radio-group v-model="form.farmerType">
                        <el-radio
                                v-for="dict in famer_type"
                                :key="dict.value"
                                :label="dict.value"
                        >{{ dict.label }}
                        </el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="真实姓名/合作社名称" prop="realName">
                    <el-input v-model="form.realName" placeholder="请输入真实姓名/合作社名称"/>
                </el-form-item>
                <el-form-item label="身份证号" prop="idCard">
                    <el-input v-model="form.idCard" placeholder="请输入身份证号"/>
                </el-form-item>
                <el-form-item label="电话号码" prop="phone">
                    <el-input v-model="form.phone" placeholder="请输入电话号码"/>
                </el-form-item>
                <el-form-item label="详细地址" prop="address">
                    <el-input v-model="form.address" type="textarea" placeholder="请输入内容"/>
                </el-form-item>
                <el-form-item label="主要农产品" prop="mainProducts">
                    <el-checkbox-group v-model="form.mainProducts">
                        <el-checkbox
                                v-for="dict in main_products"
                                :key="dict.value"
                                :label="dict.value">
                            {{ dict.label }}
                        </el-checkbox>
                    </el-checkbox-group>
                </el-form-item>
                <el-form-item label="种植/养殖规模" prop="scale">
                    <el-input v-model="form.scale" type="textarea" placeholder="请输入内容"/>
                </el-form-item>
                <el-form-item label="身份证正面" prop="idCardFront">
                    <image-upload v-model="form.idCardFront"/>
                </el-form-item>
                <el-form-item label="身份证反面" prop="idCardBack">
                    <image-upload v-model="form.idCardBack"/>
                </el-form-item>
                <el-form-item label="土地证明/承包合同" prop="landProof">
                    <image-upload v-model="form.landProof"/>
                </el-form-item>
                <el-form-item label="农产品照片" prop="productPhotos">
                    <image-upload v-model="form.productPhotos"/>
                </el-form-item>
                <el-form-item label="其他证明材料" prop="otherProof">
                    <image-upload v-model="form.otherProof"/>
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

<script setup name="Farmers">
import {listFarmers, getFarmers, delFarmers, addFarmers, updateFarmers, agree} from "@/api/assisting/farmers"
import {getToken} from "@/utils/auth.js";
import {ElMessage} from "element-plus";

const baseURL = import.meta.env.VITE_APP_BASE_API

const {proxy} = getCurrentInstance()
const {main_products, famer_type, auth_status} = proxy.useDict('main_products', 'famer_type', 'auth_status')

const farmersList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const selectedRow = ref(null)

//通过认证
const handleAgree = (row) => {
    //拿到当前行的农户ID
    const farmersId = row.farmersId
    agree(farmersId).then(res => {
        //提示
        proxy.$modal.msgSuccess("农户认证成功")
        //更新列表
        getList()
    })
}

//是否查看详情状态
const isDetail = ref(false)

const data = reactive({
    form: {},
    queryParams: {
        pageNum: 1,
        pageSize: 10,
        farmerType: null,
        realName: null,
        authStatus: null,
        userId: null,
    },
    rules: {
        farmerType: [
            {required: true, message: "农户类型不能为空", trigger: "change"}
        ],
        realName: [
            {required: true, message: "真实姓名/合作社名称不能为空", trigger: "blur"}
        ],
        idCard: [
            {required: true, message: "身份证号不能为空", trigger: "blur"}
        ],
        phone: [
            {required: true, message: "电话号码不能为空", trigger: "blur"}
        ],
        address: [
            {required: true, message: "详细地址不能为空", trigger: "blur"}
        ],
        mainProducts: [
            {required: true, message: "主要农产品不能为空", trigger: "blur"}
        ],
        scale: [
            {required: true, message: "种植/养殖规模不能为空", trigger: "blur"}
        ],
        idCardFront: [
            {required: true, message: "身份证正面不能为空", trigger: "blur"}
        ],
        idCardBack: [
            {required: true, message: "身份证反面不能为空", trigger: "blur"}
        ],
        landProof: [
            {required: true, message: "土地证明/承包合同不能为空", trigger: "blur"}
        ],
        productPhotos: [
            {required: true, message: "农产品照片不能为空", trigger: "blur"}
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
        url: baseURL + "/assisting/farmers/importData"
    }
})

const {queryParams, form, rules, upload} = toRefs(data)

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

/** 查询农户列表 */
const getList = () => {
    loading.value = true
    listFarmers(queryParams.value).then(response => {
        farmersList.value = response.rows
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
        farmersId: null,
        farmerType: null,
        realName: null,
        idCard: null,
        phone: null,
        address: null,
        mainProducts: [],
        scale: null,
        idCardFront: null,
        idCardBack: null,
        landProof: null,
        productPhotos: null,
        otherProof: null,
        authStatus: null,
        userId: null,
        createTime: null
    }
    proxy.resetForm("farmersRef")
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
    ids.value = selection.map(item => item.farmersId)
    single.value = selection.length != 1
    multiple.value = !selection.length
}

/** 新增按钮操作 */
const handleAdd = () => {
    //关闭详情状态
    isDetail.value = false
    reset()
    open.value = true
    title.value = "添加农户"
}

/** 修改按钮操作 */
const handleUpdate = (row) => {
    //关闭详情状态
    isDetail.value = false
    reset()
    const _farmersId = row.farmersId || ids.value
    getFarmers(_farmersId).then(response => {
        form.value = response.data
        form.value.mainProducts = form.value.mainProducts.split(",")
        open.value = true
        title.value = "修改农户"
    })
}

//查看详情
const handleDetail = (row) => {
    //打开详情状态
    isDetail.value = true
    reset()
    const _farmersId = row.farmersId || ids.value
    getFarmers(_farmersId).then(response => {
        form.value = response.data
        form.value.mainProducts = form.value.mainProducts.split(",")
        open.value = true
        title.value = "查看农户详情"
    })
}

/** 提交按钮 */
const submitForm = () => {
    proxy.$refs["farmersRef"].validate(valid => {
        if (valid) {
            form.value.mainProducts = form.value.mainProducts.join(",")
            if (form.value.farmersId != null) {
                updateFarmers(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功")
                    open.value = false
                    getList()
                })
            } else {
                addFarmers(form.value).then(response => {
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
    const _farmersIds = row.farmersId || ids.value
    proxy.$modal.confirm('是否确认删除该项数据？').then(function () {
        return delFarmers(_farmersIds)
    }).then(() => {
        getList()
        proxy.$modal.msgSuccess("删除成功")
    }).catch(() => {
    })
}

/** 导出按钮操作 */
const handleExport = () => {
    proxy.download('assisting/farmers/export', {
        ...queryParams.value
    }, `farmers_${new Date().getTime()}.xlsx`)
}

/** 下载模板操作 */
const importTemplate = () => {
    proxy.download('assisting/farmers/importTemplate', {}, `template_${new Date().getTime()}.xlsx`)
}

/** 导入按钮操作 */
const handleImport = () => {
    upload.value.title = "农户导入";
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
