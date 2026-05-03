<template>
  <div class="profile-page">
    <section class="profile-hero">
      <div>
        <p class="hero-kicker">个人中心</p>
        <h1 class="hero-title">维护账号资料与安全信息</h1>
      </div>
      <div class="hero-badge">
        <span>账号状态</span>
        <strong>正常使用</strong>
      </div>
    </section>

    <el-row :gutter="20">
      <el-col :span="6" :xs="24">
        <el-card class="profile-side-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
            </div>
          </template>
          <div class="text-center">
            <userAvatar />
          </div>
          <ul class="list-group list-group-striped">
            <li class="list-group-item">
              <svg-icon icon-class="user" />用户名
              <div class="pull-right">{{ state.user.userName }}</div>
            </li>
            <li class="list-group-item">
              <svg-icon icon-class="phone" />手机号码
              <div class="pull-right">{{ state.user.phonenumber }}</div>
            </li>
            <li class="list-group-item">
              <svg-icon icon-class="email" />用户邮箱
              <div class="pull-right">{{ state.user.email }}</div>
            </li>
            <li class="list-group-item">
              <svg-icon icon-class="date" />创建日期
              <div class="pull-right">{{ state.user.createTime }}</div>
            </li>
          </ul>
        </el-card>
      </el-col>
      <el-col :span="18" :xs="24">
        <el-card class="profile-main-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>基本资料</span>
            </div>
          </template>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本资料" name="userinfo">
              <userInfo :user="state.user" />
            </el-tab-pane>
            <el-tab-pane label="修改密码" name="resetPwd">
              <resetPwd />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Profile">
import userAvatar from "./userAvatar"
import userInfo from "./userInfo"
import resetPwd from "./resetPwd"
import { getUserProfile } from "@/api/system/user"

const activeTab = ref("userinfo")
const state = reactive({
  user: {},
  roleGroup: {},
  postGroup: {}
})

function getUser() {
  getUserProfile().then(response => {
    state.user = response.data
    state.roleGroup = response.roleGroup
    state.postGroup = response.postGroup
  })
}

getUser()
</script>

<style scoped>
.profile-page {
  max-width: 1360px;
  margin: 0 auto;
  padding: 24px 20px 40px;
}

.profile-hero,
.profile-side-card,
.profile-main-card {
  border: 1px solid rgba(104, 152, 85, 0.14);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 42px rgba(28, 69, 41, 0.08);
}

.profile-hero {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 20px;
  padding: 28px 30px;
  border-radius: 26px;
  background:
    radial-gradient(circle at top left, rgba(90, 156, 95, 0.12), transparent 28%),
    linear-gradient(180deg, rgba(249, 253, 248, 0.96), rgba(255, 255, 255, 0.96));
}

.hero-kicker {
  margin: 0 0 8px;
  color: #2f8850;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.hero-title {
  margin: 0 0 10px;
  color: #1e3d2b;
  font-size: 34px;
}

.hero-badge {
  min-width: 138px;
  padding: 16px 18px;
  border-radius: 18px;
  background: #fff;
  border: 1px solid rgba(106, 155, 88, 0.14);
}

.hero-badge span {
  display: block;
  color: #778c80;
  font-size: 12px;
}

.hero-badge strong {
  display: block;
  margin-top: 6px;
  color: #254633;
  font-size: 20px;
}

.profile-side-card,
.profile-main-card {
  border-radius: 24px;
}

.card-header {
  color: #244332;
  font-size: 18px;
  font-weight: 700;
}

.profile-page :deep(.el-card__header) {
  border-bottom: 1px solid #edf2ee;
}

.profile-page :deep(.el-card__body) {
  padding: 24px;
}

.text-center {
  margin-bottom: 18px;
}

.list-group {
  margin: 0;
  padding: 0;
  list-style: none;
}

.list-group-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px dashed #e6ede7;
  color: #60786d;
}

.list-group-item:last-child {
  border-bottom: none;
}

.pull-right {
  color: #274333;
  font-weight: 600;
}

.profile-page :deep(.el-tabs__item) {
  height: 46px;
  font-weight: 600;
}

@media (max-width: 992px) {
  .profile-hero {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .profile-page {
    padding: 16px 14px 34px;
  }

  .profile-hero,
  .profile-side-card,
  .profile-main-card {
    border-radius: 20px;
  }

  .hero-title {
    font-size: 28px;
  }
}
</style>
