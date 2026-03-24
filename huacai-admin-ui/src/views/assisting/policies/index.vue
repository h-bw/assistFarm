<template>
    <div class="app-container">
        <!-- 顶部搜索 -->
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
            <el-form-item label="政策标题" prop="title">
                <el-input
                        v-model="queryParams.title"
                        placeholder="请输入政策标题"
                        clearable
                        @keyup.enter="handleQuery"
                />
            </el-form-item>
            <el-form-item label="政策分类" prop="category">
                <el-select style="width: 200px;" v-model="queryParams.category" placeholder="请选择政策分类" clearable>
                    <el-option
                            v-for="dict in policies_category"
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
                        v-hasPermi="['assisting:policies:add']"
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
                        v-hasPermi="['assisting:policies:edit']"
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
                        v-hasPermi="['assisting:policies:remove']"
                >删除
                </el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button
                        type="warning"
                        plain
                        icon="Download"
                        @click="handleExport"
                        v-hasPermi="['assisting:policies:export']"
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
                  border v-loading="loading" :data="policiesList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center"/>
            <el-table-column label="序号" align="center" type="index" :index="indexMethod"/>
            <el-table-column label="政策标题" align="center" prop="title"/>
            <el-table-column label="政策分类" align="center" prop="category">
                <template #default="scope">
                    <dict-tag :options="policies_category" :value="scope.row.category"/>
                </template>
            </el-table-column>
            <el-table-column label="适用地区" align="center" prop="region"/>
            <el-table-column label="负责部门" align="center" prop="department"/>
            <el-table-column label="详情" align="center">
                <template #default="scope">
                    <el-button type="primary" @click="handleDetail(scope.row)">查看详情</el-button>
                </template>
            </el-table-column>
            <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
                               v-hasPermi="['assisting:policies:edit']">修改
                    </el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                               v-hasPermi="['assisting:policies:remove']">删除
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

        <!-- 添加或修改助农政策对话框 -->
        <vxe-modal height="90vh" :title="title" v-model="open" width="500px" show-maximize showFooter resize>
            <el-form :disabled="isDetail" ref="policiesRef" :model="form" :rules="rules" label-width="80px">
                <el-form-item label="政策标题" prop="title">
                    <el-input v-model="form.title" placeholder="请输入政策标题"/>
                </el-form-item>
                <el-form-item label="政策分类" prop="category">
                    <el-select v-model="form.category" placeholder="请选择政策分类">
                        <el-option
                                v-for="dict in policies_category"
                                :key="dict.value"
                                :label="dict.label"
                                :value="dict.value"
                        ></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="适用地区" prop="region">
                    <el-input v-model="form.region" placeholder="请输入适用地区"/>
                </el-form-item>
                <el-form-item label="发布单位" prop="publisher">
                    <el-input v-model="form.publisher" placeholder="请输入发布单位"/>
                </el-form-item>
                <el-form-item label="政策摘要" prop="summary">
                    <el-input v-model="form.summary" type="textarea" placeholder="请输入内容"/>
                </el-form-item>
                <el-form-item label="详细内容">
                    <editor v-model="form.content" :min-height="192"/>
                </el-form-item>
                <el-form-item label="负责部门" prop="department">
                    <el-input v-model="form.department" placeholder="请输入负责部门"/>
                </el-form-item>
                <el-form-item label="联系人" prop="contactPerson">
                    <el-input v-model="form.contactPerson" placeholder="请输入联系人"/>
                </el-form-item>
                <el-form-item label="联系电话" prop="phone">
                    <el-input v-model="form.phone" placeholder="请输入联系电话"/>
                </el-form-item>
                <el-form-item label="联系地址" prop="address">
                    <el-input v-model="form.address" placeholder="请输入联系地址"/>
                </el-form-item>
                <el-form-item label="政策来源" prop="sourceLink">
                    <el-input v-model="form.sourceLink" placeholder="请输入政策来源链接"/>
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

<script setup name="Policies">
import {listPolicies, getPolicies, delPolicies, addPolicies, updatePolicies} from "@/api/assisting/policies"
import {getToken} from "@/utils/auth.js";

const baseURL = import.meta.env.VITE_APP_BASE_API

const {proxy} = getCurrentInstance()
const {policies_category} = proxy.useDict('policies_category')

const policiesList = ref([])
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
        title: null,
        category: null,
    },
    rules: {
        title: [
            {required: true, message: "政策标题不能为空", trigger: "blur"}
        ],
        category: [
            {required: true, message: "政策分类不能为空", trigger: "change"}
        ],
        region: [
            {required: true, message: "适用地区不能为空", trigger: "blur"}
        ],
        publisher: [
            {required: true, message: "发布单位不能为空", trigger: "blur"}
        ],
        summary: [
            {required: true, message: "政策摘要不能为空", trigger: "blur"}
        ],
        content: [
            {required: true, message: "详细内容不能为空", trigger: "blur"}
        ],
        department: [
            {required: true, message: "负责部门不能为空", trigger: "blur"}
        ],
        contactPerson: [
            {required: true, message: "联系人不能为空", trigger: "blur"}
        ],
        phone: [
            {required: true, message: "联系电话不能为空", trigger: "blur"}
        ],
        address: [
            {required: true, message: "联系地址不能为空", trigger: "blur"}
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
        url: baseURL + "/assisting/policies/importData"
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

/** 查询助农政策列表 */
const getList = () => {
    loading.value = true
    listPolicies(queryParams.value).then(response => {
        policiesList.value = response.rows
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
        policiesId: null,
        title: null,
        category: null,
        region: null,
        publisher: null,
        summary: null,
        content: null,
        department: null,
        contactPerson: null,
        phone: null,
        address: null,
        sourceLink: null,
        createTime: null
    }
    proxy.resetForm("policiesRef")
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
    ids.value = selection.map(item => item.policiesId)
    single.value = selection.length != 1
    multiple.value = !selection.length
}

//是否查看详情状态
const isDetail = ref(false)

/** 新增按钮操作 */
const handleAdd = () => {
    //关闭详情状态
    isDetail.value = false
    reset()
    open.value = true
    title.value = "添加助农政策"
}

/** 修改按钮操作 */
const handleUpdate = (row) => {
    //关闭详情状态
    isDetail.value = false
    reset()
    const _policiesId = row.policiesId || ids.value
    getPolicies(_policiesId).then(response => {
        form.value = response.data
        open.value = true
        title.value = "修改助农政策"
    })
}

//查看详情
const handleDetail = (row) => {
    //打开详情状态
    isDetail.value = true
    reset()
    const _policiesId = row.policiesId || ids.value
    getPolicies(_policiesId).then(response => {
        form.value = response.data
        open.value = true
        title.value = "修改助农政策"
    })
}

/** 提交按钮 */
const submitForm = () => {
    proxy.$refs["policiesRef"].validate(valid => {
        if (valid) {
            if (form.value.policiesId != null) {
                updatePolicies(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功")
                    open.value = false
                    getList()
                })
            } else {
                addPolicies(form.value).then(response => {
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
    const _policiesIds = row.policiesId || ids.value
    proxy.$modal.confirm('是否确认删除该项数据？').then(function () {
        return delPolicies(_policiesIds)
    }).then(() => {
        getList()
        proxy.$modal.msgSuccess("删除成功")
    }).catch(() => {
    })
}

/** 导出按钮操作 */
const handleExport = () => {
    proxy.download('assisting/policies/export', {
        ...queryParams.value
    }, `policies_${new Date().getTime()}.xlsx`)
}

/** 下载模板操作 */
const importTemplate = () => {
    proxy.download('assisting/policies/importTemplate', {}, `template_${new Date().getTime()}.xlsx`)
}

/** 导入按钮操作 */
const handleImport = () => {
    upload.value.title = "助农政策导入";
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
