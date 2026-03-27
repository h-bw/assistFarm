<template>
  <div class="site-shell">
    <div class="header-container">
      <div class="header">
        <div class="header-left">
          <div class="logo-wrap">
            <img src="@/assets/logo/logo.png" alt="助农扶贫商城">
          </div>
          <div class="brand-copy" @click="router.push('/index/home')">
            <div class="titles">助农扶贫商城</div>
            <div class="subtitles">甄选优质农产品，连接田间与餐桌</div>
          </div>
        </div>

        <div class="header-center">
          <div class="nav-track">
            <button
              v-for="item in navItems"
              :key="item.path"
              type="button"
              class="nav-pill"
              :class="{ active: route.path === item.path }"
              @click="router.push(item.path)"
            >
              <el-icon>
                <component :is="item.icon" />
              </el-icon>
              <span>{{ item.label }}</span>
            </button>
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
            >
              农户认证
            </el-button>
            <el-dropdown>
              <div class="header-dropdown">
                <img :src="avatar" alt="用户头像">
                <div class="user-name">
                  <span>{{ nickName }}</span>
                  <i class="el-icon-arrow-down" style="margin-left: 5px"></i>
                </div>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>
                    <el-button type="text" style="text-decoration: none" @click="logout">
                      退出登录
                    </el-button>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <div class="page-shell">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Document, Goods, HomeFilled, ShoppingCart, Tickets, User } from '@element-plus/icons-vue'
import { getUser } from '@/api/system/user'
import useUserStore from '@/store/modules/user.js'
import { getToken } from '@/utils/auth'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const nickName = ref(null)
const avatar = computed(() => userStore.avatar)

const navItems = [
  { path: '/index/home', label: '首页', icon: HomeFilled },
  { path: '/index/products', label: '助农产品', icon: Goods },
  { path: '/index/shoppingCart', label: '我的购物车', icon: ShoppingCart },
  { path: '/index/myOrder', label: '我的订单', icon: Tickets },
  { path: '/index/policies', label: '助农政策', icon: Document },
  { path: '/index/profile', label: '个人中心', icon: User }
]

onMounted(() => {
  getList()
})

const getList = () => {
  if (!getToken() || !userStore.id) {
    nickName.value = null
    return
  }
  getUser(userStore.id).then((res) => {
    nickName.value = res.data.nickName
  }).catch(() => {
    nickName.value = null
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
  }).catch(() => {})
}
</script>

<style scoped>
.site-shell {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(122, 171, 88, 0.12), transparent 24%),
    radial-gradient(circle at top right, rgba(204, 184, 126, 0.12), transparent 22%),
    linear-gradient(180deg, #f6f7f1 0%, #f3f5ed 42%, #edf1e7 100%);
}

.header-container {
  position: sticky;
  top: 0;
  z-index: 30;
  padding: 10px 18px 8px;
  background: linear-gradient(180deg, rgba(246, 247, 241, 0.96), rgba(246, 247, 241, 0.64));
  backdrop-filter: blur(10px);
}

.header {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr) 272px;
  align-items: center;
  gap: 16px;
  max-width: 1520px;
  margin: 0 auto;
  padding: 10px 18px;
  border: 1px solid rgba(120, 145, 101, 0.18);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 12px 28px rgba(80, 109, 60, 0.06);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.logo-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  flex-shrink: 0;
  border-radius: 16px;
  background: linear-gradient(145deg, #eef6e0, #ffffff);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.72);
}

.logo-wrap img {
  width: 34px;
  height: 34px;
  object-fit: contain;
}

.brand-copy {
  min-width: 0;
  cursor: pointer;
}

.titles {
  color: #132813;
  font-size: 17px;
  font-weight: 800;
  line-height: 1.2;
  letter-spacing: 0.3px;
}

.subtitles {
  margin-top: 3px;
  color: #466047;
  font-size: 12px;
  line-height: 1.35;
  white-space: nowrap;
}

.header-center {
  display: flex;
  justify-content: flex-start;
  min-width: 0;
}

.nav-track {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
  width: 100%;
  padding: 5px 6px;
  border-radius: 20px;
  background: #ffffff;
  box-shadow:
    inset 0 0 0 1px rgba(123, 147, 102, 0.18),
    0 4px 14px rgba(74, 98, 59, 0.05);
}

.nav-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 46px;
  padding: 0 18px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #6b9b49, #7cb157);
  box-shadow:
    inset 0 0 0 1px rgba(108, 148, 77, 0.2),
    0 10px 18px rgba(83, 122, 57, 0.16);
  color: #ffffff;
  font-size: 14px;
  font-weight: 600;
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.25s ease;
}

.nav-pill:hover {
  background: linear-gradient(135deg, #5d8e40, #72a74f);
}

.nav-pill.active {
  background: linear-gradient(135deg, #4c7c34, #689d46);
  box-shadow:
    inset 0 0 0 1px rgba(255, 255, 255, 0.08),
    0 12px 22px rgba(76, 112, 50, 0.24);
}

.nav-pill .el-icon {
  font-size: 15px;
}

.header-right {
  display: flex;
  justify-content: flex-start;
}

.auth-buttons,
.user-info {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 10px;
  width: 100%;
}

.auth-buttons :deep(.el-button),
.farmer-auth-button {
  height: 40px;
  padding: 0 16px;
  border-radius: 14px;
  font-weight: 600;
}

.auth-buttons :deep(.el-button) {
  color: #1f331b;
  border-color: rgba(114, 151, 89, 0.28);
  background: #ffffff;
}

.auth-buttons :deep(.el-button--primary),
.farmer-auth-button {
  color: #ffffff;
  border-color: transparent;
  background: linear-gradient(135deg, #4d7d35, #6b9f49);
}

.header-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 122px;
  padding: 5px 12px 5px 5px;
  border: 1px solid rgba(129, 171, 102, 0.18);
  border-radius: 16px;
  background: #f7faf3;
  cursor: pointer;
}

.header-dropdown img {
  width: 38px;
  height: 38px;
  border: 2px solid rgba(112, 176, 84, 0.18);
  border-radius: 50%;
  object-fit: cover;
}

.user-name {
  color: #182d16;
  font-weight: 600;
  white-space: nowrap;
}

.page-shell {
  max-width: 1520px;
  margin: 0 auto;
  padding: 6px 0 36px;
}

@media (max-width: 1480px) {
  .header {
    grid-template-columns: 290px minmax(0, 1fr) 258px;
  }

  .nav-pill {
    height: 44px;
    padding: 0 14px;
    font-size: 13px;
  }
}

@media (max-width: 1320px) {
  .header {
    grid-template-columns: 1fr;
    justify-items: center;
  }

  .header-left,
  .header-right,
  .header-center {
    width: 100%;
    justify-content: center;
  }

  .nav-track,
  .user-info,
  .auth-buttons {
    justify-content: center;
    flex-wrap: wrap;
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

  .subtitles {
    display: none;
  }
}
</style>
