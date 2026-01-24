<template>
    <div class="app-container">
        <el-row :gutter="20">
            <el-col :span="6" :xs="24">
                <el-card class="box-card">
                    <template v-slot:header>
                        <div class="clearfix">
                            <span>个人信息</span>
                        </div>
                    </template>
                    <div>
                        <div class="text-center">
                            <userAvatar/>
                        </div>
                        <ul class="list-group list-group-striped">
                            <li class="list-group-item">
                                <svg-icon icon-class="user"/>
                                用户名称
                                <div class="pull-right">{{ state.user.userName }}</div>
                            </li>
                            <li class="list-group-item">
                                <svg-icon icon-class="phone"/>
                                手机号码
                                <div class="pull-right">{{ state.user.phonenumber }}</div>
                            </li>
                            <li class="list-group-item">
                                <svg-icon icon-class="email"/>
                                用户邮箱
                                <div class="pull-right">{{ state.user.email }}</div>
                            </li>
                            <li class="list-group-item">
                                <svg-icon icon-class="tree"/>
                                所属部门
                                <div class="pull-right" v-if="state.user.dept">{{ state.user.dept.deptName }} /
                                    {{ state.postGroup }}
                                </div>
                            </li>
                            <li class="list-group-item">
                                <svg-icon icon-class="peoples"/>
                                所属角色
                                <div class="pull-right">{{ state.roleGroup }}</div>
                            </li>
                            <li class="list-group-item">
                                <svg-icon icon-class="date"/>
                                创建日期
                                <div class="pull-right">{{ state.user.createTime }}</div>
                            </li>
                            <li class="list-group-item">
                                <el-icon><Money/></el-icon>
                                账户余额
                                <div class="pull-right">{{ balance }}元</div>
                            </li>
                        </ul>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="18" :xs="24">
                <el-card>
                    <template v-slot:header>
                        <div class="clearfix">
                            <span>基本资料</span>
                        </div>
                    </template>
                    <el-tabs v-model="activeTab">
                        <el-tab-pane label="基本资料" name="userinfo">
                            <userInfo :user="state.user"/>
                        </el-tab-pane>
                        <el-tab-pane label="修改密码" name="resetPwd">
                            <resetPwd/>
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
import {getUser, getUserProfile} from "@/api/system/user"
import useUserStore from "@/store/modules/user.js";
import {Money} from "@element-plus/icons-vue";

const activeTab = ref("userinfo")
const state = reactive({
    user: {},
    roleGroup: {},
    postGroup: {}
})

//账户余额
const balance = ref(0)

//当前用户ID
const loginUser = useUserStore()

function getUserItem() {
    getUserProfile().then(response => {
        state.user = response.data
        getUser(loginUser.id).then(res => {
            balance.value = res.data.balance
        })
        state.roleGroup = response.roleGroup
        state.postGroup = response.postGroup
    })
}

getUserItem()
</script>
