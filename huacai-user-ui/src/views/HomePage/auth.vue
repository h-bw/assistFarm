<template>
  <!-- 主容器 -->
    <div class="farmer-auth-container">
        <!-- 返回按钮 -->
        <div class="back-to-home">
            <el-button type="text" @click="router.go(-1)" class="back-button">
                <el-icon>
                    <ArrowLeft/>
                </el-icon>
                返回
            </el-button>
        </div>

        <!-- 根据认证状态显示不同内容 -->
        <!-- 1.未提交状态 -->
        <div v-if="authStatus === '未提交'" class="farmer-auth-container">
            <!-- 认证申请头部 -->
            <div class="auth-header">
                <h2>农户认证申请</h2>
                <p>通过认证后, 您可以在平台上销售产品, 获得更多销售渠道</p>
            </div>

            <!-- 步骤条 -->
            <div class="auth-steps">
                <el-steps :active="activeStep" finish-status="success">
                    <el-step title="基本信息"/>
                    <el-step title="农户认证"/>
                    <el-step title="提交审核"/>
                </el-steps>
            </div>

            <!-- 表单容器 -->
            <div class="auth-form-container">
                <!-- 第一步: 基本信息 -->
                <div v-show="activeStep === 0" class="auth-form-section">
                    <el-form :model="form" :rules="rules" ref="baseInfoRef" label-width="160px">
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
                            <el-input v-model="form.realName" placeholder="请输入您的真实姓名或者合作社名称"/>
                        </el-form-item>

                        <el-form-item label="身份证号" prop="idCard">
                            <el-input v-model="form.idCard" placeholder="请输入您的身份证号"/>
                        </el-form-item>

                        <el-form-item label="电话号码" prop="phone">
                            <el-input v-model="form.phone" placeholder="请输入您的电话号码"/>
                        </el-form-item>

                        <el-form-item label="详细地址" prop="address">
                            <el-input v-model="form.address" type="textarea" placeholder="请输入详细地址"/>
                        </el-form-item>

                        <el-form-item label="主要农产品(可多选)" prop="mainProducts">
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
                            <el-input v-model="form.scale" type="textarea" placeholder="例: 5亩苹果园 200头生猪等"/>
                        </el-form-item>
                    </el-form>

                    <!-- 表单操作按钮 -->
                    <div class="form-actions">
                        <el-button type="primary" @click="nextStep">下一步</el-button>
                    </div>
                </div>

                <!-- 第二步: 农户证明 -->
                <div v-show="activeStep === 1" class="auth-form-section">
                    <el-form :model="form" :rules="rules" ref="proofRef" label-width="150px">
                        <el-form-item label="身份证正面" prop="idCardFront">
                            <div style="display: block">
                                <image-upload v-model="form.idCardFront"/>
                                <div class="upload-tip">请上传清晰的身份证正面照片</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="身份证反面" prop="idCardBack">
                            <div style="display: block">
                                <image-upload v-model="form.idCardBack"/>
                                <div class="upload-tip">请上传清晰的身份证反面照片</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="土地证明/承包合同" prop="landProof">
                            <div style="display: block">
                                <image-upload :limit="3" v-model="form.landProof"/>
                                <div class="upload-tip">请上传徒弟证明或承包合同照片(最多3张)</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="农产品照片" prop="productPhotos">
                            <div style="display: block">
                                <image-upload :limit="5" v-model="form.productPhotos"/>
                                <div class="upload-tip">请上传您的农产品照片(最多5张)</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="其他证明材料" prop="otherProof">
                            <div style="display: block">
                                <image-upload :limit="20" v-model="form.otherProof"/>
                                <div class="upload-tip">可上传其他证明材料(如合作社证明、荣誉证书等)</div>
                            </div>
                        </el-form-item>
                    </el-form>

                    <!-- 表单操作按钮 -->
                    <div class="form-actions">
                        <el-button @click="prevStep">上一步</el-button>
                        <el-button type="primary" @click="nextStep">下一步</el-button>
                    </div>
                </div>

                <!-- 第三步: 提交审核 -->
                <div v-show="activeStep === 2" class="auth-form-section">
                    <div class="review-section">
                        <h3>请确认您的认证信息</h3>

                        <!-- 信息确认展示 -->
                        <el-descriptions :column="1" border>
                            <el-descriptions-item label="真实姓名">{{ form.realName }}</el-descriptions-item>
                            <el-descriptions-item label="身份证号">{{ form.idCard }}</el-descriptions-item>
                            <el-descriptions-item label="联系电话">{{ form.phone }}</el-descriptions-item>
                            <el-descriptions-item label="详细地址">{{ form.address }}</el-descriptions-item>
                            <el-descriptions-item label="农户类型">{{ form.farmerType }}</el-descriptions-item>
                            <el-descriptions-item label="主要农产品">{{ form.mainProducts }}</el-descriptions-item>
                            <el-descriptions-item label="种植/养殖规模">{{ form.scale }}</el-descriptions-item>
                        </el-descriptions>

                        <!-- 上传材料预览 -->
                        <el-form disabled>
                            <div class="upload-review">
                                <h4>上传材料预览</h4>
                                <div class="upload-item">
                                    <h5>身份证正面</h5>
                                    <div class="preview-images">
                                        <image-upload v-model="form.idCardFront"/>
                                    </div>
                                </div>

                                <div class="upload-item">
                                    <h5>身份证正面</h5>
                                    <div class="preview-images">
                                        <image-upload v-model="form.idCardBack"/>
                                    </div>
                                </div>

                                <div class="upload-item">
                                    <h5>土地证明/承包合同</h5>
                                    <div class="preview-images">
                                        <image-upload v-model="form.landProof"/>
                                    </div>
                                </div>

                                <div class="upload-item">
                                    <h5>农产品照片</h5>
                                    <div class="preview-images">
                                        <image-upload v-model="form.productPhotos"/>
                                    </div>
                                </div>

                                <div class="upload-item" v-if="form.idCardFront">
                                    <h5>其他证明材料</h5>
                                    <div class="preview-images">
                                        <image-upload v-model="form.otherProof"/>
                                    </div>
                                </div>
                            </div>
                        </el-form>
                    </div>

                    <!-- 表单操作区域 -->
                    <div class="form-actions">
                        <el-button @click="prevStep">上一步</el-button>
                        <el-button type="primary" :loading="loading" @click="submitForm">
                            {{ loading ? '提交中...' : '提交认证' }}
                        </el-button>
                    </div>
                </div>

            </div>
        </div>

        <!-- 2.待认证状态 -->
        <div v-else-if="authStatus === '待认证'">
            <div class="status-container">
                <el-result icon="info" title="认证审核中">
                    <template #extra>
                        <p>我们已收到您的认证申请, 预计3个工作日内完成审核</p>
                    </template>
                </el-result>
            </div>
        </div>

        <!-- 3.认证通过状态 -->
        <div v-else>
            <div class="status-container">
                <el-result icon="success" title="认证已通过">
                    <template #extra>
                        <p>恭喜! 您已认证通过</p>
                        <el-button type="primary" @click="toManage">进入农户后台管理</el-button>
                    </template>
                </el-result>
            </div>
        </div>

    </div>
</template>

<script setup>
import {useRouter} from "vue-router";
import {ArrowLeft} from "@element-plus/icons-vue";
import {ElMessage} from "element-plus";
import {addFarmers, selectIsAuth} from "@/api/assisting/farmers.js";

//获取当前组件实例
const {proxy} = getCurrentInstance()
//字典数据
const {main_products, famer_type, auth_status} = proxy.useDict('main_products', 'famer_type', 'auth_status')

//引入后台管理地址
const backendUrl = import.meta.env.VITE_APP_BACKEND_URL

//进入后台管理
const toManage = () => {
  window.open(backendUrl)
}

//路由实例
const router = useRouter()

//当前步骤
const activeStep = ref(0)

//表单数据
const form = ref({
    farmerType: '个体农户', //农户类型
    realName: null, //真实姓名/合作社名称
    idCard: null, //身份证号
    phone: null, //联系电话
    address: null, //详细地址
    mainProducts: [], //主要农产品
    scale: null, //规模
    idCardFront: null, //身份证正面
    idCardBack: null, //身份证反面
    landProof: null, //土地证明
    productPhotos: null, //产品照片
    otherProof: null, //其他证明
})

//表单验证规则
const rules = {
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
}

//加载状态
const loading = ref(false)

//表单引用
const baseInfoRef = ref(null)
const proofRef = ref(null)

//下一步操作
const nextStep = () => {
    if (activeStep.value === 0) {
        //验证基本信息
        baseInfoRef.value.validate(valid => {
            if (valid) {
                activeStep.value++
            } else {
                ElMessage.error('请填写完整的基本信息')
            }
        })
    } else if (activeStep.value === 1) {
        //验证证明材料
        proofRef.value.validate(valid => {
            if (valid) {
                activeStep.value++
            } else {
                ElMessage.error('请上传必要的证明材料')
            }
        })
    }
}

//上一步操作
const prevStep = () => {
    if (activeStep.value > 0) {
        activeStep.value--
    }
}

//提交表单
const submitForm = () => {
    //将数组转换为字符串
    form.value.mainProducts = form.value.mainProducts.join(",")
    console.log(form.value.mainProducts, 'daadadsads')
    loading.value = true
    //调用api提交数据
    addFarmers(form.value).then(res => {
        loading.value = false
        ElMessage.success("认证申请提交成功, 我们将在3个工作日内完成审核")
        //提交成功后重新获取认证状态
        getAuthStatus()
    })
}

//当前用户的认证状态
const authStatus = ref('')

//获取认证状态
const getAuthStatus = () => {
    selectIsAuth().then(res => {
        authStatus.value = res.msg
    })
}

//组件加载时调用
onMounted(() => {
    getAuthStatus()
})
</script>

<style scoped>
/* 主容器样式 */
.farmer-auth-container {
    max-width: 900px; /* 最大宽度限制 */
    margin: 0 auto; /* 水平居中 */
    padding: 20px; /* 内边距 */
}

/* 上传提示文字样式 */
.upload-tip {
    margin-top: 8px; /* 上边距 */
    font-size: 14px; /* 字体大小 */
    color: #999; /* 文字颜色 */
    line-height: 1.5; /* 行高 */
}

/* 返回按钮容器 */
.back-to-home {
    margin-bottom: 20px; /* 下边距 */
}

/* 返回按钮样式 */
.back-button {
    padding: 0; /* 内边距清零 */
    font-size: 16px; /* 字体大小 */
    color: #666; /* 文字颜色 */
}

/* 返回按钮悬停效果 */
.back-button:hover {
    color: #3AAE6E; /* 悬停颜色 */
}

/* 返回按钮图标样式 */
.back-button .el-icon {
    margin-right: 5px; /* 图标右边距 */
}

/* 认证头部样式 */
.auth-header {
    text-align: center; /* 文字居中 */
    margin-bottom: 30px; /* 下边距 */
}

/* 认证标题样式 */
.auth-header h2 {
    font-size: 28px; /* 字体大小 */
    color: #2c8a3e; /* 文字颜色 */
    margin-bottom: 10px; /* 下边距 */
}

/* 认证描述文字样式 */
.auth-header p {
    font-size: 16px; /* 字体大小 */
    color: #666; /* 文字颜色 */
}

/* 步骤条样式 */
.auth-steps {
    margin-bottom: 40px; /* 下边距 */
}

/* 表单容器样式 */
.auth-form-container {
    background-color: #fff; /* 白色背景 */
    padding: 30px; /* 内边距 */
    border-radius: 8px; /* 圆角 */
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); /* 阴影效果 */
}

/* 表单区域样式 */
.auth-form-section {
    padding: 20px; /* 内边距 */
}

/* 表单操作按钮区域 */
.form-actions {
    margin-top: 30px; /* 上边距 */
    text-align: center; /* 文字居中 */
}

/* 审核区域样式 */
.review-section {
    padding: 20px; /* 内边距 */
}

/* 上传材料预览区域 */
.upload-review {
    margin-top: 30px; /* 上边距 */
}

/* 上传材料预览标题 */
.upload-review h4 {
    margin-bottom: 20px; /* 下边距 */
    color: #333; /* 文字颜色 */
    font-size: 18px; /* 字体大小 */
}

/* 单个上传项样式 */
.upload-item {
    margin-bottom: 20px; /* 下边距 */
}

/* 上传项标题样式 */
.upload-item h5 {
    margin-bottom: 10px; /* 下边距 */
    color: #666; /* 文字颜色 */
}

/* 预览图片容器 */
.preview-images {
    display: flex; /* 弹性布局 */
    gap: 10px; /* 子项间距 */
    flex-wrap: wrap; /* 允许换行 */
}

/* 预览图片样式 */
.preview-images .el-image {
    width: 100px; /* 宽度 */
    height: 100px; /* 高度 */
    border: 1px solid #eee; /* 边框 */
    border-radius: 4px; /* 圆角 */
}

/* 状态容器公共样式 */
.status-container {
    max-width: 600px; /* 最大宽度 */
    margin: 50px auto; /* 外边距 */
    padding: 30px; /* 内边距 */
    background: #fff; /* 白色背景 */
    border-radius: 8px; /* 圆角 */
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1); /* 阴影效果 */
}
</style>
