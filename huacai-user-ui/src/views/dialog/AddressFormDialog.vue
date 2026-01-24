<template>
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
</template>

<script setup>
import {addAddresses, getAddresses, updateAddresses} from "@/api/assisting/addresses.js";

//获取当前组件的实例
const {proxy} = getCurrentInstance()

//是否打开弹窗
const open = ref(false)

//弹窗title
const title = ref('')

//表单
const form = ref({})

//数据验证
const rules = ref({
    name: [
        {required: true, message: "收货人不能为空", trigger: "blur"}
    ],
    phone: [
        {required: true, message: "手机号码不能为空", trigger: "blur"}
    ],
    detail: [
        {required: true, message: "详细地址不能为空", trigger: "blur"}
    ],
})

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

/** 新增按钮操作 */
const handleAdd = () => {
    reset()
    open.value = true
    title.value = "添加收货地址"
}

/** 修改按钮操作 */
const handleUpdate = (address) => {
    reset()
    const _addressesId = address.addressesId
    getAddresses(_addressesId).then(response => {
        form.value = response.data
        open.value = true
        title.value = "修改收货地址"
    })
}

const emit = defineEmits(['ok'])

/** 提交按钮 */
const submitForm = () => {
    proxy.$refs["addressesRef"].validate(valid => {
        if (valid) {
            if (form.value.addressesId != null) {
                updateAddresses(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功")
                    open.value = false
                    emit('ok')
                })
            } else {
                addAddresses(form.value).then(response => {
                    proxy.$modal.msgSuccess("新增成功")
                    open.value = false
                    emit('ok')
                })
            }
        }
    })
}

//取消按钮
const cancel = () => {
    //关闭弹窗
    open.value = false
    //重置表单
    reset()
}

// 必须暴露方法, 父组件才能调用
defineExpose({
    handleAdd,
    handleUpdate
})
</script>

<style scoped>

</style>
