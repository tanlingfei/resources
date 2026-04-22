<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-width="150px" class="form">
    <el-form-item label="当前密码" prop="password">
      <el-input v-model="form.password" type="password" placeholder="当前密码"></el-input>
    </el-form-item>
    <el-form-item label="新密码" prop="npassword">
      <el-input v-model="form.npassword" type="password" placeholder="新密码"></el-input>
    </el-form-item>
    <el-form-item label="确认密码" prop="cfpassword">
      <el-input v-model="form.cfpassword" type="password" placeholder="确认密码"></el-input>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" class="w100p" @click="submit">提交</el-button>
    </el-form-item>
  </el-form>
</template>
<script setup>
import {changePsw} from '@/apis/personal';

const formRef = ref();
const form = reactive({
  password: '',
  npassword: '',
  cfpassword: ''
})

// computed
const rules = computed(() => {
  return {
    password: {
      required: true,
      min: 4,
      message: '密码不能小于4位',
      trigger: "blur",
    },
    npassword: [
      {
        required: true,
        min: 4,
        message: '密码不能小于4位',
        trigger: "blur",
      }
    ],
    cfpassword: [
      {
        required: true,
        min: 4,
        message: '密码不能小于4位',
        trigger: "blur",
      },
      {
        validator: (rule, value, callback) => {
          if (value !== form.npassword) {
            callback(new Error('两次密码不一致'));
          } else {
            callback();
          }
        },
        trigger: "blur",
      },
    ]
  }
})

// methods
function submit() {
  formRef.value.validate(valid => {
    if (!valid) return;
    const {password, npassword, cfpassword} = form;
    changePsw({password, npassword, cfpassword}).then(() => {
      ElMessage.success('修改成功');
      formRef.value.resetFields();
    })
  })
}
</script>
<style lang="scss" scoped>
.form {
  width: 450px;
}
</style>
