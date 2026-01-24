<template>
  <!-- 政策页面容器 -->
    <div class="policies-container">
        <!-- 政策导航和内容区域 -->
        <div class="policies-content">
            <!-- 政策分类导航 -->
            <div class="policies-nav">
                <!-- 导航头部 -->
                <div class="nav-header">
                    <h2>助农政策</h2>
                    <p>了解帮扶政策, 助力乡村振兴</p>
                </div>
                <el-menu
                        :default-active="queryParams.category"
                        class="policies-menu"
                        @select="handleCategorySelect"
                        background-color="#f0f9eb"
                        text-color="#2c8a3e"
                        active-text-color="#ffffff"
                >
                    <!-- 渲染菜单项 -->
                    <el-menu-item
                            v-for="item in policies_category"
                            :key="item.value"
                            :index="item.value"
                    >
                        <el-icon><Position/></el-icon>
                        <span>{{ item.label }}</span>
                    </el-menu-item>

                </el-menu>
            </div>


            <!-- 政策内容区域 -->
            <div class="policies-main">
                <!-- 政策列表 -->
                <div class="policies-list" v-loading="loading">
                    <!-- 空状态提示 -->
                    <div v-if="policiesList.length === 0" class="empty-policies">
                        <el-empty description="暂无相关政策"/>
                    </div>

                    <div v-else>
                        <!-- 政策数据循环渲染 -->
                        <div class="policies-item" v-for="policies in policiesList" :key="policies.policiesId">
                            <!-- 政策头部 -->
                            <div class="policies-item-header">
                                <!-- 分类标签 -->
                                <dict-tag :options="policies_category" :value="policies.category"/>
                                <!-- 标题 -->
                                <h3 @click="showDetail(policies)">{{ policies.title }}</h3>
                            </div>
                            <!-- 政策信息 -->
                            <div class="policies-item-meta">
                                <span class="meta-item">
                                    <el-icon><Calendar/></el-icon>
                                    发布时间: {{ policies.createTime }}
                                </span>
                                <span class="meta-item">
                                    <el-icon><Location/></el-icon>
                                    适用地区: {{ policies.region }}
                                </span>
                                <span class="meta-item">
                                    <el-icon><OfficeBuilding/></el-icon>
                                    发布单位: {{ policies.publisher }}
                                </span>
                            </div>
                            <!-- 政策摘要 -->
                            <div class="policies-item-content">
                                {{ policies.summary }}
                            </div>
                        </div>

                        <!-- 分页组件 -->
                        <div class="policies-pagination">
                            <pagination
                                    v-show="total>0"
                                    :total="total"
                                    v-model:page="queryParams.pageNum"
                                    v-model:limit="queryParams.pageSize"
                                    @pagination="getList"
                                    :pageSizes="[5,10,15,20]"
                            />
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 政策详情对话框 -->
        <vxe-modal height="90vh" title="政策详情" v-model="open" width="70%" show-maximize showFooter resize>
            <!-- 详情内容 -->
            <div>
                <!-- 详情头部 -->
                <div class="detail-header">
                    <div class="detail-meta">
                        <dict-tag :options="policies_category" :value="currentPolicy.category"/>
                        <span class="meta-item">
                                    <el-icon><Calendar/></el-icon>
                                    发布时间: {{ currentPolicy.createTime }}
                                </span>
                        <span class="meta-item">
                                    <el-icon><Location/></el-icon>
                                    适用地区: {{ currentPolicy.region }}
                                </span>
                        <span class="meta-item">
                                    <el-icon><OfficeBuilding/></el-icon>
                                    发布单位: {{ currentPolicy.publisher }}
                                </span>
                    </div>
                </div>

                <!-- 政策内容 -->
                <div class="detail-content" v-html="currentPolicy.content"/>

                <!-- 联系方式 -->
                <div class="detail-contacts">
                    <h4>联系方式</h4>
                    <div class="contact-list">
                        <div class="contact-item">
                            <p>负责部门: {{ currentPolicy.department }}</p>
                            <p>联系人: {{ currentPolicy.contactPerson }}</p>
                            <p>电话: {{ currentPolicy.phone }}</p>
                            <p>地址: {{ currentPolicy.address }}</p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 对话框底部 -->
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="open = false">关闭</el-button>
                </div>
            </template>
        </vxe-modal>

    </div>
</template>

<script setup>
import {listPolicies} from "@/api/assisting/policies.js";
import {Calendar, Location, OfficeBuilding, Position} from "@element-plus/icons-vue";

const {proxy} = getCurrentInstance()
const {policies_category} = proxy.useDict('policies_category')

//是否打开详情弹窗
const open = ref(false)

//当前查看的政策
const currentPolicy = ref({})

//打开政策详情
const showDetail = (policies) => {
    currentPolicy.value = policies
    open.value = true
}

//处理分类选择
const handleCategorySelect = (value) => {
    queryParams.value.category = value
    getList()
}

//查询参数
const queryParams = ref({
    pageNum: 1,
    pageSize: 5,
    title: null,
    category: '金融支持', //默认选中的分类
})

//加载状态
const loading = ref(false)

//数据总数
const total = ref(0)

// 政策列表数据
const policiesList = ref([])

//获取政策列表数据
const getList = () => {
    //打开加载状态
    loading.value = true
    listPolicies(queryParams.value).then(res => {
        policiesList.value = res.rows
        total.value = res.total
        //关闭加载状态
        loading.value = false
    })
}

//组件挂载时获取数据
onMounted(() => {
    getList()
})
</script>

<style scoped>
/* 政策页面容器样式 */
.policies-container {
    min-height: 100vh; /* 最小高度为视口高度 */
    background-color: #f5f7fa; /* 浅灰色背景 */
    padding: 20px 0; /* 上下内边距 */
}

/* 政策内容区域样式 */
.policies-content {
    display: flex; /* 弹性布局 */
    max-width: 1450px; /* 最大宽度 */
    margin: 0 auto; /* 水平居中 */
    background-color: #fff; /* 白色背景 */
    border-radius: 8px; /* 圆角 */
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); /* 阴影效果 */
    overflow: hidden; /* 溢出隐藏 */
}

/* 政策导航样式 */
.policies-nav {
    width: 220px; /* 固定宽度 */
    background-color: #329860; /* 绿色背景 */
    border-right: 1px solid #539b2e; /* 右侧边框 */
    padding: 20px 0; /* 上下内边距 */
}

/* 导航头部样式 */
.nav-header {
    padding: 0 20px 20px; /* 内边距 */
    border-bottom: 1px solid #e1f3d8; /* 底部边框 */
    margin-bottom: 10px; /* 底部外边距 */
}

.nav-header h2 {
    font-size: 20px; /* 字体大小 */
    color: #fcfcfc; /* 字体颜色 */
    margin: 0 0 5px 0; /* 外边距 */
}

.nav-header p {
    font-size: 12px; /* 字体大小 */
    color: #ffffff; /* 字体颜色 */
    margin: 0; /* 外边距清零 */
}

/* 菜单样式 */
.policies-menu {
    border-right: none; /* 去除右侧边框 */
}

/* 深度选择器修改Element UI组件内部样式 */
.policies-menu :deep(.el-menu-item) {
    height: 48px; /* 菜单项高度 */
    line-height: 48px; /* 行高 */
    margin: 5px 0; /* 上下外边距 */
    transition: all 0.3s; /* 过渡效果 */
}

/* 激活状态菜单项样式 */
.policies-menu :deep(.el-menu-item.is-active) {
    background-color: #2c8a3e !important; /* 深绿色背景 */
    color: #ffffff !important; /* 白色文字 */
    font-weight: bold; /* 粗体 */
}

/* 菜单项悬停效果 */
.policies-menu :deep(.el-menu-item:hover) {
    background-color: #96e171 !important; /* 浅绿色背景 */
}

/* 菜单图标样式 */
.policies-menu :deep(.el-menu-item .el-icon) {
    color: inherit; /* 继承文字颜色 */
}

/* 主要内容区域样式 */
.policies-main {
    flex: 1; /* 占据剩余空间 */
    padding: 20px; /* 内边距 */
}

/* 政策列表样式 */
.policies-list {
    min-height: 500px; /* 最小高度 */
}

/* 空状态样式 */
.empty-policies {
    padding: 100px 0; /* 上下内边距 */
    text-align: center; /* 文本居中 */
}

/* 政策项样式 */
.policies-item {
    padding: 20px; /* 内边距 */
    border-bottom: 1px solid #eee; /* 底部边框 */
    transition: all 0.3s; /* 过渡效果 */
}

/* 政策项悬停效果 */
.policies-item:hover {
    background-color: #f9f9f9; /* 浅灰色背景 */
}

/* 政策项头部样式 */
.policies-item-header {
    display: flex; /* 弹性布局 */
    align-items: center; /* 垂直居中 */
    margin-bottom: 10px; /* 底部外边距 */
}

.policies-item-header h3 {
    margin: 0 0 0 10px; /* 外边距 */
    font-size: 18px; /* 字体大小 */
    cursor: pointer; /* 手型光标 */
    transition: color 0.3s; /* 颜色过渡效果 */
}

/* 标题悬停效果 */
.policies-item-header h3:hover {
    color: #2c8a3e; /* 绿色文字 */
}

/* 元信息样式 */
.policies-item-meta {
    display: flex; /* 弹性布局 */
    flex-wrap: wrap; /* 允许换行 */
    gap: 15px; /* 项间距 */
    margin-bottom: 15px; /* 底部外边距 */
    font-size: 12px; /* 字体大小 */
    color: #666; /* 文字颜色 */
}

/* 单个元信息项样式 */
.meta-item {
    display: flex; /* 弹性布局 */
    align-items: center; /* 垂直居中 */
}

.meta-item .el-icon {
    margin-right: 5px; /* 图标右侧间距 */
}

/* 政策内容摘要样式 */
.policies-item-content {
    margin-bottom: 15px; /* 底部外边距 */
    line-height: 1.6; /* 行高 */
    color: #333; /* 文字颜色 */
    display: -webkit-box; /* 弹性盒子 */
    -webkit-line-clamp: 3; /* 限制3行 */
    -webkit-box-orient: vertical; /* 垂直方向 */
    overflow: hidden; /* 溢出隐藏 */
}

/* 分页样式 */
.policies-pagination {
    margin-top: 30px; /* 顶部外边距 */
    text-align: center; /* 文本居中 */
}

/* 详情对话框样式 */
.detail-header {
    margin-bottom: 20px; /* 底部外边距 */
    padding-bottom: 15px; /* 底部内边距 */
    border-bottom: 1px solid #eee; /* 底部边框 */
}

/* 详情元信息样式 */
.detail-meta {
    display: flex; /* 弹性布局 */
    flex-wrap: wrap; /* 允许换行 */
    align-items: center; /* 垂直居中 */
    gap: 15px; /* 项间距 */
    font-size: 14px; /* 字体大小 */
    color: #666; /* 文字颜色 */
}

.detail-meta .el-icon {
    margin-right: 5px; /* 图标右侧间距 */
}

/* 详情内容样式 */
.detail-content {
    margin-bottom: 30px; /* 底部外边距 */
}

/* 详情内容标题样式 */
.detail-content h4 {
    font-size: 16px; /* 字体大小 */
    color: #2c8a3e; /* 绿色文字 */
    margin: 20px 0 10px; /* 外边距 */
    padding-bottom: 5px; /* 底部内边距 */
    border-bottom: 1px solid #eee; /* 底部边框 */
}

/* 段落和列表样式 */
.detail-content p,
.detail-content ol,
.detail-content ul {
    margin: 10px 0; /* 上下外边距 */
    line-height: 1.8; /* 行高 */
}

.detail-content li {
    margin: 5px 0; /* 上下外边距 */
}

/* 联系方式区域样式 */
.detail-contacts {
    margin-top: 30px; /* 顶部外边距 */
    padding-top: 20px; /* 顶部内边距 */
    border-top: 1px solid #eee; /* 顶部边框 */
}

/* 标题样式 */
.detail-attachments h4,
.detail-contacts h4 {
    font-size: 16px; /* 字体大小 */
    color: #2c8a3e; /* 绿色文字 */
    margin-bottom: 15px; /* 底部外边距 */
}

/* 联系列表样式 */
.contact-list {
    display: grid; /* 网格布局 */
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); /* 自适应列 */
    gap: 20px; /* 间隙 */
}

/* 联系项样式 */
.contact-item {
    padding: 15px; /* 内边距 */
    background-color: #f9f9f9; /* 浅灰色背景 */
    border-radius: 4px; /* 圆角 */
}

.contact-item p {
    margin: 5px 0; /* 上下外边距 */
    font-size: 14px; /* 字体大小 */
}

/* 对话框底部样式 */
.dialog-footer {
    text-align: center; /* 文本居中 */
}

/* 响应式设计 - 中等屏幕 */
@media (max-width: 992px) {
    .policies-content {
        flex-direction: column; /* 垂直排列 */
    }

    .policies-nav {
        width: 100%; /* 全宽 */
        border-right: none; /* 去除右侧边框 */
        border-bottom: 1px solid #eee; /* 底部边框 */
    }

    .policies-menu {
        display: flex; /* 弹性布局 */
        flex-wrap: wrap; /* 允许换行 */
        justify-content: center; /* 水平居中 */
    }

    .policies-menu :deep(.el-menu-item) {
        height: 40px; /* 高度调整 */
        line-height: 40px; /* 行高调整 */
        padding: 0 15px; /* 左右内边距 */
    }
}

/* 响应式设计 - 小屏幕 */
@media (max-width: 768px) {
    .policies-item-meta {
        flex-direction: column; /* 垂直排列 */
        gap: 8px; /* 间隙调整 */
        align-items: flex-start; /* 左对齐 */
    }

    .contact-list {
        grid-template-columns: 1fr; /* 单列布局 */
    }
}
</style>
