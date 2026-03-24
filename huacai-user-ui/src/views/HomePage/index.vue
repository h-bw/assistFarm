<template>
  <div class="site-shell">
    <!--头部-->
    <div class="header-container">
      <div class="header">
        <div class="header-left">
          <div class="logo-wrap">
            <img src="@/assets/logo/logo.png" alt="">
          </div>
          <div class="brand-copy" @click="router.push('/index/home')">
            <div class="titles">助农扶贫商城</div>
            <div class="subtitles">甄选优质农产品，连接田间与餐桌</div>
          </div>
        </div>
        <div class="header-center">
          <div class="front-header-nav">
            <el-menu
                :default-active="route.path"
                mode="horizontal"
                :router="true"
                class="custom-menu"
              >
              <el-menu-item index="/index/home">
                <el-icon><HomeFilled /></el-icon>
                <span>首页</span>
              </el-menu-item>
              <el-menu-item index="/index/products">
                <el-icon><Goods /></el-icon>
                <span>扶贫产品</span>
              </el-menu-item>
              <el-menu-item index="/index/shoppingCart">
                <el-icon><ShoppingCart /></el-icon>
                <span>我的购物车</span>
              </el-menu-item>
              <el-menu-item index="/index/myOrder">
                <el-icon><Tickets /></el-icon>
                <span>我的订单</span>
              </el-menu-item>
              <el-menu-item index="/index/policies">
                <el-icon><Document /></el-icon>
                <span>助农政策</span>
              </el-menu-item>
              <el-menu-item index="/index/profile">
                <el-icon><User /></el-icon>
                <span>个人中心</span>
              </el-menu-item>
            </el-menu>

          </div>
        </div>
        <div class="header-right">
          <div v-if="!nickName" class="auth-buttons">
            <el-button plain @click="router.push('/login')">登录</el-button>
            <el-button type="primary" @click="router.push('/register')">注册</el-button>
          </div>
          <div v-else class="user-info">
            <el-button
                type="primary"
                class="farmer-auth-button"
                @click="router.push('/index/auth')"
            >农户认证
            </el-button>
            <el-dropdown>
              <div class="header-dropdown">
                <img :src="avatar" alt="">
                <div class="user-name">
                  <span>{{ nickName }}</span><i class="el-icon-arrow-down" style="margin-left: 5px"></i>
                </div>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>
                    <el-button type="text" style="text-decoration: none" @click.native="logout">退出</el-button>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <div class="page-shell">
      <router-view/>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUser } from "@/api/system/user"
import useUserStore from "@/store/modules/user.js";
import {ElMessageBox} from "element-plus";
import {Document, Goods, HomeFilled, ShoppingCart, Tickets, User} from "@element-plus/icons-vue";

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const nickName = ref(null)
const avatar = computed(() => userStore.avatar)

onMounted(() => {
  getList()
})

const getList = () => {
  getUser(userStore.id).then(res => {
    nickName.value = res.data.nickName
  })
}

const logout = () => {
  ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logOut().then(() => {
      location.href = '/index'
    })
  }).catch(() => { })
}
</script>

<style scoped>
.site-shell {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(127, 190, 102, 0.15), transparent 24%),
    radial-gradient(circle at top right, rgba(255, 202, 116, 0.16), transparent 22%),
    linear-gradient(180deg, #f8fbf4 0%, #f4f8f0 40%, #eef4e8 100%);
}

.header-container {
  position: sticky;
  top: 0;
  z-index: 30;
  padding: 18px 18px 10px;
  background: linear-gradient(180deg, rgba(248, 251, 244, 0.94), rgba(248, 251, 244, 0.45));
  backdrop-filter: blur(10px);
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  max-width: 1450px;
  margin: 0 auto;
  padding: 14px 22px;
  border-radius: 28px;
  border: 1px solid rgba(118, 166, 88, 0.18);
  background: rgba(255, 255, 255, 0.84);
  box-shadow: 0 18px 38px rgba(80, 109, 60, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 280px;
}

.logo-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 58px;
  height: 58px;
  border-radius: 18px;
  background: linear-gradient(145deg, #eef6e0, #ffffff);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.72);
}

.logo-wrap img {
  width: 38px;
  height: 38px;
  object-fit: contain;
}

.brand-copy {
  cursor: pointer;
}

.titles {
  font-size: 22px;
  font-weight: 800;
  line-height: 1.15;
  color: #23421f;
  letter-spacing: 1px;
}

.subtitles {
  margin-top: 4px;
  font-size: 12px;
  color: #72846a;
  letter-spacing: 0.4px;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  min-width: 0;
}

.front-header-nav {
  width: 100%;
  max-width: 760px;
}

:deep(.custom-menu) {
  justify-content: center;
  gap: 6px;
  border-bottom: none;
  background: transparent;
}

:deep(.custom-menu .el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 0;
  border-bottom: none !important;
  border-radius: 16px;
  color: #4e6146;
  font-weight: 600;
  transition: all 0.25s ease;
}

:deep(.custom-menu .el-menu-item:hover) {
  color: #2f6630;
  background: rgba(120, 171, 89, 0.12);
}

:deep(.custom-menu .el-menu-item.is-active) {
  color: #215a2b;
  background: linear-gradient(135deg, rgba(118, 181, 90, 0.18), rgba(255, 208, 126, 0.2));
  box-shadow: inset 0 0 0 1px rgba(107, 153, 72, 0.16);
}

:deep(.custom-menu .el-icon) {
  margin-right: 6px;
}

.header-right {
  display: flex;
  justify-content: flex-end;
  min-width: 250px;
}

.auth-buttons,
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.auth-buttons :deep(.el-button),
.farmer-auth-button {
  height: 42px;
  padding: 0 18px;
  border-radius: 14px;
  font-weight: 600;
}

.header-dropdown {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 6px;
  border-radius: 16px;
  border: 1px solid rgba(129, 171, 102, 0.16);
  background: rgba(243, 248, 239, 0.92);
  cursor: pointer;
}

.header-dropdown img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(112, 176, 84, 0.18);
}

.user-name {
  color: #2f4529;
  font-weight: 600;
}

.page-shell {
  max-width: 1520px;
  margin: 0 auto;
  padding: 6px 0 36px;
}

@media (max-width: 1280px) {
  .header {
    flex-wrap: wrap;
    justify-content: center;
  }

  .header-left,
  .header-right {
    min-width: auto;
  }

  .header-center {
    order: 3;
    width: 100%;
  }
}

@media (max-width: 768px) {
  .header-container {
    padding: 12px 12px 6px;
  }

  .header {
    padding: 14px;
    border-radius: 22px;
  }

  .titles {
    font-size: 18px;
  }

  .subtitles {
    display: none;
  }

  .auth-buttons,
  .user-info {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
